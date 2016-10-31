package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samchat.cache.FollowDataCache;
import com.android.samservice.info.ContactUser;
import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import android.widget.ImageView;

public class ContactUserAdapter extends BaseAdapter{
	static private String TAG = "SamchatContactUserAdapter";
	
	private final int TYPE_CONTACTUSER = 0;
	private final int TYPE_MAX = TYPE_CONTACTUSER + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ContactUser> items;

	private boolean isFollowVisible=true;
	public void setFollowVisible(boolean visible){
		isFollowVisible = visible;
	}

	public ContactUserAdapter(Context context,List<ContactUser> list){
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
		return TYPE_CONTACTUSER;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_contactuser_list_item,parent,false);
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.username = (TextView) convertView.findViewById(R.id.username);
			holder.service_category= (TextView) convertView.findViewById(R.id.service_category);
			holder.follow_tag = (TextView) convertView.findViewById(R.id.follow_tag);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_CONTACTUSER:
			ContactUser user = items.get(position);
			holder.username.setText(user.getusername());
			holder.service_category.setText(user.getservice_category());
			holder.avatar.loadBuddyAvatar(Long.toString(user.getunique_id()),(int)mContext.getResources().getDimension(R.dimen.avatar_size_default));
			if(isFollowVisible){
				if(FollowDataCache.getInstance().getFollowSPByUniqueID(user.getunique_id())!=null){
					holder.follow_tag.setText(mContext.getString(R.string.samchat_follow));
					holder.follow_tag.setTextColor(mContext.getResources().getColor(R.color.color_black_1d4d73));
				}else{
					holder.follow_tag.setText(mContext.getString(R.string.samchat_unfollow));
					holder.follow_tag.setTextColor(mContext.getResources().getColor(R.color.color_grey_d8d2e2));
				}
				holder.follow_tag.setVisibility(View.VISIBLE);
			}else{
				holder.follow_tag.setVisibility(View.GONE);
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
	public ContactUser getItem(int position){
		return items.get(position);
	}

	public static class ViewHolder{
		public HeadImageView avatar;
		public TextView username;
		public TextView service_category;
		public TextView follow_tag;
	}
	
	
	
}




