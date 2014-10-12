package com.android.ibabairetail.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class StopListService extends IntentService {

	private static final String STOPLIST_API_ENDPOINT_URL=IbabaiUtils.BASE_API_ENDPOINT_URL+"stoplists.json";
	private static final int NOTIFY_ID = 1020;	
	private int client_id;
	private String cl_id;
	private String pa_id;
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;	
	
	public StopListService() {
		super("StopListService");
	}
	@Override
	protected void onHandleIntent(Intent i) {
		dbh=DatabaseHelper.getInstance(this.getApplicationContext());
		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE); 
		pa_id = (String) i.getExtras().get(IbabaiUtils.EXTRA_NI);
		cl_id = getClientId(getPromoact(pa_id));
		client_id = Integer.parseInt(cl_id);
		File dir_src = new File(getPromoDir(this), pa_id);			
		File src = new File(dir_src, "client.jpg");
		Log.v("CLIENT", src.getAbsolutePath());
		
		ClientBlockAction(STOPLIST_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString(IbabaiUtils.AUTH_TOKEN, ""));

	}
	public void CopyClient(File src, File dst) throws IOException {
		FileChannel in = null;
		FileChannel out=null;
		try {
			in = new FileInputStream(src).getChannel();
			out = new FileOutputStream(dst).getChannel();
			out.transferFrom(in, 0, in.size());
			
		} finally {
			in.close();
			out.close();
		}
	}
	
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
	 }
	
	static File getPromoDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }
	private void ClientBlockAction(String url) {
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject pay_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();			
		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Try again!");					
										
				pay_json.put(DatabaseHelper.CL_ID, client_id);				
				holder.put("stoplist", pay_json);
				StringEntity se = new StringEntity(holder.toString());
				post.setEntity(se);
					
				post.setHeader("Accept", "application/json");
				post.addHeader("Content-Type", "application/json");
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(post, r_handler);
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
					Log.v("DEBIT", "Account deited");					
					File dir_src = new File(getPromoDir(this), pa_id);			
					File src = new File(dir_src, "client.jpg");
					Log.v("CLIENT", src.getAbsolutePath());
					File sl_dir = getStopDir(this);
					if (!sl_dir.exists()) {
						sl_dir.mkdirs();
					}
					String path = cl_id+"_client.jpg";
					File dst= new File(sl_dir, path);		
					Log.v("CLIENT", dst.getAbsolutePath());
					try {
						dst.createNewFile();
						CopyClient(src, dst);
					}
					catch (IOException ex) {
							Log.e("STOP", ex.toString());
							
					}					
					
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
	
	private Cursor getPromoact(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id = "+id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	
	private String getClientId(Cursor c) {
		String c_id = null;
		if (c != null && c.moveToFirst()) {
			int id_ind = c.getColumnIndex("client_id");
			int cl_id = c.getInt(id_ind);
			c_id = Integer.toString(cl_id);			
		}
		return c_id;
	}	
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Error warning!").setContentText("Client block failed. Try again!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, stopListActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, 0));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}	
}
