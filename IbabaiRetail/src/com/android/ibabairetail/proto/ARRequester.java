package com.android.ibabairetail.proto;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;

public class ARRequester implements ConnectionCallbacks, OnConnectionFailedListener {
	private Context ctxt;
	private PendingIntent ar_pending_intent;
	private ActivityRecognitionClient ar_client;
	
	public ARRequester(Context context) {
		ctxt = context;
		ar_pending_intent = null;
		ar_client = null;
	}
	public PendingIntent getRequestPendingIntent() {
		return ar_pending_intent;
	}
	public void setRequestPendingIntent(PendingIntent intent) {
		ar_pending_intent = intent;
	}
	public void requestUpdates() {
		requestConnection();
	}
	public void continueRequestActivityUpdates() {
		getActivityRecognitionClient().requestActivityUpdates(GeofenceUtils.DETECTION_INTERVAL_MILLISECONDS, createRequestPendingIntent());
		requestDisconnection();
	}
	private void requestConnection() {
		getActivityRecognitionClient().connect();
	}
	private ActivityRecognitionClient getActivityRecognitionClient() {
		if (ar_client == null) {
			ar_client = new ActivityRecognitionClient(ctxt, this, this);
		}
		return ar_client;
	}
	private void requestDisconnection() {
		getActivityRecognitionClient().disconnect();
	}
	@Override
	public void onConnected(Bundle arg) {
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.ar_connected));
		continueRequestActivityUpdates();
	}
	@Override
	public void onDisconnected() {
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.ar_disconnected));
		ar_client = null;
	}
	private PendingIntent createRequestPendingIntent() {
		if (null != getRequestPendingIntent()) {
			return ar_pending_intent;
		}
		else {
			Intent i = new Intent(ctxt, ARIntentService.class);
			PendingIntent pending_intent = PendingIntent.getService(ctxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			setRequestPendingIntent(pending_intent);
			return pending_intent;
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult connection_result) {
		Intent errorBroadcastIntent = new Intent(GeofenceUtils.AR_CONNECTION_ERROR);
		errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connection_result.getErrorCode()).putExtra(GeofenceUtils.EXTRA_CONNECTION_REQUEST_TYPE, "ADD");
		LocalBroadcastManager.getInstance(ctxt).sendBroadcast(errorBroadcastIntent);
	}
}
