package com.android.ibabairetail.proto;

import java.io.File;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PromoCodeActivity extends FragmentActivity {
	private SharedPreferences shared_prefs;
	private int promo_code;
	private int a_promo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promo_code);
        
        shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
        promo_code = shared_prefs.getInt(IbabaiUtils.PROMO_CODE, 0);
        a_promo = shared_prefs.getInt(IbabaiUtils.ACTIVE_PROMO, 0);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_activation);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);        
                
        ImageView iv = (ImageView) findViewById(R.id.code_tag);
        String dir = Integer.toString(a_promo);
        File pa_folder = new File(getConDir(PromoCodeActivity.this), dir);
		if (pa_folder.exists()) {
			File tag_file = new File(pa_folder, "exit.png");
			String grat_path = tag_file.getAbsolutePath();												
			Drawable d_grat = Drawable.createFromPath(grat_path);
			iv.setImageDrawable(d_grat);
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
		case android.R.id.home:
			appExit();
			return true;		
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	public void HaveProblem(View view) {
		
		new ProblemDialogFragment().show(getSupportFragmentManager(), "problem");		
	    
	}
	
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }
	@Override
	public void onBackPressed() {
		Intent e_int = new Intent(Intent.ACTION_MAIN);
		e_int.addCategory(Intent.CATEGORY_HOME);
		e_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(e_int);
		finish();
		
	}	
	public void appExit() {
		Intent e_int = new Intent(Intent.ACTION_MAIN);
		e_int.addCategory(Intent.CATEGORY_HOME);
		e_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(e_int);
		finish();
	}	
}
