package com.android.ibabairetail.proto;

import java.io.File;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class UnblockDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog ub_dialog=null;
	DatabaseHelper dbh;
	
	public interface ReloadDataListener {
		public void ReloadData();
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
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
			Intent intent = new Intent(getActivity(), ClientUnblockService.class);
			intent.putExtra("position", position);					
			int size=getArguments().getInt("size");
			if (size > 1) {				
				getActivity().startService(intent);	
			}
			else {
				getActivity().startService(intent);	
				Intent ub_yes=new Intent(getActivity(), CoreActivity.class);				
				startActivity(ub_yes);				
			}
			break;
		default:
			break;			
		}
			
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.SL_BASEDIR));
	 }	
}
