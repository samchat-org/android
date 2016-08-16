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
import com.netease.nim.demo.R;
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


public class SamchatRegisterCodeRequestActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRegisterCodeRequestActivity.class.getSimpleName();

	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 0x100;

	private FrameLayout back_arrow_layout;
	private TextView countrycode_textview;
	private EditText cellphone_edittext;
	private TextView send_textview;

	private String countrycode = "1";
	private String cellphone;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatRegisterCodeRequestActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SIGN_IN_ALREADY);
		filter.addAction(Constants.BROADCAST_SIGN_UP_ALREADY);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SIGN_IN_ALREADY)){
					finish();
				}else if(intent.getAction().equals(Constants.BROADCAST_SIGN_UP_ALREADY)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatRegisterCodeRequestActivity.class);
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
		setContentView(R.layout.samchat_registercoderequest_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupSignUpPanel();

		registerBroadcastReceiver();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        unregisterBroadcastReceiver();
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
		updateCountryCode();
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatRegisterCodeRequestActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
	}

	private void setupSendClick(){
		send_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				requestConfirmationCode();
			}
		});

		updateSendButton(false);
	}

	private void updateSendButton(boolean enable){
		send_textview.setEnabled(enable);
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
			boolean isEnable = (cellphone_edittext.getText().length()>=5 && cellphone_edittext.getText().toString().trim().length()>=5);
			updateSendButton(isEnable);
			if(isEnable){
				cellphone = cellphone_edittext.getText().toString().trim();
			}
		}
	};

	private void setupCellphoneEditClick(){
		cellphone_edittext.addTextChangedListener(cellphone_textWatcher);		
	}
	
	private void setupSignUpPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		countrycode_textview = findView(R.id.countrycode);
		cellphone_edittext = findView(R.id.cellphone);
		send_textview = findView(R.id.send);

		setupBackArrowClick();
		setupCountryCodeClick();
		setupCellphoneEditClick();
		setupSendClick();

	}

	private void updateCountryCode(){
		countrycode_textview.setText("+" + countrycode);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_SELECT_COUNTRY_CODE && resultCode == RESULT_OK){
			countrycode = data.getStringExtra(Constants.CONFIRM_COUNTRYCODE);
			updateCountryCode();
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}

	private void requestConfirmationCode(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().register_code_request(countrycode,cellphone,UuidFactory.getInstance().getDeviceId(), 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					SamchatRegisterCodeVerifyActivity.start(SamchatRegisterCodeRequestActivity.this,countrycode,cellphone);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeRequestActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					LogUtil.e("test","register_code_request error "+code);
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeRequestActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

		});
	}

}

