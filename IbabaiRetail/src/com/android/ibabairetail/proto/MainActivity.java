package com.android.ibabairetail.proto;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends FragmentActivity {

	private ViewPager pager=null;
	private PresentationAdapter adapter=null;	
	SharedPreferences shared_prefs;
	private int a_promo;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);        
        	
        shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);        	   	
                        
        pager=(ViewPager)findViewById(R.id.pager);
        adapter=new PresentationAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        findViewById(R.id.pager).setVisibility(View.VISIBLE);
        
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_intro);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);         	
        
        GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }	
    }
    @Override
    protected void onResume() {
    	
    	if( getIntent().getBooleanExtra("EXIT", false)) {
    		finish();
    	}
    	if(shared_prefs.contains(IbabaiUtils.API_KEY)) {
    		a_promo = shared_prefs.getInt(IbabaiUtils.ACTIVE_PROMO, 0);
    		if (a_promo == 0) {
    			Intent launchIntent = new Intent(this, CoreActivity.class);
        		startActivity(launchIntent);        		
    		}
    		else {    			
    			Intent promo_intent=new Intent(this, PresentationDisplayActivity.class);			
    			promo_intent.putExtra(IbabaiUtils.EXTRA_PA, Integer.toString(a_promo));
    			startActivity(promo_intent);    			
    		}
    		finish();    		
    	}    	
    	super.onResume();
    }
    
    public void showTos(View view) {
    	Intent i = new Intent(this, tosActivity.class);
    	startActivity(i);
    }
    
    public void signUp(View view) {
    	if (isNetworkAvailable(this)) {
    		new TosDialogFragment().show(getSupportFragmentManager(), "tos");
    	}
    	else {
    		NetworkDialogFragment ndf = new NetworkDialogFragment();
        	ndf.show(getSupportFragmentManager(), "network");
    	}
    	
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
