package com.android.ibabairetail.proto;

import android.support.v4.app.FragmentActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.savagelook.android.UrlJsonAsyncTask;


public class SignupActivity extends FragmentActivity {	
	private DbLoadTask db_load = null;	
	Location current_loc;
	private String s_email;	
	private String s_phone;	
	DatabaseHelper dbh;	
	SharedPreferences shared_prefs;		
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_signup);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);       
        
        shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
 		Editor editor=shared_prefs.edit();
 		editor.putInt(IbabaiUtils.CITY, 0);
 		editor.putInt(IbabaiUtils.STORE_ID, 0);
 		editor.apply();
 		
 		if (!isNetworkAvailable(this)) {
 			NetworkDialogFragment ndf = new NetworkDialogFragment();
        	ndf.show(getSupportFragmentManager(), "network");
 		}
        
        GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }
        current_loc = gps.getLocation();             
        dbh = DatabaseHelper.getInstance(getApplicationContext());
        db_load=new DbLoadTask();
        db_load.execute();
        
	}
	
	public void AccountCreate(View view) {
		EditText email=(EditText)findViewById(R.id.email);
		EditText phone=(EditText)findViewById(R.id.phone);		
		s_email = email.getText().toString();
		s_phone = phone.getText().toString();			 
						
		if ( s_email.length() >= 5 && s_phone.length() == 10) {
			Intent sign_intent = new Intent(this, CoreActivity.class);
			startActivity(sign_intent);
			/*
			RegisterTask register = new RegisterTask(this);
			register.setMessageLoading("Creating account...");
			register.execute(IbabaiUtils.REGISTER_API_ENDPOINT_URL);
			*/
		}
		else {
			if (s_email.length() < 5) {
				email.setError("E-mail field error!");
			}
			if (s_phone.length() != 10) {
				phone.setError("Phone field error!");
			}			
		}
		
	}
	
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class DbLoadTask extends AsyncTask<ContentValues, Void, Void> {		
		 
		 @Override
		 protected Void doInBackground(ContentValues... cv) {		 
			 
			 try {
				StringBuilder buf=new StringBuilder();
				InputStream json=getAssets().open("data/cities.json"); 	
				BufferedReader in = new BufferedReader(new InputStreamReader(json));
				String str;
				while((str=in.readLine()) != null ) {
					buf.append(str);
				}				
				in.close();
				
				JSONArray ja=new JSONArray(buf.toString());				
				for(int i=0; i<ja.length(); i++) {					
					JSONObject c_jo = ja.optJSONObject(i);
					City c = new City(c_jo);
					dbh.AddCity(c);						 
				}	
								 
			}				 
			catch(Exception e) {
				e.printStackTrace();
			}	
			 
			 return null;
		 }		 
		 
		 @Override 
		 public void onPostExecute(Void result) {
			 super.onPostExecute(result);			 
			 
			 Cursor new_city_c = cityCursor();
			 int city_id_ind=new_city_c.getColumnIndex(DatabaseHelper.C_ID);
		 	 int lat_ind=new_city_c.getColumnIndex(DatabaseHelper.LAT);
		 	 int lon_ind=new_city_c.getColumnIndex(DatabaseHelper.LON);
		 	 int rad_ind=new_city_c.getColumnIndex(DatabaseHelper.RAD);
			 if (new_city_c != null) {
				 new_city_c.moveToFirst();
				 while(new_city_c.isAfterLast()!=true) {					 
				 	 int city_id=new_city_c.getInt(city_id_ind);
				 	 double latitude=new_city_c.getDouble(lat_ind);
				 	 double longitude=new_city_c.getDouble(lon_ind);
				 	 int radius=new_city_c.getInt(rad_ind);
				 	 Location location = new Location("city");
				 	 location.setLatitude(latitude);
				 	 location.setLongitude(longitude);
				 	 float distance=current_loc.distanceTo(location);
				 	 if (distance <= radius) {
				 		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
				  		Editor edit=shared_prefs.edit();
				 		edit.putInt(IbabaiUtils.CITY, city_id);
				 		edit.apply();
				 		break;
				 	 }
				 	 new_city_c.moveToNext();
			 	}
			 }
			 startService(new Intent(SignupActivity.this, StoresUploadService.class));
			 new_city_c.close();
			 dbh.close();			 
		     return;  
		 }
		 private Cursor cityCursor() {
			 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
			 return(dbh.getReadableDatabase().rawQuery(c_query, null));
		 }
	 }
	private class RegisterTask extends UrlJsonAsyncTask {
		public RegisterTask(Context ctxt) {
			super(ctxt);
		}
		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject cust_json = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");
					
					cust_json.put(IbabaiUtils.EMAIL, s_email);
					cust_json.put(IbabaiUtils.PHONE, s_phone);			
					
					holder.put("customer", cust_json);
					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);
					
					post.setHeader("Accept", "application/json");
					post.addHeader("Content-Type", "application/json");
					
					ResponseHandler<String> r_handler = new BasicResponseHandler();
					response = client.execute(post, r_handler);
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
			return json;
		}
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
					Editor e = shared_prefs.edit();
					e.putString(IbabaiUtils.AUTH_TOKEN, json.getJSONObject("data").getString("auth_token"));
					e.putString(IbabaiUtils.USER_ID, Integer.toString(json.getJSONObject("data").getJSONObject("customer").getInt("id")));
					e.putString(IbabaiUtils.EMAIL, json.getJSONObject("data").getJSONObject("customer").getString("email"));
					e.putString(IbabaiUtils.PHONE, json.getJSONObject("data").getJSONObject("customer").getString("phone"));
					e.apply();
					
					Intent i=new Intent(getApplicationContext(), CoreActivity.class);
					startActivity(i);
					finish();
				}
				Toast.makeText(SignupActivity.this, json.getString("info"), Toast.LENGTH_LONG).show();
			}
			catch(Exception e) {
				Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
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
