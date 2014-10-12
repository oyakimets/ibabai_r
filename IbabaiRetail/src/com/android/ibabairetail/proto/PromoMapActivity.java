package com.android.ibabairetail.proto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class PromoMapActivity extends Activity {
	private SharedPreferences shared_prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
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
	public void ResetAccount(View view) {
		Editor editor = shared_prefs.edit();
		editor.remove(IbabaiUtils.USER_ID);
		editor.remove(IbabaiUtils.EMAIL);
		editor.remove(IbabaiUtils.PHONE);
		editor.putString(IbabaiUtils.AUTH_TOKEN, "reset");
		editor.apply();	
		Toast.makeText(this, "Account Reset", Toast.LENGTH_SHORT).show();		
		
	}

}
