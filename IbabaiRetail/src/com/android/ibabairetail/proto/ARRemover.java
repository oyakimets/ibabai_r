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

public class ARRemover implements ConnectionCallbacks, OnConnectionFailedListener {
	private Context ctxt;
	private ActivityRecognitionClient ar_client;
	private PendingIntent current_pi;
	
	public ARRemover(Context context) {
		ctxt = context;
		ar_client = null;
	}
	public void removeUpdates(PendingIntent request_intent) {
		current_pi = request_intent;
		requestConnection();
	}
	public void requestConnection() {
		getActivityRecognitionClient().connect();
	}
	public ActivityRecognitionClient getActivityRecognitionClient() {
		if (ar_client == null) {
			setActivityRecognitionClient(new ActivityRecognitionClient(ctxt, this, this));
		}
		return ar_client;
	}
	private void requestDisconnection() {
		getActivityRecognitionClient().disconnect();
		setActivityRecognitionClient(null);
	}
	public void setActivityRecognitionClient(ActivityRecognitionClient client) {
		ar_client = client;
	}
	@Override
	public void onConnected(Bundle connectionData) {
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.ar_connected));
		continueRemoveUpdates();
	}
	private void continueRemoveUpdates() {
		ar_client.removeActivityUpdates(current_pi);
		current_pi.cancel();
		requestDisconnection();
	}
	@Override
	public void onDisconnected() {
		Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.ar_disconnected));
		ar_client = null;
	}
	@Override
	public void onConnectionFailed(ConnectionResult connection_result) {
		Intent errorBroadcastIntent = new Intent(GeofenceUtils.AR_CONNECTION_ERROR);
		errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connection_result.getErrorCode()).putExtra(GeofenceUtils.EXTRA_CONNECTION_REQUEST_TYPE, "REMOVE");
		LocalBroadcastManager.getInstance(ctxt).sendBroadcast(errorBroadcastIntent);
	}

}
