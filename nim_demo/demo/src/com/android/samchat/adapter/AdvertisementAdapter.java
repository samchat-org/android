package com.android.samchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.android.samservice.Constants;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.android.samservice.info.SendQuestion;

import android.os.Environment;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import android.widget.ImageView;

public class AdvertisementAdapter extends BaseAdapter{
	static private String TAG = "SamchatAdvertisementAdapter";
	
	private final int TYPE_ADV = 0;
	private final int TYPE_MAX = TYPE_ADV + 1;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Advertisement> items;

	public AdvertisementAdapter(Context context, List<Advertisement> list){
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
		return TYPE_ADV;
	}
	
	@Override
	public View getView(int position,View convertView, ViewGroup parent){
		int viewType = getItemViewType(position);
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.samchat_advertisement_list_item,parent,false);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.content_text = (TextView) convertView.findViewById(R.id.content_text);
			holder.content_image = (ImageViewEx) convertView.findViewById(R.id.content_image);

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		switch(viewType){
		case TYPE_ADV:
			Advertisement adv = items.get(position);
			holder.date.setText(TimeUtil.getTimeShowString(adv.getpublish_timestamp(),false));
			if(adv.gettype() == Constants.ADV_TYPE_TEXT){
				holder.content_text.setVisibility(View.VISIBLE);
				holder.content_image.setVisibility(View.GONE);
				holder.content_text.setText(adv.getcontent());
			}else if(adv.gettype() == Constants.ADV_TYPE_PIC){
				holder.content_text.setVisibility(View.GONE);
				holder.content_image.setVisibility(View.VISIBLE);
				String extension = FileUtil.getExtensionName(adv.getcontent());
				String MD5Path = Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
								+StorageType.TYPE_THUMB_IMAGE.getStoragePath()+"/"+StringUtil.makeMd5(adv.getcontent_thumb());
				holder.content_image.load("file://"+MD5Path);
			}else{
				holder.content_text.setVisibility(View.GONE);
				holder.content_image.setVisibility(View.VISIBLE);
				String extension = FileUtil.getExtensionName(adv.getcontent());
				String MD5Path = Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
								+StorageType.TYPE_THUMB_VIDEO.getStoragePath()+"/"+StringUtil.makeMd5(adv.getcontent_thumb());
				holder.content_image.load("file://"+MD5Path);
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
	public Advertisement getItem(int position){
		return items.get(position);
	}

	public static class ViewHolder{
		public TextView date;
		public TextView content_text;
		public ImageViewEx content_image;
	}
	
	
	
}





