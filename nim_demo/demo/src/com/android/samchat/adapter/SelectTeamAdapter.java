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
import com.netease.nimlib.sdk.team.model.Team;

public class SelectTeamAdapter extends BaseAdapter{
	static private String TAG = "SelectTeamAdapter";
	
	private final int TYPE_TEAM = 0;
	private final int TYPE_MAX = TYPE_TEAM + 1;

	private Context mContext;
	private LayoutInflater mInflater;
	private List<Team> items;
	public SelectTeamAdapter(Context context,List<Team> list){
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
		return TYPE_TEAM;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_team_list_item,parent,false);
			holder.avatar= (HeadImageView) convertView.findViewById(R.id.avatar);
			holder.name = (TextView) convertView.findViewById(R.id.name);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_TEAM:
			Team team = items.get(position);
			holder.avatar.loadTeamIcon(team.getId());
			holder.name.setText(team.getName());
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public Team getItem(int position){
		return items.get(position);
	}
	
	public static class ViewHolder{
		public HeadImageView avatar;
		public TextView name;
	}
	
	
	
}




