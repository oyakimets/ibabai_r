package com.android.ibabairetail.proto;

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
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class GeofenceRemover implements ConnectionCallbacks, OnConnectionFailedListener, OnRemoveGeofencesResultListener {
	private Context ctxt;
	private List<String> current_geoids;
	private LocationClient location_client;
	private PendingIntent current_intent;
	private GeofenceUtils.REMOVE_TYPE request_type;
	private boolean in_progress;
	
	public GeofenceRemover(Context context) {
		ctxt = context;
		current_geoids = null;
		location_client = null;
		in_progress = false;		
	}
	
	public void setInProgressFlag(boolean flag) {
		in_progress = flag;
	}
	
	public boolean getInProgressFlag() {
		return in_progress;
	}
	public void removeGeofencesById(List<String> geofenceIds) throws IllegalArgumentException, UnsupportedOperationException {
		if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
			throw new IllegalArgumentException();
		}
		else {
			if (!in_progress) {
				request_type = GeofenceUtils.REMOVE_TYPE.LIST;
				current_geoids = geofenceIds;
				requestConnection();
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
	}
	public void removeGeofencesByIntent(PendingIntent request_intent) {
		if (!in_progress) {
			request_type = GeofenceUtils.REMOVE_TYPE.INTENT;
			current_intent = request_intent;
			requestConnection();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	private void continueRemoveGeofences() {
		switch (request_type) {
			case INTENT:
				location_client.removeGeofences(current_intent, this);
				break;
				
			case LIST:
				location_client.removeGeofences(current_geoids, this);
				break;
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
	@Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
		Intent broadcastIntent = new Intent();
		if (statusCode == LocationStatusCodes.SUCCESS) {
			           
            Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.remove_geofences_intent_success));
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
            broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, ctxt.getString(R.string.remove_geofences_intent_success));
                    
        } else {
        	String msg = ctxt.getString(R.string.remove_geofences_intent_failure, statusCode);            
            Log.e(GeofenceUtils.APPTAG, msg);
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
            broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE, "REMOVE").putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
        }

        LocalBroadcastManager.getInstance(ctxt).sendBroadcast(broadcastIntent);

        requestDisconnection();
	}
	@Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {

        Intent broadcastIntent = new Intent();

        String msg;

        if (LocationStatusCodes.SUCCESS == statusCode) {
        	msg = ctxt.getString(R.string.remove_geofences_id_success, Arrays.toString(geofenceRequestIds));

            Log.d(GeofenceUtils.APPTAG, msg);

            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);

        } else {
            msg = ctxt.getString(R.string.remove_geofences_id_failure, statusCode, Arrays.toString(geofenceRequestIds));

            Log.e(GeofenceUtils.APPTAG, msg);

            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
        }

        LocalBroadcastManager.getInstance(ctxt).sendBroadcast(broadcastIntent);

        requestDisconnection();
    }
	private void requestDisconnection() {

        in_progress = false;

        getLocationClient().disconnect();
        
        if (request_type == GeofenceUtils.REMOVE_TYPE.INTENT) {
            current_intent.cancel();
        }

    }
	@Override
    public void onConnected(Bundle arg0) {
        
        Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.connected));
        continueRemoveGeofences();
    }
	@Override
    public void onDisconnected() {

        in_progress = false;

        Log.d(GeofenceUtils.APPTAG, ctxt.getString(R.string.disconnected));

        location_client = null;
    }
	@Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        in_progress = false;

        Intent errorBroadcastIntent = new Intent(GeofenceUtils.ACTION_CONNECTION_ERROR);
        errorBroadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES).putExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE, connectionResult.getErrorCode());
        LocalBroadcastManager.getInstance(ctxt).sendBroadcast(errorBroadcastIntent);
        
    }
}
