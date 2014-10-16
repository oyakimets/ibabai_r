package com.android.ibabairetail.proto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

public class PromoMapActivity extends Activity {
	private SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        dbh = DatabaseHelper.getInstance(getApplicationContext());
        shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_help);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);        
             
        
        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);		
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;		
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	@Override
	protected void onResume() {
		
		String s_id = Integer.toString(shared_prefs.getInt(IbabaiUtils.STORE_ID, 0));
		TextView tv1 = (TextView) findViewById(R.id.tv_1);
        tv1.setText(s_id); 
		int activity_code = shared_prefs.getInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
        TextView tv2 = (TextView)findViewById(R.id.tv_2);
        tv2.setText(getNameFromType(activity_code));       
        
		String last_s = Integer.toString(shared_prefs.getInt(IbabaiUtils.LAST_STORE, 0));
		String gf = shared_prefs.getString("geofence", "????");
	    TextView tv3 = (TextView)findViewById(R.id.tv_3);
	    tv3.setText(gf);
	    
	    
	    TextView tv4 = (TextView) findViewById(R.id.tv_4);
        
	     
	     super.onResume();
	}
	public void setPromo(View view) {
		Editor editor = shared_prefs.edit();
		editor.putInt(IbabaiUtils.ACTIVE_PROMO, 14);		
		editor.apply();	
		Intent i = new Intent(this, CoreActivity.class);
		startActivity(i);		
		
	}
	public void killPromo(View view) {
		Editor editor = shared_prefs.edit();
		editor.putInt(IbabaiUtils.ACTIVE_PROMO, 0);		
		editor.apply();	
		Intent i = new Intent(this, PresentationDisplayActivity.class);
		startActivity(i);		
	}
	private String CitiesCount() {		
		String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
		Cursor c = dbh.getWritableDatabase().rawQuery(c_query, null);
		String cnt = Integer.toString(c.getCount());
		c.close();
		return cnt;			
	}

	private String StoresCount() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		Cursor c = dbh.getWritableDatabase().rawQuery(s_query, null);
		String cnt = Integer.toString(c.getCount());
		c.close();
		return cnt;			
	}
	private String PsCount() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_SP);
		Cursor c = dbh.getWritableDatabase().rawQuery(s_query, null);
		String cnt = Integer.toString(c.getCount());
		c.close();
		return cnt;	
	}
	private String promosCount() {		
		String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_P);
		Cursor c = dbh.getWritableDatabase().rawQuery(p_query, null);
		String cnt = Integer.toString(c.getCount());
		c.close();
		return cnt;	
	}
	private String getNameFromType(int activity) {
		switch (activity) {
		 case DetectedActivity.IN_VEHICLE :
			 return "CAR";
		 case DetectedActivity.ON_BICYCLE :
			 return "BIKE";
		 case DetectedActivity.ON_FOOT :
			 return "ON FOOT";
		 case DetectedActivity.STILL :
			 return "STILL";
		 case DetectedActivity.UNKNOWN :
			 return "UNKNOWN";
		 case DetectedActivity.TILTING :
			 return "TILTING";
		}
		return "UNKNOWN";
	}
	
}
