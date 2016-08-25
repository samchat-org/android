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
import com.netease.nimlib.sdk.msg.model.IMMessage;

public class SendQuestionAdapter extends BaseAdapter{
	static private String TAG = "SendQuestionAdapter";
	
	private final int TYPE_REQUEST = 0;
	private final int TYPE_LABEL_ACTIVE = 1;
	private final int TYPE_LABEL_HISTORY = 2;
	private final int TYPE_MAX = TYPE_LABEL_HISTORY + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<SendQuestion> items;
	//-1: not history   0:no active   others:active and history both existed
	private int history;

	public SendQuestionAdapter(Context context,List<SendQuestion> list){
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		items = list;
		history = -1;
	}
	
	@Override
	public int getCount(){
		if(items ==  null || items.size() == 0){
			return 0;
		}else{
			if(history == -1){
				return items.size() + 1;
			}else if(history == 0){
				return items.size() + 1;
			}else{
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
		if(history == -1){
			if(position == 0){
				return TYPE_LABEL_ACTIVE;
			}else{
				return TYPE_REQUEST;
			}
		}else if(history == 0){
			if(position == 0){
				return TYPE_LABEL_HISTORY;
			}else{
				return TYPE_REQUEST;
			}
		}else{
			if(position == 0){
				return TYPE_LABEL_ACTIVE;
			}else if(position - 1 == history){
				return TYPE_LABEL_HISTORY;
			}else{
				return TYPE_REQUEST;
			}
		}
	}

	private boolean isActiveLabel(int position){
		if(history == -1){
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}else if(history == 0){
			return false;
		}else{
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}
	}

	private boolean isHistoryLabel(int position){
		if(history == -1){
			return false;
		}else if(history == 0){
			if(position == 0){
				return true;
			}else{
				return false;
			}
		}else{
			if(position == 0){
				return false;
			}else if(position == history + 1){
				return true;
			}else{
				return false;
			}
		}
	}

	private int positionToIndex(int position){
		if(history == -1){
			return position -1;
		}else if(history == 0){
			return position -1;
		}else{
			if(position <= history){
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
			if(isActiveLabel(position) || isHistoryLabel(position)){
				convertView = mInflater.inflate(R.layout.samchat_label_view,parent,false);
				holder.label = (TextView) convertView.findViewById(R.id.label);
			}else{
				convertView = mInflater.inflate(R.layout.samchat_send_question_list_item,parent,false);
				holder.request = (TextView) convertView.findViewById(R.id.request);
				holder.number = (TextView) convertView.findViewById(R.id.number);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.location = (TextView) convertView.findViewById(R.id.location);
			}
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_REQUEST:
			int index = positionToIndex(position);
			holder.request.setText(items.get(index).getquestion());
			long showtime = items.get(index).getlatest_answer_time()==0?items.get(index).getdatetime():items.get(index).getlatest_answer_time();
			holder.date.setText(TimeUtil.getTimeShowString(showtime,false));
			holder.location.setText(items.get(index).getaddress());
			holder.number.setText(items.get(index).getunread()+" "+ mContext.getString(R.string.samchat_new_response));
			break;
		case TYPE_LABEL_ACTIVE:
			holder.label.setText(mContext.getString(R.string.active_requests));
			break;
		case TYPE_LABEL_HISTORY:
			holder.label.setText(mContext.getString(R.string.history));
			break;
		}
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public SendQuestion getItem(int position){
		if(isActiveLabel(position) || isHistoryLabel(position)){
			return null;
		}else{
			return items.get(positionToIndex(position));
		}
	}

	public void sethistory(int id){
		this.history = id;
	}


	
	public static class ViewHolder{
		//send question view
		public TextView request;
		public TextView number;
		public TextView date;
		public TextView location;
		//label view
		public TextView label;
	}
	
	
	
}


