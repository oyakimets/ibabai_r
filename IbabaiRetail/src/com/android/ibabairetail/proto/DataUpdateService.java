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
	private static final String STORE_BASE_URL = "http://ibabai.picrunner.net/retail/city_stores/";
	private static final String PROMO_BASE_URL = "http://ibabai.picrunner.net/retail/promo_users/";
	private static final String SP_BASE_URL = "http://ibabai.picrunner.net/retail/promo_stores/";
	private static final String CITIES_URL = "http://ibabai.picrunner.net/retail/cities.txt";
	private ArrayList<Integer> current_pa;
	private ArrayList<Integer> update_pa;
	private JSONArray promoacts = null;
	private StringBuilder buf;
	private String PROMO_URL;
	private String u_id;
	private int city_id;
	private int c_id;	
	BufferedReader reader=null;
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
			u_id=shared_prefs.getString(IbabaiUtils.USER_ID, null);
			Editor ed = shared_prefs.edit();
			ed.putInt(IbabaiUtils.CITY, 0);
			ed.apply();
			
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
				if (!buf.toString().isEmpty()) {
					if (CitiesAvailability()) {
						dbh.ClearCities();
					}
					loadCities(buf.toString());	
				}											
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
					if (!buf.toString().isEmpty()) {
						if (StoresAvailability()) {
							dbh.ClearStores();
						}
						loadStores(buf.toString(), city_id);
					}
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
			
			
				if (StoresAvailability()) {
						
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
						if (!buf.toString().isEmpty()) {
							if (psAvailability()) {
								dbh.ClearPromoStores();
							}			
							loadPromoStores(buf.toString());
						}
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
				}
				
				if (psAvailability()) {
		
					PROMO_URL = PROMO_BASE_URL+u_id+".txt";
					try {
						URL p_url=new URL(PROMO_URL);
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
						if (!buf.toString().isEmpty()) {
							loadPromos(buf.toString(), u_id);
							killPromos(buf.toString(), u_id);
						}
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
				}
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
	 private void loadPromos(String str, String us_id ) throws JSONException {
		current_pa = CurrentPromos();
		update_pa = UpdatePromos(str, Integer.parseInt(us_id));
		counter = 0;
		JSONObject jso = new JSONObject(str);
		promoacts = jso.optJSONArray("promos");
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
	 private void killPromos(String str, String us_id) throws JSONException {
		 current_pa = CurrentPromos();
		 update_pa = UpdatePromos(str, Integer.parseInt(us_id));		
		 if (current_pa.size() > 0) {
			 for (int i=0; i<current_pa.size(); i++) {
				 if (!update_pa.contains(current_pa.get(i))) {
					dbh.deletePromo(current_pa.get(i));
				 }
			 }
		 }		 
	 }
	 private ArrayList<Integer> UpdatePromos(String st, int us_id) throws JSONException {
		ArrayList<Integer> up_lst = new ArrayList<Integer>();
		JSONObject jso = new JSONObject(st);
		int u_id = jso.optInt("user_id");
		if (us_id == u_id) {
			promoacts = jso.optJSONArray("promos");
			if (promoacts.length() > 0) {
				for (int i=0; i<promoacts.length(); i++) {
					JSONObject promoact = promoacts.optJSONObject(i);
					int pa_id = promoact.getInt("promoact_id");
					up_lst.add(pa_id);
				}
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
	
	private void loadPromoStores(String st) throws JSONException {
		JSONArray jsa = new JSONArray(st);
		for (int i=0; i<jsa.length(); i++) {
			JSONObject store_item = jsa.optJSONObject(i);
			int store_id = store_item.optInt("store_id");
			JSONArray promo_item = store_item.optJSONArray("promo_ids");
			for (int j=0; j<promo_item.length(); j++) {
				int promoact_id = promo_item.optInt(j);
				dbh.addPromoStores(store_id, promoact_id);
			}
		}
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
			for (int i=0; i<jsa.length(); i++) {
				JSONObject c_jso = jsa.optJSONObject(i);
				City c = new City(c_jso);
				dbh.AddCity(c);					
			}			
		}		
	}	
}
