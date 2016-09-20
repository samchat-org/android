package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samservice.info.Contact;
import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.msg.model.IMMessage;

public class SelectMemberAdapter extends BaseAdapter{
	static private String TAG = "SamchatContactAdapter";
	
	private final int TYPE_CONTACT = 0;
	private final int TYPE_MAX = TYPE_CONTACT + 1;

	private Context mContext;
	private LayoutInflater mInflater;
	private List<Contact> items;
	private List<String> selected;

	public SelectMemberAdapter(Context context,List<Contact> list, List<String> s){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
		selected = s;
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
			convertView = mInflater.inflate(R.layout.samchat_member_list_item,parent,false);
			holder.tv_tag = (TextView) convertView.findViewById(R.id.tv_lv_item_tag);
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.info_layout = (LinearLayout) convertView.findViewById(R.id.info_layout);
			holder.username = (TextView) convertView.findViewById(R.id.username);
			holder.indicate = (ImageView) convertView.findViewById(R.id.indicate);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_CONTACT:
			Contact user = items.get(position);
			holder.avatar.loadBuddyAvatar(Long.toString(user.getunique_id()));
			holder.username.setText(user.getusername());

			int selection = user.getFPinYin().charAt(0);
			int positionForSelection = getPositionForSelection(selection);
			if (position == positionForSelection) {
				holder.tv_tag.setVisibility(View.VISIBLE);
				holder.tv_tag.setText(user.getFPinYin());
			} else {
				holder.tv_tag.setVisibility(View.GONE);
			}

			if(inSelectedMembers(user.getAccount())){
				holder.indicate.setImageResource((R.drawable.nim_contact_checkbox_checked_green));
			}else{
				holder.indicate.setImageResource((R.drawable.nim_contact_checkbox_checked_grey));
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

	private boolean inSelectedMembers(String account){
		for(String id:selected){
			if(id.equals(account)){
				return true;
			}
		}
		return false;
	}

	public static class ViewHolder{
		public TextView tv_tag;
		public HeadImageView avatar;
		public LinearLayout info_layout;
		public TextView username;
		public ImageView indicate;
	}
	
	
	
}



