package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samchat.activity.SamchatContactUserSPNameCardActivity;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.common.BasicUserInfoHelper;
import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.RcvdAdvSession;
import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;

import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;

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

	private boolean isBlock(FollowedSamPros user) {
		return (user.getblock_tag() != Constants.NO_TAG);
	}

	private boolean isMute(FollowedSamPros user) {
		String public_account = NimConstants.PUBLIC_ACCOUNT_PREFIX+user.getunique_id();
		return !NIMClient.getService(FriendService.class).isNeedMessageNotify(public_account);
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
			holder.adv_time = (TextView) convertView.findViewById(R.id.adv_time);
			holder.mute_img = (ImageView) convertView.findViewById(R.id.mute_img);
			holder.block_img = (ImageView) convertView.findViewById(R.id.block_img);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.avatar.setOnClickListener(new COrder(position));

		
		switch(viewType){
		case TYPE_FOLLOWEDSP:
			FollowedSamPros user = items.get(position);
			holder.avatar.loadBuddyAvatar(""+user.getunique_id(), (int) mContext.getResources().getDimension(R.dimen.avatar_size_default));
			int labelWidth = ScreenUtil.screenWidth;
			labelWidth -= ScreenUtil.dip2px(50 + 64 + 12 + 12 + 12); 
			if (labelWidth > 0) {
				holder.username.setMaxWidth(labelWidth/2);
			}
			holder.username.setText(user.getusername());
			if (labelWidth > 0) {
				holder.service_category.setMaxWidth(labelWidth/2);
			}
			holder.service_category.setText(user.getservice_category());
			holder.mute_img.setVisibility(isMute(user)?View.VISIBLE:View.GONE);
			holder.block_img.setVisibility(isBlock(user)?View.VISIBLE:View.GONE);
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

				String timeString = TimeUtil.getTimeShowString(session.getrecent_adv_publish_timestamp(), true);
				holder.adv_time.setText(timeString);
				if (!TextUtils.isEmpty(timeString) && timeString.equals("1970-01-01")) {
					holder.adv_time.setVisibility(View.GONE);
				} else {
					holder.adv_time.setVisibility(View.VISIBLE);
				}

				if(session.getunread()>0){
					holder.avatar.setBorderColorResource(R.color.samchat_color_avatar_border_reminder);
				}else{
					holder.avatar.setBorderColorResource(R.color.samchat_color_avatar_border_default);
				}
			}else{
				holder.unread_reminder.setVisibility(View.GONE);
				holder.adv_content.setText("");
				holder.adv_time.setVisibility(View.GONE);
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
		public TextView adv_time;
		public ImageView mute_img;
		public ImageView block_img;

		public static void refreshItem(ViewHolder holder, FollowedSamPros user){
			holder.avatar.loadBuddyAvatar(""+user.getunique_id(), (int) DemoCache.getContext().getResources().getDimension(R.dimen.avatar_size_default));
			int labelWidth = ScreenUtil.screenWidth;
			labelWidth -= ScreenUtil.dip2px(50 + 64 + 12 + 12 + 12); 
			if (labelWidth > 0) {
				holder.username.setMaxWidth(labelWidth/2);
			}
			holder.username.setText(user.getusername());
			if (labelWidth > 0) {
				holder.service_category.setMaxWidth(labelWidth/2);
			}
			holder.service_category.setText(user.getservice_category());
		}
	}

	private class COrder implements View.OnClickListener {
		private int position;
		COrder(int p) {
			position = p;
		}
		@Override
		public void onClick(View v) {
			ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(items.get(position).getunique_id());
			if(user != null){
				SamchatContactUserSPNameCardActivity.start(mContext, user);
			}else{
				SamchatContactUserSPNameCardActivity.start(mContext, ""+items.get(position).getunique_id(), BasicUserInfoHelper.getUserName(items.get(position).getunique_id()));
			}
		}
	}
}



