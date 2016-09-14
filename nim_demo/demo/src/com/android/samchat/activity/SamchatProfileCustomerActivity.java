package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.android.samservice.utils.S3Util;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.android.samchat.service.SamDBManager;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.info.ContactUser;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class SamchatProfileCustomerActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatProfileCustomerActivity.class.getSimpleName();
	public static final int CONFIRM_ID_AVATAR_SELECTED=200;
	public static final int CONFIRM_ID_CROP_FINISHED=201;
	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 202;

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private TextView countrycode_textview;
	private TextView phonenumber_textview;
	private TextView username_textview;
	private TextView email_textview;
	private TextView address_textview;

	private Uri cropImageUri;
	
	private TransferUtility transferUtility;

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatProfileCustomerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	protected boolean displayHomeAsUpEnabled() {
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_profilecustomer_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

		transferUtility = S3Util.getTransferUtility(this);

		update(false);

	}

	@Override
	public void onResume(){
		super.onResume();
		updateWithoutAvatar();
	}

	@Override
    protected void onPause() {
        super.onPause();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode == CONFIRM_ID_AVATAR_SELECTED){
			if(resultCode == Activity.RESULT_OK){
				if( data.hasExtra(MultiImageSelectorActivity.EXTRA_RESULT)) {
					List<String> resultList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
					if(resultList.get(0)!=null){
						try{
							startCropIntent(resultList.get(0));
						}catch(IOException e){
							e.printStackTrace();
							Toast.makeText(this, R.string.samchat_start_crop_window_failed, Toast.LENGTH_SHORT).show();
							cropImageUri = null;
						}
					}
				}
			}
		}else if(requestCode == CONFIRM_ID_CROP_FINISHED){
			if(resultCode == Activity.RESULT_OK){
				LogUtil.e(TAG,"crop image successfully :" + cropImageUri);
				Bitmap bitmap =decodeUriAsBitmap(cropImageUri);
				LogUtil.e(TAG,"decodeUriAsBitmap :" + bitmap);
				if(bitmap != null && createAvatarFile(bitmap)){
					LogUtil.e(TAG,"createAvatarFile OK");
					uploadAvatar(getAvatarFilePath());
					bitmap.recycle();
				}
			}
		}
	}

	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	private String getAvatarFilePath() {
    	return StorageUtil.getWritePath(
					NimUIKit.getContext(),StringUtil.makeMd5("avatar_"+SamService.getInstance().get_current_user().getunique_id()),
					StorageType.TYPE_IMAGE);
    }

	private boolean createAvatarFile(Bitmap bitmap){
		String avatarPath = getAvatarFilePath();
		FileOutputStream fOut = null;
		try{
            File avatar = new File(avatarPath);
            fOut = new FileOutputStream(avatar);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            try {
                if(fOut != null)
                    fOut.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCropIntent(String path) throws IOException {
        String cropImage = StorageUtil.getWritePath(NimUIKit.getContext(),"temp_image_crop",StorageType.TYPE_TEMP);
        File cropFile = new File(cropImage);

        if(!cropFile.exists()){
            cropFile.createNewFile();
        }

        cropImageUri = Uri.fromFile(cropFile);

        File file = new File(path);
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri uri = Uri.fromFile(file);// parse(pathUri);13
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", Constants.AVATAR_PIC_MAX);
        intent.putExtra("outputY", Constants.AVATAR_PIC_MAX);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);//black scale
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, CONFIRM_ID_CROP_FINISHED);
    }


    private void update(boolean afterCrop){
		if(afterCrop){
			avatar_headimageview.setImageURI(cropImageUri);
		}else{
			avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90);
		}
		String countrycode = "+" + SamService.getInstance().get_current_user().getcountrycode();
		String phonenumber = SamService.getInstance().get_current_user().getcellphone();
		countrycode_textview.setText(countrycode);
		phonenumber_textview.setText(phonenumber);
		username_textview.setText(SamService.getInstance().get_current_user().getusername());
		email_textview.setText(SamService.getInstance().get_current_user().getemail());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress());
	}

	private void updateWithoutAvatar(){
		String countrycode = "+" + SamService.getInstance().get_current_user().getcountrycode();
		String phonenumber = SamService.getInstance().get_current_user().getcellphone();
		countrycode_textview.setText(countrycode);
		phonenumber_textview.setText(phonenumber);
		username_textview.setText(SamService.getInstance().get_current_user().getusername());
		email_textview.setText(SamService.getInstance().get_current_user().getemail());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress());
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		avatar_headimageview= findView(R.id.avatar);
		countrycode_textview = findView(R.id.countrycode);
		phonenumber_textview= findView(R.id.phonenumber);
		username_textview= findView(R.id.username);
		email_textview= findView(R.id.email);
		address_textview= findView(R.id.address);

		setupBackArrowClick();
		setupAvaterClick();
		setupEmailClick();
		setupAddressClick();
		setupCountryCodeClick();
		setupPhoneNumberClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupCountryCodeClick(){
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_PHONE, 
					SamService.getInstance().get_current_user().getcountrycode(),
					SamService.getInstance().get_current_user().getcellphone());
			}
		});
	}

	private void setupPhoneNumberClick(){
		phonenumber_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_PHONE, 
					SamService.getInstance().get_current_user().getcountrycode(),
					SamService.getInstance().get_current_user().getcellphone());
			}
		});
	}

	private void setupAvaterClick(){
		avatar_headimageview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				launchAvatarActivity();
			}
		});
	}

	private void setupEmailClick(){
		email_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_EMAIL, email_textview.getText().toString().trim());
			}
		});
	}

	private void setupAddressClick(){
		address_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS, address_textview.getText().toString().trim());
			}
		});
	}

	private void launchAvatarActivity(){
		Intent intent = new Intent(this, MultiImageSelectorActivity.class);
		// whether show camera
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
		// max select image amount
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
		// select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
		// default select images (support array list)
		//intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
		startActivityForResult(intent, CONFIRM_ID_AVATAR_SELECTED);
	}


