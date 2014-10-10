package com.android.ibabairetail.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class HelpActivity extends Activity {
	FaqListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        prepareListData();
        
        listAdapter = new FaqListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_help);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);        
        
	}
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();
		
		listDataHeader.add("Faq #1");
		listDataHeader.add("Faq #2");
		listDataHeader.add("Faq #3");
		listDataHeader.add("Faq #4");
		listDataHeader.add("Faq #5");
		listDataHeader.add("Faq #6");
		
		List<String> faq1 = new ArrayList<String>();
		faq1.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		List<String> faq2 = new ArrayList<String>();
		faq2.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		List<String> faq3 = new ArrayList<String>();
		faq3.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		List<String> faq4 = new ArrayList<String>();
		faq4.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		List<String> faq5 = new ArrayList<String>();
		faq5.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		List<String> faq6 = new ArrayList<String>();
		faq6.add("Qwerty asdfgh asdfg zxcvb mnbvcx lkjhgf poiuytre");
		
		listDataChild.put(listDataHeader.get(0), faq1);
		listDataChild.put(listDataHeader.get(1), faq2);
		listDataChild.put(listDataHeader.get(2), faq3);
		listDataChild.put(listDataHeader.get(3), faq4);
		listDataChild.put(listDataHeader.get(4), faq5);
		listDataChild.put(listDataHeader.get(5), faq6);
		
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
	
	public void callBack(View view) {
		String msg = "Please call me back for support!";
		String number="0504444007";
		SmsManager.getDefault().sendTextMessage(number, null, msg, null, null);
		Toast t = Toast.makeText(this, "We'll call you back ASAP", Toast.LENGTH_LONG);
		t.show();
	}
	
	public void sendMail(View view) {
		Intent emailInt = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "oleg.yakimets@gmail.com", null));
		emailInt.putExtra(Intent.EXTRA_SUBJECT, "Support request");
		startActivity(Intent.createChooser(emailInt, "Send email"));
	}

}
