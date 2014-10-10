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

public class StopListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Drawable> StopListItems;
	
	public StopListAdapter(Context context, ArrayList<Drawable> StopListItems ) {
		this.context=context;
		this.StopListItems=StopListItems;
	}
	@Override	
	public int getCount() {
		return StopListItems.size();
	}

	@Override
	public Object getItem(int position) {
		return StopListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.stop_list_item, null);
        }
          
        ImageView tagIcon = (ImageView) convertView.findViewById(R.id.stop_list_tag);                  
        tagIcon.setImageDrawable(StopListItems.get(position));        
        
        
		return convertView;
	}

}
