package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.RcvdAdvSession;
import com.netease.nim.demo.R;
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

public class FollowedSPAdapter extends BaseAdapter{
	static private String TAG = "FollowedSPAdapter";
	
	private final int TYPE_FOLLOWEDSP = 0;
	private final int TYPE_MAX = TYPE_FOLLOWEDSP + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<FollowedSamPros> items;
	private List<RcvdAdvSession> sessions;

	public FollowedSPAdapter(Context context,List<FollowedSamPros> list,List<RcvdAdvSession> snlist){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
		sessions = snlist;
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
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.username = (TextView) convertView.findViewById(R.id.username);
			holder.service_category= (TextView) convertView.findViewById(R.id.service_category);
			holder.adv_content= (TextView) convertView.findViewById(R.id.adv_content);
			holder.unread_reminder = (TextView) convertView.findViewById(R.id.unread_reminder);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_FOLLOWEDSP:
			FollowedSamPros user = items.get(position);
			holder.avatar.loadBuddyAvatar(""+user.getunique_id(), 50);
			holder.username.setText(user.getusername());
			holder.service_category.setText(user.getservice_category());
			RcvdAdvSession session = findSession(user.getunique_id());
			if(session != null && session.getrecent_adv_id()!=0){
				if(session.getrecent_adv_type() == Constants.ADV_TYPE_TEXT){
					holder.adv_content.setText(session.getrecent_adv_content());
				}else if(session.getrecent_adv_type() == Constants.ADV_TYPE_PIC){
					holder.adv_content.setText("["+mContext.getString(R.string.samchat_picture)+"]");
				}else{
					holder.adv_content.setText("["+mContext.getString(R.string.samchat_video)+"]");
				}

				if(session.getunread() >0){
					holder.unread_reminder.setVisibility(View.VISIBLE);
					holder.unread_reminder.setText(unreadCountShowRule(session.getunread()));
				}else{
					holder.unread_reminder.setVisibility(View.GONE);
				}
			}else{
				holder.unread_reminder.setVisibility(View.GONE);
				holder.adv_content.setText("");
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
	public FollowedSamPros getItem(int position){
		return items.get(position);
	}

	private RcvdAdvSession findSession(long unique_id){
		for(RcvdAdvSession s: sessions){
			if(s.getsession() == unique_id){
				return s;
			}
		}
		return null;
	}

	protected String unreadCountShowRule(int unread) {
        unread = Math.min(unread, 99);
        return String.valueOf(unread);
    }

	public static class ViewHolder{
		public HeadImageView avatar;
		public TextView username;
		public TextView service_category;
		public TextView adv_content;
		public TextView unread_reminder;
	}
	
	
	
}



