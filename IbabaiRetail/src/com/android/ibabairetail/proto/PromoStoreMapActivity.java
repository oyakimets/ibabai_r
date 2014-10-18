package com.android.ibabairetail.proto;

import java.util.ArrayList;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PromoStoreMapActivity extends AbstractMapActivity {
	private GoogleMap map;
	private Location current_loc;
	private double u_lat;
	private double u_lon;
	private String pa_id;
	DatabaseHelper dbh;
	private ArrayList<Integer> store_lst;
	private LatLngBounds.Builder builder = new LatLngBounds.Builder();
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {		
	    super.onCreate(savedInstanceState);
	   
	    dbh = DatabaseHelper.getInstance(getApplicationContext());
	    if (readyToGo()) { 
	      setContentView(R.layout.ps_map);
	      
	      SupportMapFragment mapFrag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.ps_map);
	      map = mapFrag.getMap();
	      GPSTracker gps = new GPSTracker(this);
    	  current_loc = gps.getLocation();
    	  u_lat = current_loc.getLatitude();
    	  u_lon = current_loc.getLongitude();
	      addMarker(map, u_lat, u_lon, "You are here", null, 240);
	      pa_id = getIntent().getStringExtra(IbabaiUtils.EXTRA_PA);
	      addPromoStoresMarkers(pa_id);	      
	      if (savedInstanceState == null) {
	    	  findViewById(android.R.id.content).post(new Runnable() {
	    		  @Override
	    		  public void run() {
	    			  CameraUpdate all_stores = CameraUpdateFactory.newLatLngBounds(builder.build(), 50);
	    	    	  map.moveCamera(all_stores);	    	    	  
	    		  }
	    	  });
	    	  
	      }
	      
	    } 
	} 
	
	private void addMarker(GoogleMap map, double lat, double lon, String title, String snippet, int hue) {
		Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(title).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(hue)));
		builder.include(marker.getPosition());
	}
	private void addPromoStoresMarkers(String promo_id) {
		store_lst = getPromoStores(promo_id);
		for (int i=0; i < store_lst.size(); i++) {
			Cursor st_cursor = StoreCursor(Integer.toString(store_lst.get(i)));
			if (st_cursor != null && st_cursor.moveToFirst()) {
				int lat_ind = st_cursor.getColumnIndex("latitude");
				int lon_ind = st_cursor.getColumnIndex("longitude");
				int cl_ind = st_cursor.getColumnIndex("client_name");
				double st_lat = st_cursor.getDouble(lat_ind);
				double st_lon = st_cursor.getDouble(lon_ind);
				String cl_name = st_cursor.getString(cl_ind);
				addMarker(map, st_lat, st_lon, cl_name, null, 0);
			}	
		}		
	}
	private ArrayList<Integer> getPromoStores(String pa_id) {
		ArrayList<Integer> st_lst = new ArrayList<Integer>();
		Cursor ps_cursor = PSCursor(pa_id);
		if (ps_cursor != null && ps_cursor.moveToFirst()) {
			int st_ind = ps_cursor.getColumnIndex(DatabaseHelper.S_ID);
			while (ps_cursor.isAfterLast()!=true) {
				int s_id = ps_cursor.getInt(st_ind);
				st_lst.add(s_id);
				ps_cursor.moveToNext();				
			}
		}
		return st_lst;
	}
	private Cursor PSCursor(String pa_id) {
		String s_query = String.format("SELECT * FROM %s WHERE promoact_id="+pa_id, DatabaseHelper.TABLE_SP);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	private Cursor StoreCursor(String st_id) {
		String s_query = String.format("SELECT * FROM %s WHERE store_id="+st_id, DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	
}
