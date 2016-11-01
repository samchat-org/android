package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.android.samchat.common.FastBlurUtils;
import com.android.samservice.utils.S3Util;
import com.android.samchat.R;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.picker.activity.PickerAlbumActivity;
import com.netease.nim.uikit.common.media.picker.model.PhotoInfo;
import com.netease.nim.uikit.common.media.picker.model.PickerContract;
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
import com.netease.nim.uikit.session.constant.Extras;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class SamchatProfileCustomerActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatProfileCustomerActivity.class.getSimpleName();
	public static final int CONFIRM_ID_AVATAR_SELECTED=200;
	public static final int CONFIRM_ID_CROP_FINISHED=201;
	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 202;

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private ImageView wall_iv;
	private TextView countrycode_textview;
	private TextView phonenumber_textview;
	private TextView username_textview;
	private TextView email_textview;
	private TextView address_textview;

	private RelativeLayout phone_layout;
	private RelativeLayout email_layout;
	private RelativeLayout address_layout;
	private View countrycode_line;
	private View phonenumber_line;
	
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
				}else if(data.hasExtra(Extras.EXTRA_PHOTO_LISTS)){
					List<PhotoInfo> photoes = PickerContract.getPhotos(data);
					if(photoes !=null && photoes.size()>0 && photoes.get(0)!=null){
						try{
							startCropIntent(photoes.get(0).getAbsolutePath());
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

	private void updatePhoneLine(boolean ccExist){
		if(ccExist){
			countrycode_line.setVisibility(View.VISIBLE);
			phonenumber_line.setVisibility(View.GONE);
		}else{
			countrycode_line.setVisibility(View.GONE);
			phonenumber_line.setVisibility(View.VISIBLE);
		}
	}

    private void update(boolean afterCrop){
		/*if(afterCrop){
			//avatar_headimageview.setImageURI(cropImageUri);
			avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90);
		}else{
			avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90);
		}*/

		avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 
			(int) getResources().getDimension(R.dimen.samchat_avatar_size_in_namecard), new HeadImageView.OnImageLoadedListener(){
			@Override
			public void OnImageLoadedListener(Bitmap bitmap){
				FastBlurUtils.blur(bitmap, wall_iv);
			}
		});

		if(!TextUtils.isEmpty(SamService.getInstance().get_current_user().getcountrycode())){
			countrycode_textview.setVisibility(View.VISIBLE);
			countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode());
			updatePhoneLine(true);
		}else{
			countrycode_textview.setVisibility(View.GONE);
			updatePhoneLine(false);
		}
		phonenumber_textview.setText(SamService.getInstance().get_current_user().getcellphone());
		username_textview.setText(SamService.getInstance().get_current_user().getusername());
		email_textview.setText(SamService.getInstance().get_current_user().getemail());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress());
	}

	private void updateWithoutAvatar(){
		if(!TextUtils.isEmpty(SamService.getInstance().get_current_user().getcountrycode())){
			countrycode_textview.setVisibility(View.VISIBLE);
			countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode());
			updatePhoneLine(true);
		}else{
			countrycode_textview.setVisibility(View.GONE);
			updatePhoneLine(false);
		}
		phonenumber_textview.setText(SamService.getInstance().get_current_user().getcellphone());
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
		wall_iv = findView(R.id.wall);

		phone_layout = findView(R.id.phone_layout);
		email_layout = findView(R.id.email_layout);
		address_layout = findView(R.id.address_layout);

		countrycode_line = findView(R.id.countrycode_line);
		phonenumber_line = findView(R.id.phonenumber_line);

		setupBackArrowClick();
		setupAvaterClick();
		setupEmailClick();
		setupAddressClick();
		setupPhoneClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupPhoneClick(){
		phone_layout.setOnClickListener(new OnClickListener() {
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
				//launchAvatarActivity();
                launchAvatarSelectActivity();
        }
		});
	}

	private void setupEmailClick(){
		email_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_EMAIL, email_textview.getText().toString().trim());
			}
		});
	}

	private void setupAddressClick(){
		address_layout.setOnClickListener(new OnClickListener() {
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

	private void launchAvatarSelectActivity(){
		Intent intent = new Intent();
		intent.setClass(this, PickerAlbumActivity.class);
		intent.putExtra(Extras.EXTRA_MUTI_SELECT_MODE,false);
		intent.putExtra(Extras.EXTRA_MUTI_SELECT_SIZE_LIMIT,1);
		intent.putExtra(Extras.EXTRA_SUPPORT_ORIGINAL, false);
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
			LogUtil.e(TAG,"onError upload avatar customer");
			observer.cleanTransferListener();
			S3Util.getTransferUtility(SamchatProfileCustomerActivity.this).deleteTransferRecord(observer.getId());
			deleteFile();
			DialogMaker.dismissProgressDialog();
			EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileCustomerActivity.this, null,
                    			getString(R.string.samchat_upload_failed), getString(R.string.samchat_ok), true, null);
		}

		@Override
		public void onProgressChanged(int id, final long bytesCurrent, final long bytesTotal) {
			LogUtil.i(TAG,"onProgressChanged "+bytesCurrent+"/"+bytesTotal);
		}

		@Override
		public void onStateChanged(int id, TransferState newState) {
			LogUtil.e(TAG,"onStateChanged "+newState);
			if(newState == TransferState.COMPLETED) {
				observer.cleanTransferListener();
				S3Util.getTransferUtility(SamchatProfileCustomerActivity.this).deleteTransferRecord(observer.getId());
				String avatar_orig = NimConstants.S3_URL_UPLOAD+ NimConstants.S3_PATH_AVATAR+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
				ContactUser user = new ContactUser(SamService.getInstance().get_current_user());
				user.setavatar_original(avatar_orig);
				user.setavatar(null);
				updateAvatar(user);
			}else if(newState == TransferState.IN_PROGRESS){
				
			}else{
				observer.cleanTransferListener();
				S3Util.getTransferUtility(SamchatProfileCustomerActivity.this).deleteTransferRecord(observer.getId());
				deleteFile();
				DialogMaker.dismissProgressDialog();
				EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileCustomerActivity.this, null,
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