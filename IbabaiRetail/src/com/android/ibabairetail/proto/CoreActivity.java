package com.android.ibabairetail.proto;

import android.support.v4.app.FragmentActivity;
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
	public static ArrayList<String> allDirs;
	public static ArrayList<String> dbPromos;
	private static int store_id;	
	private Cursor pa_cursor;
	private Cursor ps_cursor;	
	SharedPreferences shared_prefs;	
	DatabaseHelper dbh;
	FileInputStream is;
	BufferedInputStream buf;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_core);
                    
                
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);        
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);       
                
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
			pa_cursor=promoactCursor();
			if (pa_cursor != null && pa_cursor.moveToFirst()) {
				int id_ind = pa_cursor.getColumnIndex("promoact_id");
				while (!pa_cursor.isAfterLast()) {
					String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
					dbPromos.add(pa_id);
					pa_cursor.moveToNext();
				}
				pa_cursor.close();
			}
			if (store_id == 0) {
				allDirs=dbPromos;						 
			}
			else {
				ps_cursor = storePromosCursor(store_id);
				if(ps_cursor != null && ps_cursor.moveToFirst()) {
					int paid_ind = ps_cursor.getColumnIndex("promoact_id");
					while (!ps_cursor.isAfterLast()) {
						String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
						if (dbPromos.contains(promoact_id)) {
							allDirs.add(promoact_id);
						}
						ps_cursor.moveToNext();
					}
					ps_cursor.close();						 						
				}
			}				
				
			for (int j=0; j< allDirs.size(); j++) {					
				String dir=allDirs.get(j);
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
		}
		catch(Exception e) {
		 	e.printStackTrace();
		 }			
	}
		 
	@Override
	protected void onResume() {
		
		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		store_id=shared_prefs.getInt(IbabaiUtils.STORE_ID, 0);        
        
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		dbPromos=new ArrayList<String>();
        allDirs=new ArrayList<String>();
        PromoList=(ListView) findViewById(R.id.promo_list);
        PromoListItems = new ArrayList<Drawable>();
        LoadPromoTags();        
        
		GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }               
       
        super.onResume();		
	}
	
	@Override
	protected void onDestroy() {
		dbh.close();
		super.onDestroy();
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
		String pa_id = allDirs.get(position);
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
	
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
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
}
