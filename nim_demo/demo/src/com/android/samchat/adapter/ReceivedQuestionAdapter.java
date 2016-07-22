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
import com.android.samservice.info.ReceivedQuestion;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;

public class ReceivedQuestionAdapter extends BaseAdapter{
	static private String TAG = "ReceivedQuestionAdapter";
	
	private final int TYPE_REQUEST = 0;
	private final int TYPE_LABEL_NEW = 1;
	private final int TYPE_LABEL_ANSWERED = 2;
	private final int TYPE_MAX = TYPE_LABEL_ANSWERED + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ReceivedQuestion> items;
	//-1: no answered    0: no new    others:answered and new both existed
	private int answered;

	public ReceivedQuestionAdapter(Context context,List<ReceivedQuestion> list){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
		answered = -1;
	}

	public void setanswered(int ai){
		answered = ai;
	}
	
	@Override
	public int getCount(){
		if(items == null || items.size() == 0){
			LogUtil.e("test","getCount is 0");
			return 0;
		}else{
			if(answered == -1){
				LogUtil.e("test","getCount is " + items.size() + 1);
				return items.size() + 1;
			}else if(answered == 0){
				LogUtil.e("test","getCount is " + items.size() + 1);
				return items.size() + 1;
			}else{
				LogUtil.e("test","getCount is " + items.size() + 2);
				return items.size() + 2;
			}
		}
	}
	
	@Override
	public int getViewTypeCount(){
		return TYPE_MAX;
	}
	
	@Override
	public int getItemViewType(int position){
		if(answered == -1){
			if(position == 0){
				return TYPE_LABEL_NEW;
			}else{
				return TYPE_REQUEST;
			}
		}else if(answered == 0){
			if(position == 0){
				return TYPE_LABEL_ANSWERED;
			}else{
				return TYPE_REQUEST;
			}
		}else{
			if(position == 0){
				return TYPE_LABEL_NEW;
			}else if(position == answered + 1){
				return TYPE_LABEL_ANSWERED;
			}else{
				return TYPE_REQUEST;
			}
		}
	}

		private boolean isNewLabel(int position){
		if(answered == -1){
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}else if(answered == 0){
			return false;
		}else{
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}
	}

	private boolean isAnsweredLabel(int position){
		if(answered == -1){
			return false;
		}else if(answered == 0){
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}else{
			if(position == 0){
				return false;
			}else if(position == answered + 1){
				return true;
			}else{
				return false;
			}
		}
	}

	private int positionToIndex(int position){
		if(answered == -1){
			return position -1;
		}else if(answered == 0){
			return position -1;
		}else{
			if(position <= answered){
				return position - 1;
			}else{
				return position - 2;
			}
		}
	}

	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			if(isNewLabel(position) || isAnsweredLabel(position)){
				convertView = mInflater.inflate(R.layout.samchat_label_view,parent,false);
				holder.label = (TextView) convertView.findViewById(R.id.label);
			}else{
				convertView = mInflater.inflate(R.layout.samchat_received_question_list_item,parent,false);
				holder.request = (TextView) convertView.findViewById(R.id.request);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.location = (TextView) convertView.findViewById(R.id.location);
				holder.username = (TextView) convertView.findViewById(R.id.username);
			}
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_REQUEST:
			int index = positionToIndex(position);
			holder.request.setText(items.get(index).getquestion());
			long showtime = items.get(index).getdatetime();
			holder.date.setText(TimeUtil.getDateString(showtime));
			holder.location.setText(items.get(index).getaddress());
			holder.username.setText(items.get(index).getsender_unique_id()+"");
			break;
		case TYPE_LABEL_NEW:
			holder.label.setText(mContext.getString(R.string.rq_new));
			break;
		case TYPE_LABEL_ANSWERED:
			holder.label.setText(mContext.getString(R.string.rq_answered));
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public ReceivedQuestion getItem(int position){
		if(isNewLabel(position) || isAnsweredLabel(position)){
			return null;
		}else{
			return items.get(positionToIndex(position));
		}
	}
	
	public static class ViewHolder{
		//received question view
		public TextView request;
		public TextView date;
		public TextView location;
		public TextView username;
		//label view
		public TextView label;
	}
	
	
	
}



