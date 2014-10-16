package com.android.ibabairetail.proto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class GeofenceRequester implements OnAddGeofencesResultListener, ConnectionCallbacks, OnConnectionFailedListener {
	private Context ctxt;
	private static PendingIntent gfPendingIntent;
	private ArrayList<Geofence> current_geofences;
	private LocationClient location_client;
	private boolean in_progress;	
	
	public GeofenceRequester(Context context) {
		ctxt = context;
		gfPendingIntent = null;
		location_client = null;
		in_progress = false;
	}
	public void setInProgressFlag(boolean flag) {
		in_progress = flag;
	}
	public boolean getInProgressFlag() {
		return in_progress;
	}
	public PendingIntent getRequestPendingIntent() {
		return createRequestPendingIntent();
	}
	public void addGeofences(List<Geofence> geofences) throws UnsupportedOperationException {
		current_geofences = (ArrayList<Geofence>) geofences;
		if (!in_progress) {
			in_progress = true;
			requestConnection();
		}
		else {
			throw new UnsupportedOperationException(); 
		}
	}
	private void requestConnection() {
		getLocationClient().connect();
	}
	private GooglePlayServicesClient getLocationClient() {
		if (location_client == null) {
			location_client = new LocationClient(ctxt, this, this);
		}
		return location_client;
	}
	private void continueAddGeofences() {
		gfPendingIntent = createRequestPendingIntent();
		location_client.addGeofences(current_geofences, gfPendingIntent, this);
	}
	@Override
	public void onAddGeofencesResult(int StatusCode, String[] geofenceRequestIds) {
		Intent broadcastIntent = new Intent();
		String msg;
		if (LocationStatusCodes.SUCCESS == StatusCode) {
			msg = ctxt.getString(R.string.add_geofences_result_succes, Arrays.toString(geofenceRequestIds));
			Log.d(GeofenceUtils.APPTAG, msg);
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_ADDED).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
		}
		else {
			msg = ctxt.getString(R.string.add_geofences_result_failure, StatusCode, Arrays.toString(geofenceRequestIds));
			Log.e(GeofenceUtils.APPTAG, msg);
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE, "ADD").putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
		}
		LocalBroadcastManager.getInstance(ctxt).sendBroadcast(broadcastIntent);
		requestDisconnection();
	}
	private void requestDisconnection() {
		in_progress = false;
		getLocationClient().disconnect();
	}
	@Override
	public void onConnected(Bundle b) {
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.connected));
		continueAddGeofences();
	}
	@Override
	public void onDisconnected() {
		in_progress = false;
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.disconnected));
		location_client = null;
	}
	private PendingIntent createRequestPendingIntent() {
		if (null != gfPendingIntent) {
			return gfPendingIntent;
		}
		else {
			/* 
			 * Intent i = new Intent(GeofenceUtils.ACTION_RECEIVE_GEOFENCE);
			 */
			Intent i = new Intent(ctxt, ReceiveTransitionsIntentService.class);
			return PendingIntent.getService(ctxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult connection_result) {
		Intent errorBroadcastIntent = new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
		errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connection_result.getErrorCode());
		LocalBroadcastManager.getInstance(ctxt).sendBroadcast(errorBroadcastIntent);
		
	}
}
