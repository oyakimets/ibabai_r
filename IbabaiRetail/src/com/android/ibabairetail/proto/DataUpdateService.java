package com.android.ibabairetail.proto;

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class DataUpdateService extends com.commonsware.cwac.wakeful.WakefulIntentService {	
	private ArrayList<Integer> current_pa;
	private ArrayList<Integer> update_pa;
	private ArrayList<Integer> city_pa;
	private JSONArray promoacts = null;	
	private String api_key;
	private int city_id;
	private int c_id;	
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	Location current_loc;
	private int counter;
	
	
	public DataUpdateService() {
		super("StoresUpdateService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		
		if (isNetworkAvailable(this)) {
			
			Intent ls_i = new Intent(this, LocationService.class);
			stopService(ls_i);
			
			Intent ar_i = new Intent(this, ARService.class);
			stopService(ar_i);			
		
			dbh=DatabaseHelper.getInstance(getApplicationContext());
			shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);			
			api_key=shared_prefs.getString(IbabaiUtils.API_KEY, "");
			Editor ed = shared_prefs.edit();
			ed.putInt(IbabaiUtils.CITY, 0);
			ed.apply();
			
			UpdateCities(IbabaiUtils.CITIES_UPDATE_URL);			
			
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
				
				UpdateStores(IbabaiUtils.STORE_UPDATE_URL);
				
			}
			if (psAvailability()) {
				UpdatePromos(IbabaiUtils.PROMO_UPDATE_URL);
			}				
			
			WakefulIntentService.sendWakefulWork(this, ConUpdateService.class);		
			ResetStore();
			if (servicesConnected()) {
				
				Intent ar_intent = new Intent(this, ARService.class);
				startService(ar_intent);
			}
			else {
				Intent start_i = new Intent(this, LocationService.class);
				startService(start_i);
			}
		}
		else {
			stopSelf();
		}
	}
	private boolean CitiesAvailability() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
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
	
	 private Cursor cityCursor() {
		 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
		 return(dbh.getReadableDatabase().rawQuery(c_query, null));
	 }
	 private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	 private Cursor psCursor(String pa_id){
		 String sp_query = String.format("SELECT * FROM %s WHERE promoact_id="+pa_id, DatabaseHelper.TABLE_SP);
		 return(dbh.getReadableDatabase().rawQuery(sp_query, null));
	 }
	 private boolean CheckPromosInPs(int pa_id) {
		 String promo_id = Integer.toString(pa_id);
		 Cursor ps_cursor = psCursor(promo_id);
		 if (ps_cursor != null && ps_cursor.getCount() >0) {
			 return true;
		 }
		 else {
			 return false;
		 }
	 }	 
	 
	 private void loadPromos(String str) throws JSONException {
		current_pa = CurrentPromos();
		update_pa = updatePromos(str);
		counter = 0;
		promoacts = new JSONArray(str);
		if (promoacts.length() > 0) {
			for (int i=0; i<update_pa.size(); i++) {
				if (!current_pa.contains(update_pa.get(i)) && CheckPromosInPs(update_pa.get(i))) {
					JSONObject promoact = promoacts.optJSONObject(i);
					Promoact p = new Promoact(promoact);
					dbh.AddPromo(p);
					counter++;
				}
			}
		}
		Editor counter_e = shared_prefs.edit();
		if (counter > 0) {
			counter_e.putInt(IbabaiUtils.PA_UPDATE, 1);
		}
		else {
			counter_e.putInt(IbabaiUtils.PA_UPDATE, 0);
		}
		counter_e.apply();
	 }
	 private void killPromos(String str) throws JSONException {
		 current_pa = CurrentPromos();
		 update_pa = updatePromos(str);		
		 if (current_pa.size() > 0) {
			 for (int i=0; i<current_pa.size(); i++) {
				 if (!update_pa.contains(current_pa.get(i))) {
					dbh.deletePromo(current_pa.get(i));
				 }
			 }
		 }		 
	 }
	 private ArrayList<Integer> updatePromos(String st) throws JSONException {
		ArrayList<Integer> up_lst = new ArrayList<Integer>();
		promoacts = new JSONArray(st);		
		if (promoacts.length() > 0) {
			for (int i=0; i<promoacts.length(); i++) {
				JSONObject promoact = promoacts.optJSONObject(i);
				int pa_id = promoact.getInt("promoact_id");
				up_lst.add(pa_id);				
			}			
		}
		return up_lst;
	}
	 private ArrayList<Integer> CurrentPromos() {
		 ArrayList<Integer> cur_lst = new ArrayList<Integer>();
		 Cursor pa_c = promoactCursor();
		 if (pa_c != null && pa_c.moveToFirst()) {
			 int id_ind = pa_c.getColumnIndex("promoact_id");
			 while (!pa_c.isAfterLast()) {
				 int pa_id = pa_c.getInt(id_ind);
				 cur_lst.add(pa_id);
				 pa_c.moveToNext();
			 }
			 pa_c.close();
		 }
		 return cur_lst; 
	 }	
	
	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d(GeofenceUtils.APPTAG, "Google Play Service is available");
			return true;
		}
		else {
			Log.d(GeofenceUtils.APPTAG, "Google Play Service is not available");
			return false;
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
	private void ResetStore() {
		Editor editor = shared_prefs.edit();
		editor.putInt(IbabaiUtils.STORE_ID, 0);
		editor.remove(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE);
		editor.apply();
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
	private void UpdateCities(String url) {
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
				get_cities.setHeader("Authorization", "Token token="+api_key);
					
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
	
	private void UpdateStores(String url) {
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
				holder.put("store_update", stores_json);
				StringEntity se = new StringEntity(holder.toString());
				post_stores.setEntity(se);
				
				post_stores.setHeader("Accept", "application/json");
				post_stores.addHeader("Content-Type", "application/json");				
				post_stores.setHeader("Authorization", "Token token="+api_key);
				
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
	
	private void UpdatePromos(String url) {
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
				holder.put("promo_update", promos_json);
				StringEntity se = new StringEntity(holder.toString());
				post_promos.setEntity(se);
				
				post_promos.setHeader("Accept", "application/json");
				post_promos.addHeader("Content-Type", "application/json");
				post_promos.setHeader("Authorization", "Token token="+api_key);
					
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
					loadPromos(json.getJSONObject("data").getString("promos"));
					killPromos(json.getJSONObject("data").getString("promos"));
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
