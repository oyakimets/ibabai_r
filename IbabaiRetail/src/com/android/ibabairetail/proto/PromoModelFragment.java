package com.android.ibabairetail.proto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PromoModelFragment extends Fragment {
	private PromoPresentation presentation=null;
	private PresLoadTask presTask=null;	
	
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setRetainInstance(true);
		deliverModel();
	}
	synchronized private void deliverModel() {
		if (presentation != null) {
			((PresentationDisplayActivity)getActivity()).setupPager(presentation);
		}
		else {
			if(presentation == null && presTask == null) {
				presTask = new PresLoadTask();
				executeAsyncTask(presTask, getActivity().getApplicationContext());
			}
		}
	}
	static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class PresLoadTask extends AsyncTask<Context, Void, Void> {		
		private PromoPresentation localPresentation=null;		
		private Exception e=null;		
		private File pa_dir;
		private String pa_id;		
		private File map;		
		
		@Override 
		protected Void doInBackground(Context... ctxt) {			
			pa_id=getActivity().getIntent().getStringExtra(IbabaiUtils.EXTRA_PA);			
			pa_dir = new File(getConDir(getActivity()), pa_id);			
			if (pa_dir.exists()) {
				map = new File(pa_dir, "promoact_map.txt");
				Log.v("MAP", map.getAbsolutePath());
			}
			try {
				StringBuilder buf=new StringBuilder();
				InputStream json=null; 	
				json = new FileInputStream(map);
				BufferedReader in = new BufferedReader(new InputStreamReader(json));				 
				String str;
				while((str=in.readLine()) != null ) {
					buf.append(str);
				}
				in.close();
				localPresentation=new PromoPresentation(new JSONObject(buf.toString()));
			}
			catch(Exception e) {
				this.e=e;
			}
			return null;
		}
		@Override
		public void onPostExecute(Void a) {
			if (e==null) {
				PromoModelFragment.this.presentation=localPresentation;
				PromoModelFragment.this.presTask=null;
				deliverModel();
			}
			else {
				Log.e(getClass().getSimpleName(), "Exception loading presentation", e);
			}
		}		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.CON_BASEDIR));
	 }
}
