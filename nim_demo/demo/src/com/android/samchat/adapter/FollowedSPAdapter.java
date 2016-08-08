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

public class FollowedSPAdapter extends BaseAdapter{
	static private String TAG = "FollowedSPAdapter";
	
	private final int TYPE_FOLLOWEDSP = 0;
	private final int TYPE_MAX = TYPE_FOLLOWEDSP + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<FollowUser> items;

	public FollowedSPAdapter(Context context,List<FollowUser> list){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
	}
	
	@Override
	public int getCount(){
		return items == null ? 0 : items.size();
	}
	
	@Override
	public int getViewTypeCount(){
		return TYPE_MAX;
	}
	
	@Override
	public int getItemViewType(int position){
		return TYPE_FOLLOWEDSP;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_followed_sp_list_item,parent,false);
			holder.avatar= (ImageView) convertView.findViewById(R.id.avatar);
			holder.username = (TextView) convertView.findViewById(R.id.username);
			holder.service_category= (TextView) convertView.findViewById(R.id.service_category);
			holder.adv_content= (TextView) convertView.findViewById(R.id.adv_content);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_FOLLOWEDSP:
			FollowUser user = items.get(position);
			holder.username.setText(user.getusername());
			holder.service_category.setText(user.getservice_category());
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public FollowUser getItem(int position){
		return items.get(position);
	}

	public static class ViewHolder{
		public ImageView avatar;
		public TextView username;
		public TextView service_category;
		public TextView adv_content;
	}
	
	
	
}



