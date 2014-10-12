package com.android.ibabairetail.proto;

import java.io.File;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PresentationDisplayActivity extends FragmentActivity {		
	SharedPreferences shared_prefs;		
	private String pa_folder_path=null;
	private ViewPager pres_pager=null;
	private PromoPresAdapter adapter=null;		
	private String promoact_id;	
	DatabaseHelper dbh;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(getSupportFragmentManager().findFragmentByTag(IbabaiUtils.MODEL) == null) {
        	getSupportFragmentManager().beginTransaction().add(new PromoModelFragment(), IbabaiUtils.MODEL).commit();
        	
        }
        
        shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
        
        if (shared_prefs.getInt(IbabaiUtils.STORE_ID, 0) == 0) {
        	setContentView(R.layout.presentation_pager); 
        }
        else {
        	setContentView(R.layout.presentation_pager_in_store);
        }
        
        
        pres_pager = (ViewPager)findViewById(R.id.presentation_pager);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);       
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        dbh = DatabaseHelper.getInstance(this);       
        
        promoact_id=getIntent().getStringExtra(IbabaiUtils.EXTRA_PA);        
                
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_delete:			
			Bundle bundle = new Bundle();
	    	bundle.putString("promoact", promoact_id);
	    	DeleteDialogFragment ddf = new DeleteDialogFragment();
	    	ddf.setArguments(bundle);
	    	ddf.show(getSupportFragmentManager(), "ni");			
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
	}
	
	void setupPager(PromoPresentation presentation) {		
		File pa_folder = new File(getConDir(this), promoact_id);
		if (pa_folder.exists()) {
			pa_folder_path = pa_folder.getAbsoluteFile()+"/";
		}	
		
		adapter = new PromoPresAdapter(this, presentation, pa_folder_path);
		pres_pager.setAdapter(adapter);
	}
	public static String getPromoDir(int position) {			   
	    return CoreActivity.allDirs.get(position);
	}
	public void showPromoRules(View v) {				
		Intent promo_rules_intent=new Intent(this, PromoRulesActivity.class);
		promo_rules_intent.putExtra(IbabaiUtils.EXTRA_DIR, promoact_id);
		startActivity(promo_rules_intent); 
	}
	public void PromoMap(View v) {		
		
	}	
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }	
	
}
