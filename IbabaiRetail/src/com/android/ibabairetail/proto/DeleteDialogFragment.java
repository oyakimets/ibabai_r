package com.android.ibabairetail.proto;

import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DeleteDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog del_dialog=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
		shared_prefs = getActivity().getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
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
		String cl_id = getClientId(getPromoact(pa_id));
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
			dbh.updateStatusBlock(cl_id);
			Editor b_editor = shared_prefs.edit();
			if (!shared_prefs.contains(IbabaiUtils.BLOCK_COUNTER)) {
				b_editor.putInt(IbabaiUtils.BLOCK_COUNTER, 1);
				b_editor.apply();
			}
			else {
				b_editor.putInt(IbabaiUtils.BLOCK_COUNTER, shared_prefs.getInt(IbabaiUtils.BLOCK_COUNTER, 0)+1);
				b_editor.apply();
			}
			Intent block_intent=new Intent(getActivity(), StopListService.class);
			block_intent.putExtra(IbabaiUtils.EXTRA_NI, pa_id);
			getActivity().startService(block_intent);
			Intent core_intent = new Intent(getActivity(), CoreActivity.class);
			getActivity().startActivity(core_intent);
			getActivity().finish();
			dbh.close();
			break;
		
		default:
			break;			
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
}
