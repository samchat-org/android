package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.netease.nim.uikit.common.util.log.LogUtil;
/**
 * Created by huangjun on 2015/11/13.
 */
public class HeadImageView extends CircleImageView {

    public static final int DEFAULT_AVATAR_THUMB_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_max_size);
    public static final int DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_notification_size);

    private DisplayImageOptions options = createImageOptions();

    private static final DisplayImageOptions createImageOptions() {
        int defaultIcon = NimUIKit.getUserInfoProvider().getDefaultIconResId();
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultIcon)
                .showImageOnFail(defaultIcon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public HeadImageView(Context context) {
        super(context);
    }

    public HeadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param account
     */
    public void loadBuddyAvatar(String account) {
        loadBuddyAvatar(account, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载用户头像（原图）
     *
     * @param account
     */
    public void loadBuddyOriginalAvatar(String account) {
        loadBuddyAvatar(account, 0);
    }

    /**
     * 加载用户头像（指定缩略大小）
     *
     * @param account
     * @param thumbSize 缩略图的宽、高
     */
	public void loadBuddyAvatar(final String account, final int thumbSize) {
		try{
			// 先显示默认头像
			//setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());

			// 判断是否需要ImageLoader加载
			final UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
			boolean needLoad = userInfo != null && ImageLoaderKit.isImageUriValid(userInfo.getAvatar());

			doLoadImage(needLoad, account, userInfo != null ? userInfo.getAvatar() : null, thumbSize,NimUIKit.getUserInfoProvider().getDefaultIconResId());
		}catch(Exception e){
			e.printStackTrace(); 
		}
    }

    public void loadBuddyAvatarByUrl(final String account, final String path, final int thumbSize) {
		try{
			boolean needLoad = path != null && ImageLoaderKit.isImageUriValid(path);
			doLoadImage(needLoad, account, path != null ? path : null, thumbSize,NimUIKit.getUserInfoProvider().getDefaultIconResId());
		}catch(Exception e){
			e.printStackTrace(); 
		}
    }

	public void loadBuddyAvatarByUrl(final String account,final String path, final int thumbSize, final OnImageLoadedListener callback) {
		try{
			boolean needLoad = path != null && ImageLoaderKit.isImageUriValid(path);
			doLoadImage(needLoad, account, path != null ? path : null, thumbSize,NimUIKit.getUserInfoProvider().getDefaultIconResId(),callback);
		}catch(Exception e){
			e.printStackTrace(); 
		}
    }

	public interface OnImageLoadedListener {
		public void OnImageLoadedListener(Bitmap bitmap);
	}

	public void loadBuddyAvatar(final String account, final int thumbSize, final OnImageLoadedListener callback) {
		try{
			// 先显示默认头像
			//setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());

			// 判断是否需要ImageLoader加载
			final UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
			boolean needLoad = userInfo != null && ImageLoaderKit.isImageUriValid(userInfo.getAvatar());

			doLoadImage(needLoad, account, userInfo != null ? userInfo.getAvatar() : null, thumbSize,NimUIKit.getUserInfoProvider().getDefaultIconResId(),callback);
		}catch(Exception e){
			e.printStackTrace(); 
		}
    }

	private void doLoadImage(final boolean needLoad, final String tag, final String url, final int thumbSize,final int default_resid,final OnImageLoadedListener callback) {
		if (needLoad) {
			setTag(tag); // 解决ViewHolder复用问题
            /**
             * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
             * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
             */
            final String thumbUrl = makeAvatarThumbNosUrl(url, thumbSize);

            // 异步从cache or NOS加载图片
			ImageLoader.getInstance().displayImage(thumbUrl, new NonViewAware(new ImageSize(thumbSize, thumbSize),
				ViewScaleType.CROP), options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					if (getTag() != null && getTag().equals(tag)) {
						setImageBitmap(loadedImage);
						callback.OnImageLoadedListener(loadedImage);
					}
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					if (getTag() != null && getTag().equals(tag)){	
						setImageResource(default_resid);
					}
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					if (getTag() != null && getTag().equals(tag)){	
						setImageResource(default_resid);
					}
				}
			});
		} else {
			setImageResource(default_resid);
			setTag(null);
		}
    }

	public void loadTeamIcon(String tid) {
		try{
			Bitmap bitmap = NimUIKit.getUserInfoProvider().getTeamIcon(tid);
			setImageBitmap(bitmap);
		}catch(Exception e){
			e.printStackTrace(); 
		}
	}

	public void loadTeamIconByTeam(final Team team) {
		try{
			// 先显示默认头像
			setImageResource(R.drawable.nim_avatar_group);
			// 判断是否需要ImageLoader加载
			boolean needLoad = team != null && ImageLoaderKit.isImageUriValid(team.getIcon());
			doLoadImage(needLoad, team != null ? team.getId() : null, team.getIcon(), DEFAULT_AVATAR_THUMB_SIZE,R.drawable.nim_avatar_group);
		}catch(Exception e){
			e.printStackTrace(); 
		}
    }

    /**
     * ImageLoader异步加载
     */
	private void doLoadImage(final boolean needLoad, final String tag, final String url, final int thumbSize, final int default_resid) {
		if (needLoad) {
			setTag(tag); // 解决ViewHolder复用问题
			/**
				* 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
				* 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
			*/
			final String thumbUrl = makeAvatarThumbNosUrl(url, thumbSize);

			// 异步从cache or NOS加载图片
			ImageLoader.getInstance().displayImage(thumbUrl, new NonViewAware(new ImageSize(thumbSize, thumbSize),
					ViewScaleType.CROP), options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					if (getTag() != null && getTag().equals(tag)) {
						setImageBitmap(loadedImage);
					}
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					if (getTag() != null && getTag().equals(tag)){	
						setImageResource(default_resid);
					}
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					if (getTag() != null && getTag().equals(tag)){	
						setImageResource(default_resid);
					}
				}
			});
		} else {
			setImageResource(default_resid);
			setTag(null);
		}
	}

    /**
     * 解决ViewHolder复用问题
     */
    public void resetImageView() {
        setImageBitmap(null);
    }

    /**
     * 生成头像缩略图NOS URL地址（用作ImageLoader缓存的key）
     */
    private static String makeAvatarThumbNosUrl(final String url, final int thumbSize) {
        //return thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url, NosThumbParam.ThumbType.Crop, thumbSize, thumbSize) : url;
        return url;
    }

    public static String getAvatarCacheKey(final String url) {
        return makeAvatarThumbNosUrl(url, DEFAULT_AVATAR_THUMB_SIZE);
    }
}
