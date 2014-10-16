package com.android.ibabairetail.proto;

import java.io.File;
import java.io.IOException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.ibabairetail.proto.UnblockDialogFragment.ReloadDataListener;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class ClientUnblockService extends IntentService {
	private static final String STOPLIST_API_ENDPOINT_URL=IbabaiUtils.BASE_API_ENDPOINT_URL+"stoplists.json";
	private static final int NOTIFY_ID = 1030;		
	private int position;
	private File sl_f;
	private String client_id;
	private int cl_id;
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	public ClientUnblockService() {
		super("ClientUnblockService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		dbh=DatabaseHelper.getInstance(this.getApplicationContext());
		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE); 
		position = (Integer) intent.getExtras().get("position");
		
		File sl_dir = getStopDir(this); 
		if (sl_dir.exists() && sl_dir.isDirectory()) {
			File[] sl_lst = sl_dir.listFiles();
			sl_f = sl_lst[position];
			String path = sl_f.getAbsolutePath();			
			int s_ind = path.lastIndexOf("/");
			int f_ind = path.lastIndexOf("_");
			client_id = path.substring(s_ind+1, f_ind);
			cl_id = Integer.parseInt(client_id);
		}
		ClientUnblockAction(STOPLIST_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString("AuthToken", ""));

	}
	private void ClientUnblockAction(String url) {
		
		DefaultHttpClient client = new DefaultHttpClient();
		DeleteWithBody delete = new DeleteWithBody(url);
		JSONObject holder = new JSONObject();
		JSONObject pay_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();			
		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Try again!");					
										
				pay_json.put(DatabaseHelper.CL_ID, cl_id);				
				holder.put("stoplist", pay_json);
				StringEntity se = new StringEntity(holder.toString());
				delete.setEntity(se);
					
				delete.setHeader("Accept", "application/json");
				delete.addHeader("Content-Type", "application/json");
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(delete, r_handler);
				json = new JSONObject(response);					
			}
			catch (HttpResponseException ex) {
				ex.printStackTrace();
				Log.e("ClientProtocol", ""+ex);
				json.put("info", "Response Error");
			}
			catch (IOException ex) {
				ex.printStackTrace();
				Log.e("IO", ""+ex);
			}
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			Log.e("JSON", ""+ex);
		}
		if (json != null) {
			
			try {
				if (json.getBoolean("success")) {
					Log.v("UNBLOCK", "Client unblocked");					
					dbh.updateStatusUnblock(client_id);
					dbh.close();
					sl_f.delete();
					ReloadDataListener activity = (ReloadDataListener) this;
					activity.ReloadData();
					
				}
				else {
					raiseNotification(this, null);	
					Log.e("DEBIT", json.getString("info"));
					/* launch "payment failed" sms/email
					 * 
					 */
				}	
			}
			catch(Exception e) {
				Log.e("SCAN", "Post execute exception");
			}			
		}
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Error warning!").setContentText("Client unblock failed. Try again!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, stopListActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, 0));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
	 }	

}
