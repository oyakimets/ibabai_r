package com.android.ibabairetail.proto;

import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DeleteDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog del_dialog=null;
	DatabaseHelper dbh;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_del, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Delete", this).setNeutralButton("Block", this).setNegativeButton("Cancel", null).create(); 
		del_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = del_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = del_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
				Button b = del_dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				b.setTextSize(20);
				b.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(del_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		String pa_id=getArguments().getString("promoact");
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:			
			dbh.paStopUpdate(pa_id, 1);
			Intent ni_yes=new Intent(getActivity(), CoreActivity.class);			
			startActivity(ni_yes);			
			break;
		case AlertDialog.BUTTON_NEUTRAL:			
			dbh.paStopUpdate(pa_id, 1);
			Intent block_intent=new Intent(getActivity(), StopListService.class);
			block_intent.putExtra(IbabaiUtils.EXTRA_NI, pa_id);
			getActivity().startService(block_intent);			
			break;
		
		default:
			break;			
		}			
	}	
}
