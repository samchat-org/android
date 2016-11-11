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

public class SamchatProfileServiceProviderActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatProfileServiceProviderActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private ImageView wall_iv;
	private TextView company_name_textview;
	private TextView service_category_textview;
	private TextView service_description_textview;
	private TextView countrycode_textview;
	private TextView phonenumber_textview;
	private TextView email_textview;
	private TextView address_textview;

	private RelativeLayout company_layout;
	private RelativeLayout service_category_layout;
	private RelativeLayout service_description_layout;
	private RelativeLayout phone_layout;
	private RelativeLayout email_layout;
	private RelativeLayout address_layout;

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

		if(!TextUtils.isEmpty(SamService.getInstance().get_current_user().getcountrycode_sp())){
			countrycode_textview.setVisibility(View.VISIBLE);
			countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode_sp());
		}else{
			countrycode_textview.setVisibility(View.GONE);
		}
		phonenumber_textview.setText(SamService.getInstance().get_current_user().getphone_sp());
		company_name_textview.setText(SamService.getInstance().get_current_user().getcompany_name());
		service_category_textview.setText(SamService.getInstance().get_current_user().getservice_category());
		service_description_textview.setText(SamService.getInstance().get_current_user().getservice_description());
		email_textview.setText(SamService.getInstance().get_current_user().getemail_sp());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress_sp());
	}

	private void updateWithoutAvatar(){
		if(!TextUtils.isEmpty(SamService.getInstance().get_current_user().getcountrycode_sp())){
			countrycode_textview.setVisibility(View.VISIBLE);
			countrycode_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode_sp());
		}else{
			countrycode_textview.setVisibility(View.GONE);
		}
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
		wall_iv = findView(R.id.wall);
		countrycode_textview = findView(R.id.countrycode);
		phonenumber_textview= findView(R.id.phonenumber);
		company_name_textview = findView(R.id.company_name);
		service_category_textview = findView(R.id.service_category);
		service_description_textview = findView(R.id.service_description);
		email_textview= findView(R.id.email);
		address_textview= findView(R.id.address);

		company_layout = findView(R.id.company_layout);
		service_category_layout = findView(R.id.service_category_layout);
		service_description_layout = findView(R.id.service_description_layout);
		phone_layout = findView(R.id.phone_layout);
		email_layout = findView(R.id.email_layout);
		address_layout = findView(R.id.address_layout);

		setupBackArrowClick();
		setupCompanyNameClick();
		setupServiceCategoryClick();
		setupServiceDescriptionClick();
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
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_PHONE, 
					SamService.getInstance().get_current_user().getphone_sp());
			}
		});
	}

	private void setupCompanyNameClick(){
		company_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, 
					SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_COMPANY_NAME, company_name_textview.getText().toString().trim());
			}
		});
	}

	private void setupServiceCategoryClick(){
		service_category_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY, service_category_textview.getText().toString().trim());
			}
		});
	}

	private void setupServiceDescriptionClick(){
		service_description_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION, service_description_textview.getText().toString().trim());
			}
		});
	}

	private void setupEmailClick(){
		email_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_EMAIL, email_textview.getText().toString().trim());
			}
		});
	}

	private void setupAddressClick(){
		address_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileServiceProviderActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_SP_ADDRESS, address_textview.getText().toString().trim());
			}
		});
	}

}




