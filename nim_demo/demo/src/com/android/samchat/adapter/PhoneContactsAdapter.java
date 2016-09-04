package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PhoneContact;
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

public class PhoneContactsAdapter extends BaseAdapter{
	static private String TAG = "SamchatPhoneContactsAdapter";
	
	private final int TYPE_PHONECONTACT = 0;
	private final int TYPE_MAX = TYPE_PHONECONTACT + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
    protected List<PhoneContact> items;

	public PhoneContactsAdapter(Context context,List<PhoneContact> list){
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
		return TYPE_PHONECONTACT;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_phonecontact_list_item,parent,false);
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.name = (TextView) convertView.findViewById(R.id.name);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_PHONECONTACT:
			holder.name.setText(items.get(position).getname());
			holder.avatar.setImageBitmap(items.get(position).getavatar());
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public PhoneContact getItem(int position){
		return items.get(position);
	}

	public static class ViewHolder{
		public HeadImageView avatar;
		public TextView name;
	}
	
}





