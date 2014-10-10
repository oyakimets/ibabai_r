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
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class psUploadService extends IntentService {

	private static final String SP_BASE_URL = "http://ibabai.picrunner.net/promo_stores/";
	private static final String PA_URL = "http://ibabai.picrunner.net/promo_users/ibabai_promoacts.txt";
	private int city_id;	
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	public psUploadService() {
		super("psUploadService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		city_id = shared_prefs.getInt(IbabaiUtils.CITY, 0);
		
		if (city_id != 0 && TableEmpty(DatabaseHelper.TABLE_SP)) {
			String SP_URL= SP_BASE_URL + Integer.toString(city_id) +".txt";
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
		}
		if (TableEmpty(DatabaseHelper.TABLE_P)) {
			try {
				URL pa_url=new URL(PA_URL);
				HttpURLConnection con=(HttpURLConnection)pa_url.openConnection();
				con.setRequestMethod("GET");
				con.setReadTimeout(15000);
				con.connect();
					
				reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder buf = new StringBuilder();
				String line = null;
					
				while ((line=reader.readLine()) != null) {
					buf.append(line+"\n");
				}
				Log.d("HP", buf.toString());
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
		}		
		
		WakefulIntentService.sendWakefulWork(this, ConUpdateService.class);
				
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
	
	private void loadPromos(String st) throws JSONException {
		JSONObject jso = new JSONObject(st);
		JSONArray promoacts = jso.optJSONArray("promos");		
		if (promoacts.length() > 0) {
			for (int i=0; i<promoacts.length(); i++) {
				JSONObject promoact = promoacts.optJSONObject(i);
				Promoact p = new Promoact(promoact);
				dbh.AddPromo(p);				
			}			
		}
	}	
	
	private boolean TableEmpty(String table) {
		 String p_query = String.format("SELECT * FROM %s", table);
		 if (dbh.getReadableDatabase().rawQuery(p_query, null).getCount() == 0) {
			 return true;
		 }
		 else {
			 return false;
		 }
	}
}
