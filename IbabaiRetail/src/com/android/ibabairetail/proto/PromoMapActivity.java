package com.android.ibabairetail.proto;

import java.util.ArrayList;
import android.content.Context;
import android.content.SharedPreferences;
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

public class PromoMapActivity extends AbstractMapActivity {
	private GoogleMap map;
	private Location current_loc;
	private double u_lat;
	private double u_lon;	
	DatabaseHelper dbh;
	private ArrayList<Integer> store_lst;
	private LatLngBounds.Builder builder = new LatLngBounds.Builder();
	private SharedPreferences shared_prefs;
	private int category;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {		
	    super.onCreate(savedInstanceState);
	   
	    dbh = DatabaseHelper.getInstance(getApplicationContext());
	    shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
	    if (readyToGo()) { 
	      setContentView(R.layout.ps_map);
	      
	      SupportMapFragment mapFrag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.ps_map);
	      map = mapFrag.getMap();
	      GPSTracker gps = new GPSTracker(this);
    	  current_loc = gps.getLocation();
    	  u_lat = current_loc.getLatitude();
    	  u_lon = current_loc.getLongitude();
	      addMarker(map, u_lat, u_lon, "You are here", null, 240);
	      category = shared_prefs.getInt(DatabaseHelper.CAT, 0);
	      addStoresMarkers(category);	      
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
	private void addStoresMarkers(int cat) {
		store_lst = new ArrayList<Integer>();
		getStoresWithPromo(cat);		
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
	private void getStoresWithPromo(int cat) {
		ArrayList<Integer> pa_lst = new ArrayList<Integer>();
		Cursor pa_cursor = PromoCursor(cat);
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex(DatabaseHelper.P_ID);
			while (pa_cursor.isAfterLast()!=true) {
				int pa_id = pa_cursor.getInt(id_ind);
				pa_lst.add(pa_id);
				pa_cursor.moveToNext();				
			}
			pa_cursor.close();
		}
		for (int i=0; i<pa_lst.size(); i++) {
			String pa_id = Integer.toString(pa_lst.get(i)); 
			getPromoStores(pa_id);			
		}
	}
	private void getPromoStores(String pa_id) {
		Cursor ps_cursor = PSCursor(pa_id);
		if (ps_cursor != null && ps_cursor.moveToFirst()) {
			int st_ind = ps_cursor.getColumnIndex(DatabaseHelper.S_ID);
			while (ps_cursor.isAfterLast()!=true) {
				int s_id = ps_cursor.getInt(st_ind);
				if(!store_lst.contains(s_id)) {
					store_lst.add(s_id);
				}
				ps_cursor.moveToNext();				
			}			
		}
		ps_cursor.close();		
	}
	
	private Cursor PSCursor(String pa_id) {
		String s_query = String.format("SELECT * FROM %s WHERE promoact_id="+pa_id, DatabaseHelper.TABLE_SP);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	private Cursor StoreCursor(String st_id) {
		String s_query = String.format("SELECT * FROM %s WHERE store_id="+st_id, DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	private Cursor PromoCursor(int cat) {
		String s_query = null;
		if (cat == 0) {
			s_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		}
		else {
			String s_cat = Integer.toString(cat);
			s_query = String.format("SELECT * FROM %s WHERE stopped=0 AND category="+s_cat, DatabaseHelper.TABLE_P);
		}
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	
}
