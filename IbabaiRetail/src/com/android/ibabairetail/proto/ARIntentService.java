package com.android.ibabairetail.proto;

import java.util.ArrayList;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

public class ARIntentService extends IntentService {
	private SharedPreferences shared_prefs;	
	private static final long GEOFENCE_EXPIRATION_TIME  = Geofence.NEVER_EXPIRE;
	private static final int NOTIFY_ID = 1000;
	private int previous_type;
	private SimpleGeofence sgf;
	private GeofenceRequester gfr;
	private GeofenceRemover gf_remover;
	private Cursor s_cursor;
	ArrayList<Geofence> gf_list;
	DatabaseHelper dbh;	
	
	public ARIntentService() {
		 super("ARIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		shared_prefs = getApplicationContext().getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		gfr = new GeofenceRequester(this);
		s_cursor = StoresCursor();
		if (ActivityRecognitionResult.hasResult(intent) && s_cursor.getCount() > 0) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity most_probable_activity = result.getMostProbableActivity();
			int confidence = most_probable_activity.getConfidence();
			int activity_type = most_probable_activity.getType();
			Editor editor = shared_prefs.edit();
			int store = shared_prefs.getInt(IbabaiUtils.STORE_ID, 0);
			if (!shared_prefs.contains(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE)) {
				if (!isInCarOrStill(activity_type)) {
					addGeofences();
				}
				editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
				editor.apply();
			}
			else {
				previous_type = shared_prefs.getInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
					
				if (!isInCarOrStill(activity_type) && isInCarOrStill(previous_type) && (confidence >= 50)) {
					addGeofences();
					editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
					editor.apply();
					if (shared_prefs.getInt(IbabaiUtils.PA_UPDATE, 0) > 0) {
						raiseNotification(this, null);
						Editor updated_e = shared_prefs.edit();
						updated_e.putInt(IbabaiUtils.PA_UPDATE, 0);
						updated_e.apply();
					}
				}
				else if (isInCarOrStill(activity_type) && !isInCarOrStill(previous_type) && (confidence >= 50) && (store == 0)) {
					removeGeofences();
					editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);					
					editor.apply();
				}				
			}
		}
	}
		
	private boolean isInCarOrStill(int type) {
		if (type == DetectedActivity.IN_VEHICLE || type == DetectedActivity.STILL) {
			return true;
		}
		else {
			return false;
		}
	}
	public void addGeofences() {		
		int c_id = shared_prefs.getInt(IbabaiUtils.CITY, 0);
		if (c_id != 0) {			
			gf_list = new ArrayList<Geofence>();
			if (s_cursor !=null && s_cursor.moveToFirst()) {
				while (s_cursor.isAfterLast()!=true) {
					int id_ind = s_cursor.getColumnIndex(DatabaseHelper.S_ID);
					int lat_ind = s_cursor.getColumnIndex(DatabaseHelper.LAT);
					int lon_ind = s_cursor.getColumnIndex(DatabaseHelper.LON);
					int rad_ind = s_cursor.getColumnIndex(DatabaseHelper.RAD);
					int st_id = s_cursor.getInt(id_ind);
					double target_lat = s_cursor.getDouble(lat_ind);
					double target_lon = s_cursor.getDouble(lon_ind);
					int rad = s_cursor.getInt(rad_ind);					
					sgf = new SimpleGeofence(Integer.toString(st_id), target_lat, target_lon, rad, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
					gf_list.add(sgf.toGeoFence());
					s_cursor.moveToNext();
				}				
				s_cursor.close();
				dbh.close();
			}
		}
		try {
			gfr.addGeofences(gf_list);
				
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.add_geofences_already_requested_error));
		}
	}
	private Cursor StoresCursor() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	public void removeGeofences() {
		try {
			gf_remover = new GeofenceRemover(this);		
			gf_remover.removeGeofencesByIntent(gfr.getRequestPendingIntent());			
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.remove_geofences_already_requested_error));
		}
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		Bitmap bm_resized = Bitmap.createScaledBitmap(bm, 72, 72, false);
		if (e == null) {			
			b.setContentTitle("Hello!").setContentText("You have new promos from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm_resized);

			Intent outbound=new Intent(ctxt, MainActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
		
	}
}


