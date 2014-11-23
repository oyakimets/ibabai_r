package com.android.ibabairetail.proto;

import java.io.File;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class UnblockDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog ub_dialog=null;
	DatabaseHelper dbh;
	private File sl_f;
	private String client_id;
	private int cl_id;
	SharedPreferences shared_prefs;
	
	
	public interface ReloadDataListener {
		public void ReloadData();
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
		shared_prefs = getActivity().getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_unblock, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("OK", this).setNegativeButton("Cancel", null).create(); 
		ub_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = ub_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = ub_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(ub_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:			
			int position=getArguments().getInt("position");
			File sl_dir = getStopDir(getActivity()); 
			if (sl_dir.exists() && sl_dir.isDirectory()) {
				File[] sl_lst = sl_dir.listFiles();
				sl_f = sl_lst[position];
				String path = sl_f.getAbsolutePath();			
				int s_ind = path.lastIndexOf("/");
				int f_ind = path.lastIndexOf("_");
				client_id = path.substring(s_ind+1, f_ind);
				cl_id = Integer.parseInt(client_id);
			}
			dbh.updateStatusUnblock(client_id);
			Intent intent = new Intent(getActivity(), ClientUnblockService.class);			
			intent.putExtra(DatabaseHelper.CL_ID, cl_id);
			intent.putExtra("position", position);
			Editor ub_editor = shared_prefs.edit();
			if (shared_prefs.contains(IbabaiUtils.BLOCK_COUNTER) && shared_prefs.getInt(IbabaiUtils.BLOCK_COUNTER, 0)>0){
				ub_editor.putInt(IbabaiUtils.BLOCK_COUNTER, shared_prefs.getInt(IbabaiUtils.BLOCK_COUNTER, 0)-1);
				ub_editor.apply();
			}					
			int size=getArguments().getInt("size");
			if (size > 1) {				
				getActivity().startService(intent);	
			}
			else {
				getActivity().startService(intent);	
				Intent ub_yes=new Intent(getActivity(), CoreActivity.class);				
				startActivity(ub_yes);
				getActivity().finish();
			}
			dbh.close();
			break;
		default:
			break;			
		}
			
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
	 }	
}
