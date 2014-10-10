package com.android.ibabairetail.proto;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PromoPresAdapter extends FragmentStatePagerAdapter {
	private PromoPresentation presentation=null;
	private String promo_folder=null;
	
	public PromoPresAdapter(FragmentActivity ctxt, PromoPresentation presentation, String promo_folder) {
		super(ctxt.getSupportFragmentManager());
		this.presentation=presentation;
		this.promo_folder=promo_folder;		
	}
	@Override
	public Fragment getItem(int position) {		
		String path=presentation.getSlideFile(position);
		return (SimpleContentFragment.newInstance("file:///"+promo_folder+path));
	}

	@Override
	public int getCount() {
		return (presentation.getSlidesCount());
	}
}
