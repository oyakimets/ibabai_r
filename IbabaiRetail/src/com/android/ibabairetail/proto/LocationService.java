package com.android.ibabairetail.proto;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

	private LocationManager locationManager;	
	private static final long POINT_RADIUS = 100;
	private static final long PROX_ALERT_EXPIRATION = -1;	
	private static final String PROX_ALERT_INTENT = "com.ibabai.android.proto.ProximityAlert";	
	private Cursor s_cursor;
	private BroadcastReceiver b_rec;
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	private int n = 0;
	@Override
	public void onCreate() {
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		int c_id = shared_prefs.getInt(IbabaiUtils.CITY, 0);
		if (c_id != 0) {
			dbh=DatabaseHelper.getInstance(getApplicationContext());
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);			
			s_cursor=StoresCursor();
			b_rec = new ProximityIntentReceiver();
			if (s_cursor !=null && s_cursor.moveToFirst()) {
				while (s_cursor.isAfterLast()!=true) {
					int id_ind = s_cursor.getColumnIndex("store_id");
					int lat_ind = s_cursor.getColumnIndex("latitude");
					int lon_ind = s_cursor.getColumnIndex("longitude");
					int st_id = s_cursor.getInt(id_ind);
					double target_lat = s_cursor.getDouble(lat_ind);
					double target_lon = s_cursor.getDouble(lon_ind);				
					String proxy = PROX_ALERT_INTENT+n;
					IntentFilter filter = new IntentFilter(proxy);
					registerReceiver(b_rec, filter);
					Intent intent = new Intent(proxy);
					intent.putExtra("st_id", st_id);
					PendingIntent proxIntent = PendingIntent.getBroadcast(this, n, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					locationManager.addProximityAlert(target_lat, target_lon, POINT_RADIUS, PROX_ALERT_EXPIRATION, proxIntent);
					n++;
					s_cursor.moveToNext();
				}
				s_cursor.close();
				dbh.close();
			}
		}
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i("LocalService", "Receive start id "+startId+": "+intent);
		return(START_STICKY);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class myLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			
		}
		public void onStatusChanged(String s, int i, Bundle b) {
			
		}
		public void onProviderDisabled(String s) {
			
		}
		public void onProviderEnabled(String s) {
			
		}
	}
	private Cursor StoresCursor() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	@Override
	public void onDestroy() {
		try {
			Log.i("LocService", "Service stopped");
			unregisterReceiver(b_rec);
		}
		catch(IllegalArgumentException e) {
			Log.d("receiver", e.toString());
		}
		super.onDestroy();
	}
}
