package com.android.ibabairetail.proto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import android.app.AlarmManager;
import android.app.PendingIntent;


public class DataUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctxt, Intent i) {
		if (i.getAction() != null) {
			scheduleAlarm(ctxt);
		}
		else {
			WakefulIntentService.sendWakefulWork(ctxt, DataUpdateService.class);
		}
	}
	static void scheduleAlarm(Context ctxt) {
		AlarmManager mgr=(AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
		Intent i=new Intent(ctxt, DataUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(ctxt, 0, i, 0);
		Calendar cal=Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, 2);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		if (cal.getTimeInMillis() < System.currentTimeMillis()) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		mgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
	}
}
