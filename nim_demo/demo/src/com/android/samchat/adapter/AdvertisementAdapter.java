package com.android.samchat.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.samchat.common.SamchatFileNameUtils;
import com.android.samservice.Constants;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.android.samservice.info.SendQuestion;

import android.os.Environment;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;
import com.netease.nim.uikit.common.ui.imageview.MsgThumbImageView;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;

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
			holder.content_image = (MsgThumbImageView) convertView.findViewById(R.id.content_image);
			holder.content_image_layout = (FrameLayout) convertView.findViewById(R.id.content_image_layout);
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
				holder.content_image_layout.setVisibility(View.GONE);
				holder.content_text.setText(adv.getcontent());
			}else if(adv.gettype() == Constants.ADV_TYPE_PIC){
				holder.content_text.setVisibility(View.GONE);
				holder.content_image_layout.setVisibility(View.VISIBLE);
				String extension = FileUtil.getExtensionName(adv.getcontent());
				String MD5Path = SamchatFileNameUtils.getMD5Path(StorageType.TYPE_THUMB_IMAGE,adv.getcontent_thumb());
				if(AttachmentStore.isFileExist(MD5Path)){
					//holder.content_image.setImageBitmap(BitmapDecoder.decodeSampled(MD5Path, (int)(ScreenUtil.screenWidth*0.8), (int)(ScreenUtil.screenWidth*0.8)));
					setImageSize(MD5Path,holder.content_image);
					holder.content_image.loadAsPath(MD5Path,MsgViewHolderThumbBase.getImageMaxEdge2(), MsgViewHolderThumbBase.getImageMaxEdge2(),R.drawable.nim_message_item_round_bg);
					/*int width = (int)(ScreenUtil.screenWidth * 1);
					ViewGroup.LayoutParams lp = holder.content_image.getLayoutParams();
					lp.width = width;
					lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
					holder.content_image.setLayoutParams(lp);

					holder.content_image.setMaxWidth(width);
					holder.content_image.setMaxHeight(width * 3);
					holder.content_image.load("file://"+MD5Path);*/
					LogUtil.i(TAG,"load MD5Path:"+MD5Path+" screenWidth:"+ScreenUtil.screenWidth);
				}
			}else{
				holder.content_text.setVisibility(View.GONE);
				holder.content_image_layout.setVisibility(View.VISIBLE);
				String extension = FileUtil.getExtensionName(adv.getcontent());
				String MD5Path = Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
								+StorageType.TYPE_THUMB_VIDEO.getStoragePath()+"/"+StringUtil.makeMd5(adv.getcontent_thumb());

				//holder.content_image.load("file://"+MD5Path);
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

	private void setImageSize(String thumbPath,View thumbnail) {
		int[] bounds = null;
		if (thumbPath != null) {
			bounds = BitmapDecoder.decodeBound(new File(thumbPath));
		}

		if (bounds != null) {
			ImageUtil.ImageSize imageSize = ImageUtil.getThumbnailDisplaySize(bounds[0], bounds[1], MsgViewHolderThumbBase.getImageMaxEdge2(), MsgViewHolderThumbBase.getImageMinEdge());
			ViewGroup.LayoutParams maskParams = thumbnail.getLayoutParams();
			maskParams.width = imageSize.width;
			maskParams.height = imageSize.height;
			thumbnail.setLayoutParams(maskParams);
		}
	}

	public static class ViewHolder{
		public TextView date;
		public TextView content_text;
		public MsgThumbImageView content_image;
		public FrameLayout content_image_layout;
	}
	
	
	
}





