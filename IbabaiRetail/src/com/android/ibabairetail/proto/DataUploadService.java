package com.android.ibabairetail.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
	private static final String STORE_BASE_URL = "http://ibabai.picrunner.net/retail/city_stores/";
	private static final String PROMO_NEW_USER_URL = "http://ibabai.picrunner.net/retail/promo_users/0.txt";
	private static final String SP_BASE_URL = "http://ibabai.picrunner.net/retail/promo_stores/";
	private static final String CITIES_URL = "http://ibabai.picrunner.net/retail/cities.txt";
	private ArrayList<Integer> city_pa;	
	private JSONArray promoacts = null;
	private StringBuilder buf;	
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
			
			if (CitiesAvailability()) {
				dbh.ClearCities();
			}
			try {
				URL c_url=new URL(CITIES_URL);
				HttpURLConnection con=(HttpURLConnection)c_url.openConnection();
				con.setRequestMethod("GET");
				con.setReadTimeout(15000);
				con.connect();
							
				reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
				buf = new StringBuilder();
				String line = null;
							
				while ((line=reader.readLine()) != null) {
					buf.append(line+"\n");
				}							
				loadCities(buf.toString());							
				}
			catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Exception retrieving Cities data", e);
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException e) {
						Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);						
					}
				}			 			
			}			
			
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
				
			if (StoresAvailability()) {
				dbh.ClearStores();
			}
			if (c_id != 0) {
				
				String STORES_URL = STORE_BASE_URL + Integer.toString(c_id) +".txt";
				try {
					URL s_url=new URL(STORES_URL);
					HttpURLConnection con=(HttpURLConnection)s_url.openConnection();
					con.setRequestMethod("GET");
					con.setReadTimeout(15000);
					con.connect();
							
					reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
					buf = new StringBuilder();
					String line = null;
							
					while ((line=reader.readLine()) != null) {
						buf.append(line+"\n");
					}							
					loadStores(buf.toString(), city_id);							
				}
				catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Exception retrieving store data", e);
				}
				finally {
					if (reader != null) {
						try {
							reader.close();
						}
						catch (IOException e) {
							Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);
						}
					}
				}			
			
				if (psAvailability()) {
					dbh.ClearPromoStores();
				}			
						
				String SP_URL= SP_BASE_URL + Integer.toString(c_id) +".txt";
				try {
					URL sp_url=new URL(SP_URL);
					HttpURLConnection con=(HttpURLConnection)sp_url.openConnection();
					con.setRequestMethod("GET");
					con.setReadTimeout(15000);
					con.connect();
				
					reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
					StringBuilder buf = new StringBuilder();
					String line = null;
				
					while ((line=reader.readLine()) != null) {
						buf.append(line+"\n");
					}
					loadPromoStores(buf.toString());
				}
				catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Exception retrieving promo_store data", e);
				}
				finally {
					if (reader != null) {
						try {
							reader.close();
						}
						catch (IOException e) {
							Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);
						}
					}
				}			
				if (promosAvailability()) {
					dbh.ClearPromos();
				}
			
				try {
					URL p_url=new URL(PROMO_NEW_USER_URL);
					HttpURLConnection con=(HttpURLConnection)p_url.openConnection();
					con.setRequestMethod("GET");
					con.setReadTimeout(15000);
					con.connect();
					
					reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
					buf = new StringBuilder();
					String line = null;
					
					while ((line=reader.readLine()) != null) {
						buf.append(line+"\n");
					}
					loadPromos(buf.toString());					
				}
				catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Exception retrieving promo data", e);
				}
				finally {
					if (reader != null) {
						try {
							reader.close();
						}
						catch (IOException e) {
							Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);
						}
					}
				}		
				    
				Intent content_i = new Intent(this, ConUpdateService.class);		
				startService(content_i);			
			}
			else {
				stopSelf();
			}
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
			for (int i=0; i<jsa.length(); i++) {
				JSONObject c_jso = jsa.optJSONObject(i);
				City c = new City(c_jso);
				dbh.AddCity(c);					
			}			
		}		
	}	
	 
	 private void loadStores(String st, int id) throws JSONException {
		JSONObject jso = new JSONObject(st);
		int c_id = jso.optInt("city_id");
		if (id == c_id) {
			JSONArray stores = jso.optJSONArray("stores");
			if (stores.length()>0) {
				for (int i=0; i<stores.length(); i++) {
					JSONObject store = stores.optJSONObject(i);
					Store s = new Store(store);
					dbh.AddStore(s);
				}
			}			
		}		
	}	
	 private void loadPromos(String str) throws JSONException {
		JSONObject jso = new JSONObject(str);
		promoacts = jso.optJSONArray("promos");
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
		JSONArray jsa = new JSONArray(st);
		for (int i=0; i<jsa.length(); i++) {
			JSONObject store_item = jsa.optJSONObject(i);
			int store_id = store_item.optInt("store_id");
			JSONArray promo_items = store_item.optJSONArray("promo_ids");
			for (int j=0; j<promo_items.length(); j++) {
				int promoact_id = promo_items.optInt(j);
				dbh.addPromoStores(store_id, promoact_id);
				if (!city_pa.contains(promoact_id)) {
					city_pa.add(promoact_id);
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
}
