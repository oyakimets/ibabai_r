package com.android.ibabairetail.proto;

import android.app.IntentService;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class StoresUploadService extends IntentService {
	private static final String STORE_BASE_URL = "http://ibabai.picrunner.net/city_stores/";	
	private int city_id;
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	public StoresUploadService() {
		super("StoresUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		city_id = shared_prefs.getInt(IbabaiUtils.CITY, 0);		
		if (city_id != 0 && StoresEmpty()) {
			String STORES_URL = STORE_BASE_URL + Integer.toString(city_id) +".txt";
			try {
				URL s_url=new URL(STORES_URL);
				HttpURLConnection con=(HttpURLConnection)s_url.openConnection();
				con.setRequestMethod("GET");
				con.setReadTimeout(15000);
				con.connect();
				
				reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder buf = new StringBuilder();
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
		}
		Intent ps_intent = new Intent(this, psUploadService.class);
		startService(ps_intent);
		
	}	
	
	private void loadStores(String st, int id) throws JSONException {
		JSONObject jso = new JSONObject(st);
		int c_id = jso.optInt("city_id");
		if (id == c_id) {
			JSONArray stores = jso.optJSONArray("stores");
			for (int i=0; i<stores.length(); i++) {
				JSONObject store = stores.optJSONObject(i);
				Store s = new Store(store);
				dbh.AddStore(s);
			}
			dbh.close();
		}		
	}
	private boolean StoresEmpty() {
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		 if (dbh.getReadableDatabase().rawQuery(p_query, null).getCount() == 0) {
			 return true;
		 }
		 else {
			 return false;
		 }
	}
}
