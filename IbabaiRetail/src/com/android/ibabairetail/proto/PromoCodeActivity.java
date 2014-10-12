package com.android.ibabairetail.proto;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PromoCodeActivity extends FragmentActivity {
	private SharedPreferences shared_prefs;
	private int promo_code;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promo_code);
        
        shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_activation);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        promo_code = shared_prefs.getInt(IbabaiUtils.PROMO_CODE, 0); 
        if ( promo_code == 0) {
        	RequestCode();
        }
        TextView tv = (TextView) findViewById(R.id.promo_code);
        tv.setText(Integer.toString(promo_code));
        
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
	public void HaveProblem(View view) {		
	    	
		new ProblemDialogFragment().show(getSupportFragmentManager(), "problem");	    	
	    
	}
	private void RequestCode() {
		
	}
}
