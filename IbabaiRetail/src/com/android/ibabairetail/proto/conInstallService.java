package com.android.ibabairetail.proto;

import android.app.IntentService;
import android.content.Intent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class conInstallService extends IntentService {
	public conInstallService() {
		super("conInstallService");
	}
	@Override
	protected void onHandleIntent(Intent i) {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String con_dir_path=prefs.getString(IbabaiUtils.PREF_CON_DIR, null);
		if (con_dir_path != null) {
			File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File update=new File(root, IbabaiUtils.CON_EXT);
			try {
				if (!(new File(con_dir_path)).exists()) {
					unzip(update, new File(con_dir_path));
				}
			}
			catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Exception unzipping update");
			}
			update.delete();
			Intent intent = new Intent(this, ConUpdateService.class);
			startService(intent);
			
		}
		else {
			Log.e(getClass().getSimpleName(), "null content dir path");
		}
	}
	private static void unzip(File src, File dest) throws IOException {
		InputStream is = new FileInputStream(src);
		ZipInputStream zis=new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;
		
		dest.mkdirs();
		
		while ((ze=zis.getNextEntry()) != null) {
			byte[] buffer=new byte[8192];
			int count;
			FileOutputStream fos=new FileOutputStream(new File(dest, ze.getName()));
			BufferedOutputStream out = new BufferedOutputStream(fos);
			try {
				while ((count=zis.read(buffer)) != -1) {
					out.write(buffer, 0, count);
				}
				out.flush();
			}
			finally {
				fos.getFD().sync();
				out.close();
			}
			zis.closeEntry();
		}
		zis.close();
	}
}
