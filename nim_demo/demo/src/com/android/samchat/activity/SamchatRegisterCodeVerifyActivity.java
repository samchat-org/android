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
import android.os.SystemClock;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;

public class SamchatRegisterCodeVerifyActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRegisterCodeVerifyActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private TextView countrycode_textview;
	private TextView cellphone_textview;

	private EditText code_1_edittext;
	private EditText code_2_edittext;
	private EditText code_3_edittext;
	private EditText code_4_edittext;

	private boolean code_1_ready=false;
	private boolean code_2_ready=false;
	private boolean code_3_ready=false;
	private boolean code_4_ready=false;

	private static final String COUNTRYCODE = "COUNTRYCODE";
	private static final String CELLPHONE = "CELLPHONE";
	private static final String DEVICEID = "DEVICEID";
	private static final String FROM = "FROM";
	
	private int from;
	private String countrycode;
	private String cellphone;
	private String verifycode;
	private String deviceid;

	private boolean isVerifying = false;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatRegisterCodeVerifyActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SIGN_UP_ALREADY);
		filter.addAction(Constants.BROADCAST_FINDPWD_ALREADY);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SIGN_UP_ALREADY)){
					finish();
				}else if(intent.getAction().equals(Constants.BROADCAST_FINDPWD_ALREADY)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}
	
	public static void start(Context context,String countrycode, String cellphone,String deviceid,int from) {
		Intent intent = new Intent(context, SamchatRegisterCodeVerifyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(COUNTRYCODE,countrycode);
		intent.putExtra(CELLPHONE,cellphone);
		intent.putExtra(DEVICEID,deviceid);
		intent.putExtra(FROM,from);
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
		setContentView(R.layout.samchat_registercodeverify_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();
		setupConfirmationCodePanel();

		registerBroadcastReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterBroadcastReceiver();
	}

	private void onParseIntent() {
		countrycode = getIntent().getStringExtra(COUNTRYCODE);
		cellphone = getIntent().getStringExtra(CELLPHONE);
		deviceid = getIntent().getStringExtra(DEVICEID);
		from = getIntent().getIntExtra(FROM,Constants.FROM_SIGNUP);
	}

	private void setupConfirmationCodePanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		countrycode_textview = findView(R.id.countrycode);
		cellphone_textview = findView(R.id.cellphone);
		code_1_edittext = findView(R.id.code_1);
		code_2_edittext = findView(R.id.code_2);
		code_3_edittext = findView(R.id.code_3);
		code_4_edittext = findView(R.id.code_4);

		if(from == Constants.FROM_SIGNUP){
			titlebar_name_textview.setText(getString(R.string.samchat_confirmation_code));
		}else{
			titlebar_name_textview.setText(getString(R.string.samchat_reset_password));
		}

		setupBackArrowClick();
		updateCountryCode();
		updateCellphone();
		setupInputCode();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private TextWatcher code_1_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			code_1_ready = code_1_edittext.getText().length() == 1;
			verify();
			if(code_1_ready){
				code_1_edittext.clearFocus();
				code_2_edittext.requestFocus();
			}
		}
    };

	private TextWatcher code_2_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			code_2_ready = code_2_edittext.getText().length() == 1;
			verify();
			if(code_2_ready){
				code_2_edittext.clearFocus();
				code_3_edittext.requestFocus();
			}
		}
    };

	private TextWatcher code_3_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			code_3_ready = code_3_edittext.getText().length() == 1;
			verify();
			if(code_3_ready){
				code_3_edittext.clearFocus();
				code_4_edittext.requestFocus();
			}
		}
    };

	private TextWatcher code_4_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			code_4_ready = code_4_edittext.getText().length() == 1;
			verify();
			if(code_4_ready){
				code_4_edittext.clearFocus();
				code_1_edittext.requestFocus();
			}
		}
    };
	
	private void setupInputCode(){
		code_1_edittext.addTextChangedListener(code_1_textWatcher);
		code_2_edittext.addTextChangedListener(code_2_textWatcher);
		code_3_edittext.addTextChangedListener(code_3_textWatcher);
		code_4_edittext.addTextChangedListener(code_4_textWatcher);
	}

	private void updateCountryCode(){
		countrycode_textview.setText("+" + countrycode);
	}

	private void updateCellphone(){
		cellphone_textview.setText(cellphone);
	}
	
	

	private void verify(){
		if(isVerifying){
			return;
		}
		
		if(code_1_ready && code_2_ready && code_3_ready && code_4_ready){
			verifycode = code_1_edittext.getText().toString() + code_2_edittext.getText().toString() +
								  code_3_edittext.getText().toString() + code_4_edittext.getText().toString();
			
			if(from == Constants.FROM_SIGNUP){
				isVerifying = true;
				verifyConfirmationCode();
			}else{
				isVerifying = true;
				verifyForgetPwdConfirmationCode();
			}
			
		}
	}

	private void clearAllEditText(){
		code_1_edittext.setText("");
		code_2_edittext.setText("");
		code_3_edittext.setText("");
		code_4_edittext.setText("");
	}
	
/*************************************data flow control*********************************************************/
	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}
	private void verifyConfirmationCode(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_verifying), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().register_code_verify(countrycode,cellphone,verifycode,deviceid, 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					//lunch sign up finally ui
					SamchatSignupActivity.start(SamchatRegisterCodeVerifyActivity.this,  countrycode,  cellphone,  
						verifycode, deviceid);
					
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_SIGN_UP_ALREADY);
                sendbroadcast(intent);
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeVerifyActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							clearAllEditText();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeVerifyActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isVerifying = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeVerifyActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							clearAllEditText();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeVerifyActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isVerifying = false;
						}
					}, 0);
				}
		});
	}


	private void verifyForgetPwdConfirmationCode(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_verifying), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().findpwd_code_verify(countrycode,cellphone,verifycode,deviceid, 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					//lunch reset password finally ui
					SamchatResetPasswordActivity.start(SamchatRegisterCodeVerifyActivity.this,  countrycode,  cellphone,  
						verifycode, deviceid);

					Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_FINDPWD_ALREADY);
                sendbroadcast(intent);
					
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeVerifyActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							clearAllEditText();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeVerifyActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isVerifying = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeVerifyActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							clearAllEditText();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeVerifyActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isVerifying = false;
						}
					}, 0);
				}
		});
	}

}


