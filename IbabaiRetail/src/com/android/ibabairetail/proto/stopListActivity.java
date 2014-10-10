package com.android.ibabairetail.proto;

import java.io.File;
import java.util.ArrayList;
import org.json.JSONArray;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.android.ibabairetail.proto.UnblockDialogFragment.ReloadDataListener;


public class stopListActivity extends FragmentActivity implements ReloadDataListener {
	private Bundle bundle;
	public static int sl_size=0;	
	private ListView StopList;
	private StopListAdapter sl_adapter;
	public static ArrayList<Drawable> StopListItems;	
	public static ArrayList<String> StopListIds;
	private File stop_dir;
	JSONArray stopJArr = null;
	SharedPreferences shared_prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_list);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_stoplist);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);        
             
       
	}
	
	private void LoadBlockTags() {
			 
		if (stop_dir.exists() && stop_dir.isDirectory()) {				 
			File[] f_lst = stop_dir.listFiles();
			String[] f_names = stop_dir.list();
			for (File f:f_lst) {
				Drawable d_stop = Drawable.createFromPath(f.getAbsolutePath());
				StopListItems.add(d_stop);
			}
			for (String s:f_names) {
				StopListIds.add(s);
			}			 
		 }
			 
		 sl_adapter = new StopListAdapter(getApplicationContext(), StopListItems);
		 sl_size=sl_adapter.getCount();			 
		 StopList.setAdapter(sl_adapter);
		 StopList.setOnItemClickListener(new StopListClickListener());		     
		 
	 }
	private class StopListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayStopAction(position);
		}		
	}
	private void displayStopAction(int pos) {
		bundle = new Bundle();
		bundle.putString("vend_f", StopListIds.get(pos));
    	bundle.putInt("position", pos);
    	bundle.putInt("size", StopListItems.size());
    	UnblockDialogFragment unbl = new UnblockDialogFragment();
    	unbl.setArguments(bundle);
    	unbl.show(getSupportFragmentManager(), "unblock");		
	}
	
	protected void onResume() {
		ReloadData();
        super.onResume();
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
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	public void ReloadData() {		
        StopList=(ListView) findViewById(R.id.stop_list);
        StopListIds = new ArrayList<String>();
        StopListItems = new ArrayList<Drawable>();
        stop_dir = getStopDir(this);
        LoadBlockTags();
        if(sl_adapter != null) {
        	sl_adapter.notifyDataSetChanged();
        }
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
	 }	
}
