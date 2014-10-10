package com.android.ibabairetail.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StopDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private String client_id;
	private AlertDialog sl_dialog=null;
	DatabaseHelper dbh;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_sl, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Yes", this).setNegativeButton("No", null).create(); 
		sl_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = sl_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = sl_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(sl_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:
			String pa_id=getArguments().getString("promoact");			
			Cursor to_sl_cursor = getPromoact(pa_id);
			client_id = getClientId(to_sl_cursor);
			to_sl_cursor.close();
			dbh.updateStatusBlock(client_id);
			Intent ser_int=new Intent(getActivity(), StopListService.class);
			ser_int.putExtra(IbabaiUtils.EXTRA_NI, pa_id);
			ser_int.putExtra(DatabaseHelper.CL_ID, client_id);
			getActivity().startService(ser_int);
			Intent sl_yes=new Intent(getActivity(), CoreActivity.class);
			sl_yes.putExtra(IbabaiUtils.EXTRA_NI, pa_id);
			startActivity(sl_yes);
			Toast.makeText(getActivity().getBaseContext(), "All promos of the company are blocked", Toast.LENGTH_LONG).show();
			
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
