package com.android.ibabairetail.proto;

import java.util.ArrayList;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {
	public static ArrayList<String> userPromos;
	public static ArrayList<String> storePromos;
	SharedPreferences shared_prefs;	
	DatabaseHelper dbh;
	private static final int NOTIFY_ID = 1000;
	private int offers = 0;
	private String str;

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		dbh = DatabaseHelper.getInstance(ctxt);
		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		int st_id = (Integer)intent.getExtras().get("st_id");
		Boolean entering = intent.getBooleanExtra(key, false);
		shared_prefs = ctxt.getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = shared_prefs.edit();
		if (entering && hasPromos(st_id)) {			
			editor.putInt(IbabaiUtils.STORE_ID, st_id);
			editor.putInt(IbabaiUtils.LAST_STORE, st_id);
			editor.apply();			
			Log.d(getClass().getSimpleName(), "entering");			
			raiseNotification(ctxt, null);	
			Intent entry_i = new Intent(ctxt, EntryRegService.class);
			ctxt.startService(entry_i);
		}
		else {
				if (st_id == shared_prefs.getInt(IbabaiUtils.LAST_STORE, 0)) {
					Intent exit_i = new Intent(ctxt, ExitRegService.class);
					ctxt.startService(exit_i);
					editor.putInt(IbabaiUtils.STORE_ID, 0);
					editor.apply();			
					Log.d(getClass().getSimpleName(), "exiting");
			}
		}
		
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		
		if (e == null) {
			if (offers == 1) {
				str = offers + " offer";
			}
			else {
				str = offers + " offers";
			}
			b.setContentTitle("Hello!").setContentText("You have " +str+ " from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, CoreActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}
	private boolean hasPromos(int store_id) {
		userPromos = new ArrayList<String>();
		storePromos=new ArrayList<String>();
		Cursor pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (!pa_cursor.isAfterLast()) {
				String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
				userPromos.add(pa_id);
				pa_cursor.moveToNext();
			}
			pa_cursor.close();
		}
			
		Cursor ps_cursor = storePromosCursor(store_id);
		if(ps_cursor != null && ps_cursor.moveToFirst()) {
			int paid_ind = ps_cursor.getColumnIndex("promoact_id");
			while (!ps_cursor.isAfterLast()) {
				String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
				storePromos.add(promoact_id);				
				ps_cursor.moveToNext();
			}
			ps_cursor.close();
		}
		if (userPromos != null && storePromos != null) {
			for (int i=0; i<storePromos.size(); i++) {
				if (userPromos.contains(storePromos.get(i))) {
					offers++;						
				}				
			}
		}
		if (offers > 0) {
			return true;
		}
		else {
			return false;
		}
			
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	
}
