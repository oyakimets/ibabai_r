package com.android.ibabairetail.proto;

import java.io.File;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class PromoRulesActivity extends FragmentActivity {
SharedPreferences shared_prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String dir = getIntent().getStringExtra(IbabaiUtils.EXTRA_DIR); 
        File pa_folder = new File(getConDir(this), dir);
        if (getSupportFragmentManager().findFragmentById(R.id.promo_rules)==null) {
        	if (pa_folder.exists()) {
        		Fragment f=SimpleContentFragment.newInstance("file:///"+pa_folder+"/rules.jpg");
        		getSupportFragmentManager().beginTransaction().add(R.id.promo_rules, f).commit();
        	}
		}		
        
        setContentView(R.layout.promo_rules);        
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);        
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
                        
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
		case R.id.action_help:
			Intent i=new Intent(this, HelpActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }
}
