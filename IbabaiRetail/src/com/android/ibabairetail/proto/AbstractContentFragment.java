package com.android.ibabairetail.proto;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractContentFragment extends WebViewFragment {
	abstract String getPage();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result=super.onCreateView(inflater, container, savedInstanceState);
		getWebView().getSettings().setJavaScriptEnabled(true);
		getWebView().getSettings().setSupportZoom(true);
		getWebView().getSettings().setBuiltInZoomControls(true);
		getWebView().loadUrl(getPage());
		return(result);
	}	
}
