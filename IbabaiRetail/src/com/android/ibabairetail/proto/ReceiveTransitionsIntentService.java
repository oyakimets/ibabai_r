package com.android.ibabairetail.proto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveTransitionsIntentService extends IntentService {
	private SharedPreferences shared_prefs;	
	public static ArrayList<Integer> userPromos;
	public static ArrayList<Integer> storePromos;
	private static final int NOTIFY_ID = 1000;
	private int active_promo;
	private int store_cat;
	DatabaseHelper dbh;
	private String str;		
	private Date date_entry=null;
	private Date date_exit=null;
	private SimpleDateFormat sdf;
	private long diffMin;
	
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Intent broadcastIntent = new Intent();
		
		broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		
		if(LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
			
			Log.e(GeofenceUtils.APPTAG, getString(R.string.geofence_transition_error_detail, errorMessage));
			
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage).putExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE, "TRANSITION");
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
		else {
			int transition = LocationClient.getGeofenceTransition(intent);
			
			if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
				String geofenceId = geofences.get(0).getRequestId();
				int s_id = Integer.parseInt(geofenceId);				
				String transitionType = getTransitionString(transition);
				dbh = DatabaseHelper.getInstance(getApplicationContext());
				shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
				Editor editor = shared_prefs.edit();
				sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				if (transitionType.equals(getString(R.string.geofence_transition_entered))) {					
					store_cat = GetStoreCategory(geofenceId);
					active_promo = GetActivePromo(geofenceId);
					String entry_time = getCurrentTime();
					editor.putString(IbabaiUtils.STORE_ENTRY_TIME, entry_time);
					editor.putInt(IbabaiUtils.STORE_ID, s_id);
					editor.putInt(IbabaiUtils.LAST_STORE, s_id);
					editor.putInt(DatabaseHelper.CAT, store_cat);
					editor.putInt(IbabaiUtils.ACTIVE_PROMO, active_promo);
					editor.apply();					
					Log.d(getClass().getSimpleName(), "entering");
					if (HasPromoCat(Integer.toString(store_cat)) || active_promo != 0) {
						raiseNotification(this, null);	
						/*Intent i = new Intent(this, DelRegService.class);
						startService(i);
						 */
					}
				}
				else if (transitionType.equals(getString(R.string.geofence_transition_exited))) {
					if (s_id == shared_prefs.getInt(IbabaiUtils.LAST_STORE, 0)) {
						String exit_time = getCurrentTime();
						String entry_time = shared_prefs.getString(IbabaiUtils.STORE_ENTRY_TIME, "01/01/2014 00:00:00");
						getTimeDiff(entry_time, exit_time);
						editor.putLong(IbabaiUtils.STORE_TIME, diffMin);
						editor.putInt(IbabaiUtils.STORE_ID, 0);
						editor.putInt(DatabaseHelper.CAT, 0);
						editor.putInt(IbabaiUtils.ACTIVE_PROMO, 0);
						editor.putInt(IbabaiUtils.STORE_ENTRY_TIME, 0);
						editor.apply();			
						Log.d(getClass().getSimpleName(), "exiting");
					}
				}
				else {
					Log.d(getClass().getSimpleName(), "unknown");
				}
			}
			else {
				Log.e(GeofenceUtils.APPTAG, getString(R.string.geofence_transition_invalid_type, transition));
			}
		}
	}
	private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
	private int GetActivePromo(String store_id) {
		userPromos = new ArrayList<Integer>();
		storePromos=new ArrayList<Integer>();
		Cursor pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (!pa_cursor.isAfterLast()) {
				int pa_id = pa_cursor.getInt(id_ind);						
				userPromos.add(pa_id);
				pa_cursor.moveToNext();
			}
			pa_cursor.close();
		}
			
		Cursor ps_cursor = storePromosCursor(store_id);
		if(ps_cursor != null && ps_cursor.moveToFirst()) {
			int paid_ind = ps_cursor.getColumnIndex("promoact_id");
			while (!ps_cursor.isAfterLast()) {
				int promoact_id=ps_cursor.getInt(paid_ind);
				storePromos.add(promoact_id);				
				ps_cursor.moveToNext();
			}
			ps_cursor.close();
		}
		if (userPromos != null && storePromos != null) {
			for (int i=0; i<storePromos.size(); i++) {
				if (userPromos.contains(storePromos.get(i))) {
					active_promo = storePromos.get(i);
					break;
				}
				else {
					active_promo = 0;
				}
			}
		}
		return active_promo;
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor storeCursor(String store_id) {
		 String p_query = String.format("SELECT * FROM %s WHERE store_id="+store_id, DatabaseHelper.TABLE_S);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	
	private Cursor storePromosCursor(String store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+store_id;
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	private int GetStoreCategory(String store_id) {
		Cursor store_c = storeCursor(store_id);
		if (store_c != null && store_c.moveToFirst()) {
			int cat_ind = store_c.getColumnIndex(DatabaseHelper.CAT);
			int category = store_c.getInt(cat_ind);
			return category;
		}
		else {
			return 0;
		}
	}
	private boolean HasPromoCat(String cat) {
		String p_query = String.format("SELECT * FROM %s WHERE stopped=0 AND category="+cat, DatabaseHelper.TABLE_P);
		Cursor promo_c = dbh.getReadableDatabase().rawQuery(p_query, null);
		if (promo_c.getCount() >0) {
			return true;
		}
		else {
			return false;
		}
		
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		Bitmap bm_resized = Bitmap.createScaledBitmap(bm, 72, 72, false);
		if (e == null) {
			if (active_promo != 0) {
				str = "You have a special offer from this store!";
			}
			else {
				str = "Have a look at offers from other stores!";
			}
			b.setContentTitle("Hello!").setContentText(str).setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm_resized);

			Intent outbound=new Intent(ctxt, CoreActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
		
	}
	private String getCurrentTime() {
		Calendar cal = Calendar.getInstance();		
		return sdf.format(cal.getTime());
	}
	private void getTimeDiff( String entry, String exit) {
		
		try {
			date_entry = sdf.parse(entry);
			date_exit = sdf.parse(exit);
			long diff = date_exit.getTime() - date_entry.getTime();
			diffMin = diff/(60*1000)%60;
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
