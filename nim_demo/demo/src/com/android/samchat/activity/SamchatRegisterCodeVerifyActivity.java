package com.android.samchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.samchat.ui.CircleEditText;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;

public class SamchatRegisterCodeVerifyActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRegisterCodeVerifyActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private TextView countdown_textview;
	private LinearLayout indication_layout;
	private TextView phoneWithCC_tv;

	private CircleEditText code_1_edittext;
	private CircleEditText code_2_edittext;
	private CircleEditText code_3_edittext;
	private CircleEditText code_4_edittext;

	private boolean code_1_ready=false;
	private boolean code_2_ready=false;
	private boolean code_3_ready=false;
	private boolean code_4_ready=false;

	private static final String COUNTRYCODE = "COUNTRYCODE";
	private static final String CELLPHONE = "CELLPHONE";
	private static final String DEVICEID = "DEVICEID";
	private static final String FROM = "FROM";
	private static final String COUNTDOWN="COUNTDOWN";
	
	private int from;
	private String countrycode;
	private String cellphone;
	private String verifycode;
	private String deviceid;
	private int countdown;

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
	
	public static void start(Context context,String countrycode, String cellphone,String deviceid,int from, int countdown) {
		Intent intent = new Intent(context, SamchatRegisterCodeVerifyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(COUNTRYCODE,countrycode);
		intent.putExtra(CELLPHONE,cellphone);
		intent.putExtra(DEVICEID,deviceid);
		intent.putExtra(FROM,from);
		intent.putExtra(COUNTDOWN,countdown);
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

		updateCountdown();
		postCountdownMsg();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterBroadcastReceiver();
	}

	private void postCountdownMsg(){
		if(isDestroyedCompatible()){
			return;
		}
		
		if(countdown > 0){
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateCountdown();
					postCountdownMsg();
				}
			}, 1000);
			countdown--;
		}
	}

	private void updateCountdown(){
		if(countdown>0){
			indication_layout.setVisibility(View.VISIBLE);
			countdown_textview.setText(" "+countdown+" ");
		}else{
			indication_layout.setVisibility(View.GONE);
			countdown_textview.setText(" "+countdown+" ");
		}
	}
		
	private void onParseIntent() {
		countrycode = getIntent().getStringExtra(COUNTRYCODE);
		cellphone = getIntent().getStringExtra(CELLPHONE);
		deviceid = getIntent().getStringExtra(DEVICEID);
		from = getIntent().getIntExtra(FROM,Constants.FROM_SIGNUP);
		countdown = getIntent().getIntExtra(COUNTDOWN,0);
	}

	private void setupConfirmationCodePanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		code_1_edittext = findView(R.id.code_1);
		code_2_edittext = findView(R.id.code_2);
		code_3_edittext = findView(R.id.code_3);
		code_4_edittext = findView(R.id.code_4);
		countdown_textview = findView(R.id.countdown);
		indication_layout = findView(R.id.indication);
		phoneWithCC_tv = findView(R.id.phoneWithCC);

		if(from == Constants.FROM_SIGNUP){
			titlebar_name_textview.setText(getString(R.string.samchat_signup));
		}else{
			titlebar_name_textview.setText(getString(R.string.samchat_reset_password));
		}

		setupBackArrowClick();
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
				code_1_edittext.setFilled();
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
              code_2_edittext.setFilled();
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
             	code_3_edittext.setFilled();
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
              code_4_edittext.setFilled();
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

	private void updateCellphone(){
		phoneWithCC_tv.setText("+" + countrycode+" "+cellphone);
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
		code_1_edittext.clearFilled();
		code_2_edittext.clearFilled();
		code_3_edittext.clearFilled();
		code_4_edittext.clearFilled();
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


