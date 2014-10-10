package com.android.ibabairetail.proto;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


public class PromoListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Drawable> PromoListItems;
	
	public PromoListAdapter(Context context, ArrayList<Drawable> PromoListItems ) {
		this.context=context;
		this.PromoListItems=PromoListItems;
	}

	@Override
	public int getCount() {
		return PromoListItems.size();
	}

	@Override
	public Object getItem(int position) {
		return PromoListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.promo_list_item, null);
        }
          
        ImageView tagIcon = (ImageView) convertView.findViewById(R.id.promo_tag);
                  
        tagIcon.setImageDrawable(PromoListItems.get(position));        
        
         
        return convertView;
	}

}
