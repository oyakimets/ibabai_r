package com.android.ibabairetail.proto;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class ARService extends Service {

	IntentFilter broadcast_filter;
	private LocalBroadcastManager broadcast_manager;
	private ARRequester ar_requester;
	private ARRemover ar_remover;
	private ARReceiver ar_receiver;		
	private SharedPreferences shared_prefs;	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		broadcast_manager = LocalBroadcastManager.getInstance(this);
		broadcast_filter = new IntentFilter(GeofenceUtils.ACTION_ACTIVITY_RECOGNITION);		
		broadcast_filter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		broadcast_filter.addAction(GeofenceUtils.AR_CONNECTION_ERROR);
		broadcast_filter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
		broadcast_filter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
		broadcast_filter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
		broadcast_filter.addAction(GeofenceUtils.ACTION_CONNECTION_ERROR);
		ar_receiver = new ARReceiver();
		broadcast_manager.registerReceiver(ar_receiver, broadcast_filter);
		ar_requester = new ARRequester(this);
		ar_remover = new ARRemover(this);		
		ar_requester.requestUpdates();
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i(GeofenceUtils.APPTAG, "Receive start id "+startId+": "+intent);
		return(START_STICKY);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	@Override
	public void onDestroy() {
		try {
			
			ar_remover.removeUpdates(ar_requester.getRequestPendingIntent());
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.remove_ar_already_requested_error));
		}
		broadcast_manager.unregisterReceiver(ar_receiver);		
	}
	public class ARReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctxt, Intent i) {
			String action = i.getAction();			
			Editor editor = shared_prefs.edit();
			
			if (TextUtils.equals(action, GeofenceUtils.AR_CONNECTION_ERROR)) {
				String connection_error = i.getStringExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE);
				Log.e(GeofenceUtils.APPTAG, connection_error);
				String connection_error_type = i.getStringExtra(GeofenceUtils.EXTRA_CONNECTION_REQUEST_TYPE);
				if (connection_error_type.equals("ADD")) {
					Log.e(GeofenceUtils.APPTAG, "AR connection error on ADD");
					Intent ls_intent = new Intent(ctxt, LocationService.class);
					startService(ls_intent);
					stopSelf();
				}
				else {
					Log.e(GeofenceUtils.APPTAG, "AR connection error on REMOVE");
				}
			}
			
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)) {
				Log.d(GeofenceUtils.APPTAG, "Geofences successfully added");
				editor.putString("geofence", "GF ON");
				editor.apply();
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {
				Log.d(GeofenceUtils.APPTAG, "Geofences successfully removed");
				editor.putString("geofence", "GF OFF");
				editor.apply();				
				
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {
				String status = i.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
				String type = i.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE);
				if (TextUtils.equals(type, "ADD")) {
					Log.e(GeofenceUtils.APPTAG, status);
					Intent ls_intent = new Intent(ctxt, LocationService.class);
					ctxt.startService(ls_intent);					
					stopSelf();
				}
				else {
					Log.e(GeofenceUtils.APPTAG, status);
				}
				
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_CONNECTION_ERROR)) {
				String connection_error = i.getStringExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE);
				Log.e(GeofenceUtils.APPTAG, connection_error);
			}
			else {
				Log.e(GeofenceUtils.APPTAG, ctxt.getString(R.string.invalid_action_detail, action));
			}
		}
	}

}
