package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.android.samchat.R;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import com.android.samservice.info.ContactUser;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class SamchatProfileServiceProviderActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatProfileServiceProviderActivity.class.getSimpleName();

	public static final int CONFIRM_ID_AVATAR_SELECTED=200;
	public static final int CONFIRM_ID_CROP_FINISHED=201;

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private TextView company_name_textview;
	private TextView service_category_textview;
	private TextView service_description_textview;
	private TextView countrycode_textview;
	private TextView phonenumber_textview;
	private TextView email_textview;
	private TextView address_textview;

	private Uri cropImageUri;
	private TransferUtility transferUtility;

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatProfileServiceProviderActivity.class);
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
		setContentView(R.layout.samchat_profileserviceprovider_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();
		transferUtility = S3Util.getTransferUtility(this);
		update(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume(){
		super.onResume();
		updateWithoutAvatar();
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
				if(bitmap != null && AttachmentStore.saveBitmap(bitmap, getAvatarFilePath(), true)){
					LogUtil.e(TAG,"createAvatarFile OK");
					uploadAvatar(getAvatarFilePath());
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
		countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode_sp());
		phonenumber_textview.setText(SamService.getInstance().get_current_user().getphone_sp());
		company_name_textview.setText(SamService.getInstance().get_current_user().getcompany_name());
		service_category_textview.setText(SamService.getInstance().get_current_user().getservice_category());
		service_description_textview.setText(SamService.getInstance().get_current_user().getservice_description());
		email_textview.setText(SamService.getInstance().get_current_user().getemail_sp());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress_sp());
	}

	private void updateWithoutAvatar(){
		countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode_sp());
		phonenumber_textview.setText(SamService.getInstance().get_current_user().getphone_sp());
		company_name_textview.setText(SamService.getInstance().get_current_user().getcompany_name());
		service_category_textview.setText(SamService.getInstance().get_current_user().getservice_category());
		service_description_textview.setText(SamService.getInstance().get_current_user().getservice_description());
		email_textview.setText(SamService.getInstance().get_current_user().getemail_sp());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress_sp());
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		avatar_headimageview= findView(R.id.avatar);
		countrycode_textview = findView(R.id.countrycode);
		phonenumber_textview= findView(R.id.phonenumber);
		company_name_textview = findView(R.id.company_name);
		service_category_textview = findView(R.id.service_category);
		service_description_textview = findView(R.id.service_description);
		email_textview= findView(R.id.email);
		address_textview= findView(R.id.address);

		setupBackArrowClick();
		setupCompanyNameClick();
		setupServiceCategoryClick();
		setupServiceDescriptionClick();
		setupEmailClick();
		setupAddressClick();
		setupCountryCodeClick();
		setupPhoneNumberClick();
		setupAvaterClick();

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
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_PHONE, 
					SamService.getInstance().get_current_user().getcountrycode_sp(),
					SamService.getInstance().get_current_user().getphone_sp());
			}
		});
	}

	private void setupPhoneNumberClick(){
		phonenumber_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_PHONE, 
					SamService.getInstance().get_current_user().getcountrycode_sp(),
					SamService.getInstance().get_current_user().getphone_sp());
			}
		});
	}

	private void setupCompanyNameClick(){
		company_name_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, 
					SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_COMPANY_NAME, company_name_textview.getText().toString().trim());
			}
		});
	}

	private void setupServiceCategoryClick(){
		service_category_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY, service_category_textview.getText().toString().trim());
			}
		});
	}

	private void setupServiceDescriptionClick(){
		service_description_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION, service_description_textview.getText().toString().trim());
			}
		});
	}

	private void setupEmailClick(){
		email_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_EMAIL, email_textview.getText().toString().trim());
			}
		});
	}

	private void setupAddressClick(){
		address_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_ADDRESS, address_textview.getText().toString().trim());
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
			S3Util.getTransferUtility(SamchatProfileServiceProviderActivity.this).deleteTransferRecord(observer.getId());
		}

		@Override
		public void onProgressChanged(int id, final long bytesCurrent, final long bytesTotal) {

		}

		@Override
		public void onStateChanged(int id, TransferState newState) {
			if(newState == TransferState.COMPLETED) {
				observer.cleanTransferListener();
				S3Util.getTransferUtility(SamchatProfileServiceProviderActivity.this).deleteTransferRecord(observer.getId());
				String avatar_orig = NimConstants.S3_URL_UPLOAD + NimConstants.S3_PATH_AVATAR+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
				ContactUser user = new ContactUser(SamService.getInstance().get_current_user());
				user.setavatar_original(avatar_orig);
				user.setavatar(null);
				updateAvatar(user);
			}else if(newState == TransferState.IN_PROGRESS){
				
			}else{
				observer.cleanTransferListener();
				S3Util.getTransferUtility(SamchatProfileServiceProviderActivity.this).deleteTransferRecord(observer.getId());
				deleteFile();
				DialogMaker.dismissProgressDialog();
				EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileServiceProviderActivity.this, null,
                    			getString(R.string.samchat_upload_failed), getString(R.string.samchat_ok), true, null);
			}
		}
	}

	private void deleteFile(){
		String path = getAvatarFilePath();
		AttachmentStore.deleteIfExist(path);
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
					deleteFile();
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
					final ErrorString error = new ErrorString(SamchatProfileServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					deleteFile();
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatProfileServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}
		});
	}

	

}




