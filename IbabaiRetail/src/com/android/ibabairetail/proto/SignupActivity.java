package com.android.ibabairetail.proto;

import android.support.v4.app.FragmentActivity;
import java.io.IOException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.savagelook.android.UrlJsonAsyncTask;


public class SignupActivity extends FragmentActivity {	
	Location current_loc;
	private String s_email;	
	private String s_phone;	
	SharedPreferences shared_prefs;		
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		
		ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_signup);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);       
         		
 		if (!isNetworkAvailable(this)) {
 			NetworkDialogFragment ndf = new NetworkDialogFragment();
        	ndf.show(getSupportFragmentManager(), "network");
 		}
 		
        
        GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }     
        
	}
	
	public void AccountCreate(View view) {
		EditText email=(EditText)findViewById(R.id.email);
		EditText phone=(EditText)findViewById(R.id.phone);		
		s_email = email.getText().toString();
		s_phone = phone.getText().toString();			 
						
		if ( s_email.length() >= 5 && s_phone.length() == 10) {			
			
			RegisterTask register = new RegisterTask(this);
			register.setMessageLoading("Creating account...");
			register.execute(IbabaiUtils.REGISTER_API_ENDPOINT_URL);
			
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
					e.putString(IbabaiUtils.API_KEY, json.getJSONObject("data").getString("api_key"));					
					e.apply();
					
					Intent i=new Intent(getApplicationContext(), CoreActivity.class);
					startActivity(i);
					Toast.makeText(SignupActivity.this, "Wellcome to Ibabai!", Toast.LENGTH_LONG).show();
					finish();
				}
				else {
					Toast.makeText(SignupActivity.this, "Registration failed. Try again or visit HELP page", Toast.LENGTH_LONG).show();
					Log.d("CLIENT", json.getString("info"));
				}
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
