package com.android.ibabairetail.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DataUploadService extends IntentService {	
	private ArrayList<Integer> city_pa;	
	private JSONArray promoacts = null;	
	private int city_id;
	private int c_id;	
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	Location current_loc;	
	
	public DataUploadService() {
		super("DataUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (isNetworkAvailable(this)) {
			
			dbh=DatabaseHelper.getInstance(getApplicationContext());
			shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
			
			UploadCities(IbabaiUtils.CITIES_URL);
			
			GPSTracker gps = new GPSTracker(this);
			current_loc = gps.getLocation();					    
		
			Cursor new_city_c = cityCursor();
			int city_id_ind=new_city_c.getColumnIndex(DatabaseHelper.C_ID);
			int lat_ind=new_city_c.getColumnIndex(DatabaseHelper.LAT);
			int lon_ind=new_city_c.getColumnIndex(DatabaseHelper.LON);
			int rad_ind=new_city_c.getColumnIndex(DatabaseHelper.RAD);
			if (new_city_c != null && new_city_c.moveToFirst()) {
				while(new_city_c.isAfterLast()!=true) {					 
					city_id=new_city_c.getInt(city_id_ind);
					double latitude=new_city_c.getDouble(lat_ind);
					double longitude=new_city_c.getDouble(lon_ind);
					int radius=new_city_c.getInt(rad_ind);
					Location location = new Location("city");
					location.setLatitude(latitude);
					location.setLongitude(longitude);
					float distance=current_loc.distanceTo(location);
					if (distance <= radius) {
						Editor edit=shared_prefs.edit();
						edit.putInt(IbabaiUtils.CITY, city_id);
						edit.apply();
						break;
					}
					new_city_c.moveToNext();
				}
			}
			
			c_id=shared_prefs.getInt(IbabaiUtils.CITY, 0);
			if (c_id != 0) {
				UploadStores(IbabaiUtils.STORE_BASE_URL);
			}
			if (psAvailability()) {
				UploadPromos(IbabaiUtils.PROMO_NEW_USER_URL);
			}
			Intent content_i = new Intent(this, ConUpdateService.class);		
			startService(content_i);
			 
		}
		else {
			stopSelf();
		}
	}	
	
	private boolean CitiesAvailability() {		
		String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
		Cursor c = dbh.getWritableDatabase().rawQuery(c_query, null);
		if (c.getCount() != 0) {
			c.close();
			return (true);
		}
		else {
			c.close();
			return (false);
		}
	}

	private boolean StoresAvailability() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		Cursor c = dbh.getWritableDatabase().rawQuery(s_query, null);
		if (c.getCount() != 0) {
			c.close();
			return (true);
		}
		else {
			c.close();
			return (false);
		}
	}
	private boolean psAvailability() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_SP);
		Cursor c = dbh.getWritableDatabase().rawQuery(s_query, null);
		if (c.getCount() != 0) {
			c.close();
			return (true);
		}
		else {
			c.close();
			return (false);
		}
	}
	private boolean promosAvailability() {		
		String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_P);
		Cursor c = dbh.getWritableDatabase().rawQuery(p_query, null);
		if (c.getCount() != 0) {
			c.close();
			return (true);
		}
		else {
			c.close();
			return (false);
		}
	}
	
	 private Cursor cityCursor() {
		 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
		 return(dbh.getReadableDatabase().rawQuery(c_query, null));
	 }
	 
	 private void loadCities(String st) throws JSONException {
		JSONArray jsa = new JSONArray(st);
		if (jsa.length()>0) {
			if (CitiesAvailability()) {
				dbh.ClearCities();
			}			
			for (int i=0; i<jsa.length(); i++) {
				JSONObject c_jso = jsa.optJSONObject(i);
				City c = new City(c_jso);
				dbh.AddCity(c);					
			}			
		}		
	}	
	 
	 private void loadStores(String st) throws JSONException {		
		JSONArray stores = new JSONArray(st);
		if (stores.length()>0) {
			for (int i=0; i<stores.length(); i++) {
				JSONObject store = stores.optJSONObject(i);
				Store s = new Store(store);
				dbh.AddStore(s);
				
			}			
		}		
	}	
	 private void loadPromos(String str) throws JSONException {
		promoacts = new JSONArray(str);		
		if (promoacts.length() > 0) {
			for (int i=0; i<promoacts.length(); i++) {
				JSONObject promoact = promoacts.optJSONObject(i);
				int pa_id = promoact.getInt("promoact_id");
				if (city_pa.contains(pa_id)) {
					Promoact p = new Promoact(promoact);
					dbh.AddPromo(p);
				}
			}
		}		
	 }	
	
	private void loadPromoStores(String st) throws JSONException {
		city_pa = new ArrayList<Integer>();		
		JSONArray stores = new JSONArray(st);
		if (stores.length()>0) {
			for (int i=0; i<stores.length(); i++) {
				JSONObject store = stores.optJSONObject(i);
				int store_id = store.optInt("store_id");
				JSONArray promo_items = store.optJSONArray("promos"); 
				for (int j=0; j<promo_items.length(); j++) {
					int promoact_id = promo_items.optInt(j);
					dbh.addPromoStores(store_id, promoact_id);
					if (!city_pa.contains(promoact_id)) {
						city_pa.add(promoact_id);
					}
				}				
			}			
		}		
	}	
	
	public static boolean isNetworkAvailable(Context ctxt) {
		boolean outcome = false;
		if (ctxt != null) {
			ConnectivityManager cm = (ConnectivityManager) ctxt.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] network_info = cm.getAllNetworkInfo();
			for (NetworkInfo ni:network_info) {
				if (ni.isConnected()) {
					outcome = true;
					break;
				}
			}
		}
		return outcome;
	}
	private void UploadCities(String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get_cities = new HttpGet(url);		
		String response = null;
		JSONObject json = new JSONObject();
		try {
			try {
				json.put("success", false);
				json.put("info", "Error. Try again!");			
					
				get_cities.setHeader("Accept", "application/json");
				get_cities.addHeader("Content-Type", "application/json");				
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(get_cities, r_handler);
				json = new JSONObject(response);					
			}
			catch (HttpResponseException ex) {
				ex.printStackTrace();
				Log.e("ClientProtocol", ""+ex);
			}
			catch (IOException ex) {
				ex.printStackTrace();
				Log.e("IO", ""+ex);
			}
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			Log.e("JSON", ""+ex);
		}
		if (json != null) {
			try {
				if (json.getBoolean("success")) {										
					Log.v("IPI", "Cities delivered");
					loadCities(json.getJSONObject("data").getString("cities"));					
				}
				else {
					Log.e("DELIVERY", json.getString("info"));
				}
			}
			catch(Exception e) {
				Log.e("DELIVERY", e.getMessage());
			}
		}		
	}
	private void UploadStores(String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post_stores = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject stores_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();
		try {
			try {
				json.put("success", false);
				json.put("info", "Error. Try again!");			
				
				stores_json.put(DatabaseHelper.C_ID, c_id);
				holder.put("store_upload", stores_json);
				StringEntity se = new StringEntity(holder.toString());
				post_stores.setEntity(se);
				
				post_stores.setHeader("Accept", "application/json");
				post_stores.addHeader("Content-Type", "application/json");				
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(post_stores, r_handler);
				json = new JSONObject(response);					
			}
			catch (HttpResponseException ex) {
				ex.printStackTrace();
				Log.e("ClientProtocol", ""+ex);
			}
			catch (IOException ex) {
				ex.printStackTrace();
				Log.e("IO", ""+ex);
			}
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			Log.e("JSON", ""+ex);
		}
		if (json != null) {
			try {
				if (json.getBoolean("success")) {										
					Log.v("IPI", "Stores delivered");
					if (StoresAvailability()) {
						dbh.ClearStores();
					}
					loadStores(json.getJSONObject("data").getString("stores"));
					if (psAvailability()) {
						dbh.ClearPromoStores();
					}
					loadPromoStores(json.getJSONObject("data").getString("stores"));
				}
				else {
					Log.e("DELIVERY", json.getString("info"));
				}
			}
			catch(Exception e) {
				Log.e("DELIVERY", e.getMessage());
			}
		}		
	}
	private void UploadPromos(String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post_promos = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject promos_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();
		try {
			try {
				json.put("success", false);
				json.put("info", "Error. Try again!");			
				
				promos_json.put(DatabaseHelper.C_ID, c_id);
				holder.put("promo_upload", promos_json);
				StringEntity se = new StringEntity(holder.toString());
				post_promos.setEntity(se);
				
				post_promos.setHeader("Accept", "application/json");
				post_promos.addHeader("Content-Type", "application/json");				
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(post_promos, r_handler);
				json = new JSONObject(response);					
			}
			catch (HttpResponseException ex) {
				ex.printStackTrace();
				Log.e("ClientProtocol", ""+ex);
			}
			catch (IOException ex) {
				ex.printStackTrace();
				Log.e("IO", ""+ex);
			}
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			Log.e("JSON", ""+ex);
		}
		if (json != null) {
			try {
				if (json.getBoolean("success")) {										
					Log.v("IPI", "Promos delivered");
					if (promosAvailability()) {
						dbh.ClearPromos();
					}
					loadPromos(json.getJSONObject("data").getString("promos"));					
				}
				else {
					Log.e("DELIVERY", json.getString("info"));
				}
			}
			catch(Exception e) {
				Log.e("DELIVERY", e.getMessage());
			}
		}		
	}
}
