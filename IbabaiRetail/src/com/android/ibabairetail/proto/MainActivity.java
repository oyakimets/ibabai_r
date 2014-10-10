package com.android.ibabairetail.proto;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class MainActivity extends FragmentActivity {

	private ViewPager pager=null;
	private PresentationAdapter adapter=null;	
	SharedPreferences shared_prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        	setContentView(R.layout.activity_main);        
        	
                        
        	pager=(ViewPager)findViewById(R.id.pager);
        	adapter=new PresentationAdapter(getSupportFragmentManager());
        	pager.setAdapter(adapter);
        	findViewById(R.id.pager).setVisibility(View.VISIBLE);
        
        	ActionBar ab = getActionBar(); 
        	ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	ab.setCustomView(R.layout.ab_intro);
        	ab.setDisplayShowHomeEnabled(true);
        	ab.setDisplayShowTitleEnabled(false);  	
        	
        	
    }
    @Override
    protected void onResume() {
    	shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
    	if( getIntent().getBooleanExtra("EXIT", false)) {
    		finish();
    	}
    	if(shared_prefs.contains(IbabaiUtils.AUTH_TOKEN)) {
    		Intent launchIntent = new Intent(this, CoreActivity.class);
    		startActivity(launchIntent);
    		finish();
    	}
    	else {
    		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    		if (ConnectionResult.SUCCESS == resultCode) {
    			Log.d("GF Detection", "Google Play Service is available");    			
    		}
    		else {
    			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
    			if (dialog != null) {
    				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
    				errorFragment.setDialog(dialog);
    				errorFragment.show(getFragmentManager(), "PlayService error");
    			}    			
    		}    		 
    	}
    	super.onResume();
    }
    
    public void showTos(View view) {
    	Intent i = new Intent(this, tosActivity.class);
    	startActivity(i);
    }
    
    public void signUp(View view) {
    	new TosDialogFragment().show(getSupportFragmentManager(), "tos");    	
    	
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       
        return super.onOptionsItemSelected(item);
    }
    
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog m_dialog;
		
		public ErrorDialogFragment() {
			super();
			m_dialog = null;
		}
		public void setDialog(Dialog dialog) {
			m_dialog = dialog;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return m_dialog;
		}
	}	
}
