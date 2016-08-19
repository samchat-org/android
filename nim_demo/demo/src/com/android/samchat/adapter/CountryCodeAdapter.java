package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.netease.nim.demo.R;
import com.android.samservice.info.SendQuestion;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.info.FollowUser;
import android.widget.ImageView;
import com.android.samservice.info.Contact;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import android.widget.LinearLayout;
import com.android.samchat.common.CountryInfo;
public class CountryCodeAdapter extends BaseAdapter{
	static private String TAG = "SamchatCountryCodeAdapter";

	private final int TYPE_TOP_HIT = 0;
	private final int TYPE_TOP_CHINA = 1;
	private final int TYPE_TOP_USA = 2;
	private final int TYPE_COUNTRY_CODE = 3;
	private final int TYPE_MAX = TYPE_COUNTRY_CODE + 1;

	private Context mContext;
	private LayoutInflater mInflater;
	private List<CountryInfo> items;

	public CountryCodeAdapter(Context context,List<CountryInfo> list){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
	}
	
	@Override
	public int getCount(){
		return ((items == null) ? 0 : items.size())+3;
	}
	
	@Override
	public int getViewTypeCount(){
		return TYPE_MAX;
	}
	
	@Override
	public int getItemViewType(int position){
		if(position == 0){
			return TYPE_TOP_HIT;
		}else if(position == 1){
			return TYPE_TOP_CHINA;
		}else if(position == 2){
			return TYPE_TOP_USA;
		}else{
			return TYPE_COUNTRY_CODE;
		}
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_countrycode_list_item,parent,false);
			holder.tv_tag = (TextView) convertView.findViewById(R.id.tv_lv_item_tag);
			holder.countryname_textview = (TextView) convertView.findViewById(R.id.countryname);
			holder.countrycode_textview = (TextView) convertView.findViewById(R.id.countrycode); 
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_TOP_HIT:
			holder.tv_tag.setVisibility(View.GONE);
			holder.countryname_textview.setText(mContext.getString(R.string.samchat_tophit));
			holder.countrycode_textview.setVisibility(View.GONE);
			break;
		case TYPE_TOP_CHINA:
			holder.tv_tag.setVisibility(View.GONE);
			holder.countryname_textview.setText(mContext.getString(R.string.China));
			holder.countrycode_textview.setVisibility(View.VISIBLE);
			holder.countrycode_textview.setText("+86");
			break;
			
		case TYPE_TOP_USA:
			holder.tv_tag.setVisibility(View.GONE);
			holder.countryname_textview.setText(mContext.getString(R.string.USA));
			holder.countrycode_textview.setVisibility(View.VISIBLE);
			holder.countrycode_textview.setText("+1");
			break;
			
		case TYPE_COUNTRY_CODE:
			CountryInfo info = items.get(position-3);
			holder.countryname_textview.setText(info.name);
			holder.countrycode_textview.setVisibility(View.VISIBLE);
			holder.countrycode_textview.setText("+"+info.code);

			int selection = info.getFPinYin().charAt(0);
			int positionForSelection = getPositionForSelection(selection);
			if (position-3 == positionForSelection) {
				holder.tv_tag.setVisibility(View.VISIBLE);
				holder.tv_tag.setText(info.getFPinYin());
			} else {
				holder.tv_tag.setVisibility(View.GONE);
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
	public CountryInfo getItem(int position){
		return items.get(position);
	}

	public int getPositionForSelection(int selection) {
		for (int i = 0; i < items.size(); i++) {
			String Fpinyin = items.get(i).getFPinYin();
			char first = Fpinyin.toUpperCase().charAt(0);
			if (first == selection) {
				return i;
			}
		}
		return -1;

	}

	public static class ViewHolder{
		public TextView tv_tag;
		public TextView countryname_textview;
		public TextView countrycode_textview;
	}
	
	
	
}




