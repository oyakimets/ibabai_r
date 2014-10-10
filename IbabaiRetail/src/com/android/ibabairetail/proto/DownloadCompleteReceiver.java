package com.android.ibabairetail.proto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import java.io.File;

public class DownloadCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctxt, Intent i) {
		File con_update=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), IbabaiUtils.CON_EXT);
		if(con_update.exists()) {
			WakefulIntentService.sendWakefulWork(ctxt, conInstallService.class);
		}
	}
}
