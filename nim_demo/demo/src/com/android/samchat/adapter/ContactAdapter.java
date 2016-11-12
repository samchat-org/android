package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import android.widget.ImageView;
import com.android.samservice.info.Contact;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;

import android.widget.LinearLayout;

public class ContactAdapter extends BaseAdapter{
	static private String TAG = "SamchatContactAdapter";
	
	private final int TYPE_CONTACT = 0;
	private final int TYPE_MAX = TYPE_CONTACT + 1;

	//0: contact list adapter
	//1: customer list adapter
	private int type;
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Contact> items;

	public ContactAdapter(Context context,List<Contact> list, int t){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
		type = t;
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
		return TYPE_CONTACT;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			if(type == 0){
				convertView = mInflater.inflate(R.layout.samchat_contact_list_item,parent,false);
			}else{
				convertView = mInflater.inflate(R.layout.samchat_customer_list_item,parent,false);
			}
			holder.tv_tag = (TextView) convertView.findViewById(R.id.tv_lv_item_tag);
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.info_layout = (LinearLayout) convertView.findViewById(R.id.info_layout);
			holder.username = (TextView) convertView.findViewById(R.id.username);
			if(type == 0){
				holder.service_category= (TextView) convertView.findViewById(R.id.service_category);
			}
			holder.block_img = (ImageView) convertView.findViewById(R.id.block_img);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_CONTACT:
			Contact user = items.get(position);
			holder.avatar.loadBuddyAvatar(Long.toString(user.getunique_id()));
			holder.username.setText(user.getusername());
			if(type == 0){
				holder.service_category.setText(user.getservice_category());
			}

			int selection = user.getFPinYin().charAt(0);
			int positionForSelection = getPositionForSelection(selection);
			if (position == positionForSelection) {
				holder.tv_tag.setVisibility(View.VISIBLE);
				holder.tv_tag.setText(user.getFPinYin());
			} else {
				holder.tv_tag.setVisibility(View.GONE);
			}

			boolean block = NIMClient.getService(FriendService.class).isInBlackList(user.getAccount());
        	holder.block_img.setVisibility(block ? View.VISIBLE:View.GONE);
			
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public Contact getItem(int position){
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
		public HeadImageView avatar;
		public LinearLayout info_layout;
		public TextView username;
		public TextView service_category;
		public ImageView block_img;

		public static void refreshItem(ViewHolder holder,Contact user, int type){
			if(holder == null || holder.avatar == null){
				LogUtil.i("test","holder:"+holder+" avatar:"+holder.avatar);
			}

			if(user == null ){
				LogUtil.i("test","user null");
			}
			holder.avatar.loadBuddyAvatar(Long.toString(user.getunique_id()));
			if(type == 0){
				holder.service_category.setText(user.getservice_category());
			}
			boolean block = NIMClient.getService(FriendService.class).isInBlackList(user.getAccount());
        	holder.block_img.setVisibility(block ? View.VISIBLE:View.GONE);
		}
	}
	
	
	
}




