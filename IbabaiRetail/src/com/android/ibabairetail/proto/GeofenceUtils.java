package com.android.ibabairetail.proto;

public class GeofenceUtils {
	public enum REMOVE_TYPE {INTENT, LIST}    
    public enum REQUEST_TYPE {ADD, REMOVE}
    public static final String APPTAG = "Geofence Detection";
    public static final String ACTION_CONNECTION_ERROR = "com.example.android.geofence.ACTION_CONNECTION_ERROR";
    public static final String ACTION_CONNECTION_SUCCESS = "com.example.android.geofence.ACTION_CONNECTION_SUCCESS";
    public static final String ACTION_GEOFENCES_ADDED = "com.example.android.geofence.ACTION_GEOFENCES_ADDED";    
    public static final String ACTION_GEOFENCES_REMOVED = "com.example.android.geofence.ACTION_GEOFENCES_DELETED";    
    public static final String ACTION_GEOFENCE_ERROR = "com.example.android.geofence.ACTION_GEOFENCES_ERROR";
    public static final String ACTION_GEOFENCE_TRANSITION = "com.example.android.geofence.ACTION_GEOFENCE_TRANSITION";
    public static final String ACTION_GEOFENCE_TRANSITION_ERROR = "com.example.android.geofence.ACTION_GEOFENCE_TRANSITION_ERROR";
    public static final String CATEGORY_LOCATION_SERVICES = "com.example.android.geofence.CATEGORY_LOCATION_SERVICES";
    public static final String EXTRA_CONNECTION_CODE = "com.example.android.EXTRA_CONNECTION_CODE";
    public static final String EXTRA_CONNECTION_ERROR_CODE = "com.example.android.geofence.EXTRA_CONNECTION_ERROR_CODE";
    public static final String EXTRA_CONNECTION_ERROR_MESSAGE = "com.example.android.geofence.EXTRA_CONNECTION_ERROR_MESSAGE";
    public static final String EXTRA_GEOFENCE_STATUS = "com.example.android.geofence.EXTRA_GEOFENCE_STATUS"; 
    public static final String EXTRA_GEOFENCE_TYPE = "com.example.android.geofence.EXTRA_GEOFENCE_TYPE";
    public static final String KEY_LATITUDE = "com.example.android.geofence.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.example.android.geofence.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.example.android.geofence.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "com.example.android.geofence.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "com.example.android.geofence.KEY_TRANSITION_TYPE";
    public static final String ACTION_RECEIVE_GEOFENCE = "com.ibabai.android.proto.ACTION_RECEIVE_GEOFENCE";
    
    public static final String KEY_PREFIX = "com.example.android.geofence.KEY";
        
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    
    public static final String EMPTY_STRING = new String();
    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";
    
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int DETECTION_INTERVAL_MINUTES = 1;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * DETECTION_INTERVAL_MINUTES;
    
    public static final String ACTION_ACTIVITY_RECOGNITION =
            "com.example.android.activityrecognition.ACTION_ACTIVITY_RECOGNITION";
    public static final String KEY_PREVIOUS_ACTIVITY_TYPE =
            "com.example.android.activityrecognition.KEY_PREVIOUS_ACTIVITY_TYPE";
    public static final String AR_CONNECTION_ERROR =
            "com.example.android.activityrecognition.ACTION_CONNECTION_ERROR";
    public static final String EXTRA_CONNECTION_REQUEST_TYPE =
            "com.example.android.activityrecognition.EXTRA_CONNECTION_REQUEST_TYPE";

}