/************************************Data Flow Control******************************************************/
	private class UploadListener implements TransferListener {
		public String s3name_origin;
		public TransferObserver observer;
		
		public UploadListener(String s3name_origin,TransferObserver observer){
			this.s3name_origin = s3name_origin;
			this.observer = observer;
		}

		// Simply updates the UI list when notified.
		@Override
		public void onError(int id, Exception e) {
			observer.cleanTransferListener();
		}

		@Override
		public void onProgressChanged(int id, final long bytesCurrent, final long bytesTotal) {

		}

		@Override
		public void onStateChanged(int id, TransferState newState) {
			LogUtil.e("test","onStateChanged "+newState);
			if(newState == TransferState.COMPLETED) {
				Toast.makeText(SamchatProfileCustomerActivity.this, "upload avatar finished", Toast.LENGTH_SHORT).show();
				observer.cleanTransferListener();
				String avatar_orig = NimConstants.S3_URL + NimConstants.S3_PATH_AVATAR+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
				LogUtil.e("test","upload avatar succeed: "+avatar_orig);
				ContactUser user = new ContactUser(SamService.getInstance().get_current_user());
				user.setavatar_original(avatar_orig);
				user.setavatar(null);
				updateAvatar(user);
			}else if(newState == TransferState.FAILED){
				observer.cleanTransferListener();
				deleteFile();
				DialogMaker.dismissProgressDialog();
				EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileCustomerActivity.this, null,
                    			getString(R.string.samchat_upload_failed), getString(R.string.samchat_ok), true, null);
			}
		}
	}

	private void deleteFile(){
		String path = getAvatarFilePath();
		File file = new File(path);
		file.delete();
	}
	
	private void uploadAvatar(String path){
		String s3name_orig = "orig_"+SamService.getInstance().get_current_user().getunique_id()+"_"+ TimeUtil.currentTimeMillis()+".jpg";
		String key = NimConstants.S3_PATH_AVATAR+NimConstants.S3_FOLDER_ORIGIN+s3name_orig;
		TransferObserver observer = transferUtility.upload(NimConstants.S3_BUCKETNAME,key,new File(path));
		observer.setTransferListener(new UploadListener(s3name_orig,observer));
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_upload_avatar), false, null).setCanceledOnTouchOutside(false);
	}

	private void updateAvatar(ContactUser user){
		SamService.getInstance().update_avatar(user, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							update(true);
							Intent intent = new Intent();
							intent.setAction(Constants.BROADCAST_MYSELF_AVATAR_UPDATE);
							SamService.getInstance().sendbroadcast(intent);
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					deleteFile();
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatProfileCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					deleteFile();
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatProfileCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}
		});
	}
}



