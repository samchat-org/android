package com.android.samchat.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.ImageView;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.android.samservice.info.ContactUser;
public class SamchatCreateSPStepTwoActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCreateSPStepTwoActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private FrameLayout right_button_layout;
	private HeadImageView avatar_headimageview;
	private EditText cellphone_edittext;
	private EditText email_edittext;
	private EditText address_edittext;

	private ContactUser info;
	private String cellphone=null;
	private String email=null;
	private String address=null;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private boolean isCreating = false;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatCreateSPStepTwoActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_CREATE_SP_SUCCESS);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_CREATE_SP_SUCCESS)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	public static void start(Context context,ContactUser sp) {
		Intent intent = new Intent(context, SamchatCreateSPStepTwoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("info", sp);
		intent.putExtras(bundle);
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
		setContentView(R.layout.samchat_createspsteptwo_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();

		setupPanel();
		registerBroadcastReceiver();

	}

	@Override
	protected void onDestroy() {
		unregisterBroadcastReceiver();
		super.onDestroy();
	}

	private void onParseIntent() {
		info = (ContactUser)getIntent().getSerializableExtra("info");
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		right_button_layout = findView(R.id.right_button_layout);
		avatar_headimageview = findView(R.id.avatar);
		cellphone_edittext = findView(R.id.cellphone);
		email_edittext = findView(R.id.email);
		address_edittext = findView(R.id.address);

		setupBackArrowClick();
		setupNextClick();
		setupCellphoneEditClick();
		setupEmailEditClick();
		setupAddressEditClick();

		avatar_headimageview.loadBuddyAvatar(DemoCache.getAccount());
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupNextClick(){
		right_button_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isCreating){
					return;
				}
				info.setphone_sp(cellphone);
				info.setemail_sp(email);
				info.setaddress_sp(address);

				isCreating = true;
				createSPAccount();
			}
		});
	}

	private TextWatcher cellphone_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			cellphone = cellphone_edittext.getText().toString().trim();
		}
	};

	private void setupCellphoneEditClick(){
		cellphone_edittext.addTextChangedListener(cellphone_textWatcher);	
	}

	private TextWatcher email_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			email = email_edittext.getText().toString().trim();
		}
	};

	private void setupEmailEditClick(){
		email_edittext.addTextChangedListener(email_textWatcher);	
	}

	private TextWatcher address_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			address = address_edittext.getText().toString().trim();
		}
	};

	private void setupAddressEditClick(){
        address_edittext.addTextChangedListener(address_textWatcher);
	}

/************************************date flow control************************************/
	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void createSPAccount(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_create_sp), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().create_sam_pros(info,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							SamchatCreateSPStepThreeActivity.start(SamchatCreateSPStepTwoActivity.this);
							Intent intent = new Intent();
                		intent.setAction(Constants.BROADCAST_CREATE_SP_SUCCESS);
                		sendbroadcast(intent);
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatCreateSPStepTwoActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatCreateSPStepTwoActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isCreating = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatCreateSPStepTwoActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatCreateSPStepTwoActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isCreating = false;
						}
					}, 0);
				}

		} );

	}


}



