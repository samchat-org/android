package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samservice.SamService;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PhoneContact;
import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hp.hpl.sparta.Text;
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
			holder.tv_tag= (TextView) convertView.findViewById(R.id.tv_lv_item_tag);
			holder.name = (TextView) convertView.findViewById(R.id.name);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_PHONECONTACT:
			PhoneContact user = items.get(position);
			holder.name.setText(user.getname());
			int selection = user.getFPinYin().charAt(0);
			int positionForSelection = getPositionForSelection(selection);
			if (position == positionForSelection) {
				holder.tv_tag.setVisibility(View.VISIBLE);
				holder.tv_tag.setText(user.getFPinYin());
			} else {
				holder.tv_tag.setVisibility(View.GONE);
			}
			break;
		}
		
		return convertView;
	}

	private int getPositionForSelection(int selection) {
		for (int i = 0; i < items.size(); i++) {
			String Fpinyin = items.get(i).getFPinYin();
			char first = Fpinyin.toUpperCase().charAt(0);
			if (first == selection) {
				return i;
			}
		}
		return -1;

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
		public TextView name;
		public TextView tv_tag;
	}
	
}





