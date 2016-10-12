package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samchat.R;
import com.android.samservice.info.PlacesInfo;

import android.graphics.Typeface;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlaceInfoAdapter extends BaseAdapter{
	static private String TAG = "SamchatPlaceInfoAdapter";
	
	private final int TYPE_PLACE = 0;
	private final int TYPE_MAX = TYPE_PLACE + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PlacesInfo> items;

	public PlaceInfoAdapter(Context context,List<PlacesInfo> list){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
	}
	
	@Override
	public int getCount(){
		return items==null ? 0:items.size();
	}
	
	@Override
	public int getViewTypeCount(){
		return TYPE_MAX;
	}
	
	@Override
	public int getItemViewType(int position){
		return TYPE_PLACE;
	}

	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_simple_list_item,parent,false);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_PLACE:
			holder.content.setText(items.get(position).description);
			if(position == 0 ){
				holder.content.setTextColor(mContext.getResources().getColor(R.color.color_black_1d4d73));
				holder.content.setTypeface(null, Typeface.BOLD);
			}else{
				holder.content.setTextColor(mContext.getResources().getColor(R.color.black));
				holder.content.setTypeface(null, Typeface.NORMAL);
			}
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public PlacesInfo getItem(int position){
		return items.get(position);
	}

	public static class ViewHolder{
		public TextView content;
	}
	
	
	
}



