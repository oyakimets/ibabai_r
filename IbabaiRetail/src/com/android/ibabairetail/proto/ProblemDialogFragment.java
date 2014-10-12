package com.android.ibabairetail.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class ProblemDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog problem_dialog=null;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_problem, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Help", this).setNegativeButton(android.R.string.cancel, null).create(); 
		problem_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = problem_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = problem_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(problem_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent i = new Intent(getActivity(), HelpActivity.class);
    	startActivity(i);
	}
}
