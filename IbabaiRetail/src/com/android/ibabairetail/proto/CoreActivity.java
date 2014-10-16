package com.android.ibabairetail.proto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CoreActivity extends FragmentActivity {
	private String sl_count = null;				
	private ListView PromoList;
	private PromoListAdapter pl_adapter=null;
	private ArrayList<Drawable> PromoListItems;		
	public static ArrayList<String> dbPromos;	
	private int a_promo;
	private Cursor pa_cursor;		
	SharedPreferences shared_prefs;	
	DatabaseHelper dbh;
	FileInputStream is;
	BufferedInputStream buf;
	private int cat_index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_core);                   
                
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_core);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);       
                
		if (servicesConnected()) {
			Intent ar_intent = new Intent(this, ARService.class);
        	startService(ar_intent);        	
		}
        else {
        	Intent start_i = new Intent(this, LocationService.class);
      	    startService(start_i);      	   
        } 
		
		DataUpdateReceiver.scheduleAlarm(this);
       
	}	
	private void LoadPromoTags() {
			 
		try {
			if (cat_index == 0) {
				pa_cursor=AllPromoCursor();
			}
			else {
				pa_cursor=CatPromoCursor(Integer.toString(cat_index));
			}
			if (pa_cursor != null && pa_cursor.moveToFirst()) {
				int id_ind = pa_cursor.getColumnIndex("promoact_id");
				while (!pa_cursor.isAfterLast()) {
					String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
					dbPromos.add(pa_id);
					pa_cursor.moveToNext();
				}
				pa_cursor.close();
			}
			
			for (int j=0; j< dbPromos.size(); j++) {					
				String dir=dbPromos.get(j);
				File pa_folder = new File(getConDir(CoreActivity.this), dir);
				if (pa_folder.exists()) {
					File tag_file = new File(pa_folder, "con_tag.jpg");
					String tag_path = tag_file.getAbsolutePath();												
					Drawable d_promo = Drawable.createFromPath(tag_path);
					PromoListItems.add(d_promo);								
				}							
			 }
			pl_adapter = new PromoListAdapter(getApplicationContext(), PromoListItems);
			pl_adapter.notifyDataSetChanged();
		    PromoList.setAdapter(pl_adapter);
		    PromoList.setOnItemClickListener(new PromoListClickListener());
		    dbh.close();
		}
		catch(Exception e) {
		 	e.printStackTrace();
		 }			
	}
		 
	@Override
	protected void onResume() {
		
		GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }               
		
		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);		
		a_promo=shared_prefs.getInt(IbabaiUtils.ACTIVE_PROMO, 0);
		cat_index=shared_prefs.getInt(DatabaseHelper.CAT, 0);
		if (a_promo == 0) {        
			dbh=DatabaseHelper.getInstance(getApplicationContext());
			dbPromos=new ArrayList<String>();			
			PromoList=(ListView) findViewById(R.id.promo_list);
			PromoListItems = new ArrayList<Drawable>();
			LoadPromoTags();
		}
		else {
			Intent promo_intent=new Intent(this, PresentationDisplayActivity.class);			
			promo_intent.putExtra(IbabaiUtils.EXTRA_PA, Integer.toString(a_promo));
			startActivity(promo_intent);
			finish();
		}
        
		
       
        super.onResume();		
	}	
	
	private class PromoListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayPromoAction(position);
		}		
	}
	private void displayPromoAction(int position) {
		Intent promo_intent=new Intent(this, PresentationDisplayActivity.class);
		promo_intent.putExtra(IbabaiUtils.EXTRA_POSITION, position);
		String pa_id = dbPromos.get(position);
		promo_intent.putExtra(IbabaiUtils.EXTRA_PA, pa_id);
		startActivity(promo_intent);		
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);		
        
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.home:			
			appExit();
			return true;
		case R.id.action_help:
			Intent in=new Intent(this, HelpActivity.class);
			startActivity(in);
			return true;
		case R.id.action_share:
			Intent sharingIntent=new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = "www.ibabai.com";
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Cool App!");
			sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Share through"));			
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {		
		return super.onPrepareOptionsMenu(menu);
	}	
		
	@Override
	public void onBackPressed() {
		Intent e_int = new Intent(Intent.ACTION_MAIN);
		e_int.addCategory(Intent.CATEGORY_HOME);
		e_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(e_int);
		
	}	
	
	private Cursor AllPromoCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor CatPromoCursor(String cat) {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0 AND category="+cat, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }	
	private String getStoplistCount() {
		String sl = null;
		File sl_dir = getStopDir(this);
		if (sl_dir.exists()) {
			int count = sl_dir.list().length;
			if (count>0) {
				sl = Integer.toString(count);
			}			
		}
		return sl;		
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
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
	public void toPromoMap(View view) {
		Intent map_intent = new Intent(this, PromoMapActivity.class);
		startActivity(map_intent);
	}
	public void toStopList() {
		Intent sl_intent = new Intent(this, stopListActivity.class);
		startActivity(sl_intent);
	}
	public void appExit() {
		Intent e_int = new Intent(Intent.ACTION_MAIN);
		e_int.addCategory(Intent.CATEGORY_HOME);
		e_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(e_int);		
	}	
	
}
