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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class SamchatRegisterCodeRequestActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRegisterCodeRequestActivity.class.getSimpleName();

	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 0x100;

	public static final int COUNT_DOWN_MAXIUM = 60;
	
	private static final String FROM = "FROM";
	private int from;

	private int countdown=0;

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private TextView countrycode_textview;
	private EditText cellphone_edittext;
	private TextView send_textview;
	private ImageView cellphone_ok_imageview;
	private LinearLayout indication_layout;
	private TextView countdown_textview;

	private String countrycode = "1";
	private String cellphone;
	private String deviceid="000000";

	private boolean ready_send = false;

	private boolean isRequesting = false;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatRegisterCodeRequestActivity.this);
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

	public static void start(Context context,int from) {
		Intent intent = new Intent(context, SamchatRegisterCodeRequestActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
		setContentView(R.layout.samchat_registercoderequest_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		deviceid = UuidFactory.getInstance().getDeviceId();

		onParseIntent();

		setupPanel();

		registerBroadcastReceiver();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getHandler().removeCallbacksAndMessages(null);
		unregisterBroadcastReceiver();
	}

	private void onParseIntent() {
		from = getIntent().getIntExtra(FROM,Constants.FROM_SIGNUP);
	}

	private boolean isMobileNo(String phone){
		Pattern p = Pattern.compile("[0-9]*"); 
		Matcher m = p.matcher(phone); 
		if(m.matches()){
			return true;
		}else{
			return false;
		}
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
				if(isRequesting){
					return;
				}
				
				if(!ready_send && countdown == 0){
					
				}else if(countdown > 0){
					SamchatRegisterCodeVerifyActivity.start(SamchatRegisterCodeRequestActivity.this,countrycode,cellphone,deviceid,from,countdown);
				}else{
					if(from == Constants.FROM_SIGNUP){
						isRequesting = true;
						requestConfirmationCode();
					}else{
						isRequesting = true;
						requestFrogetPwdConfirmationCode();
					}
				}
			}
		});
		updateSendButton();
	}

	private void setCountDown(){
		indication_layout.setVisibility(View.GONE);
		countdown_textview.setText(" "+countdown+" ");
	}
	
	private void updateSendButton(){
		if(!ready_send && countdown == 0){
			send_textview.setEnabled(false);
			send_textview.setBackgroundResource(R.drawable.samchat_button_green_inactive);
			indication_layout.setVisibility(View.GONE);
		}else if(countdown > 0){
			send_textview.setEnabled(true);
			send_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
			setCountDown();
		}else{
			send_textview.setEnabled(true);
			send_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
			indication_layout.setVisibility(View.GONE);
		}
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
			ready_send = cellphone_edittext.getText().toString().trim().length()>=Constants.MIN_MPHONE_NUMBER_LENGTH;
			updateSendButton();
			if(ready_send){
				cellphone = cellphone_edittext.getText().toString().trim();
				cellphone_ok_imageview.setVisibility(View.VISIBLE);
			}else{
				cellphone_ok_imageview.setVisibility(View.GONE);
			}
		}
	};

	private void setupCellphoneEditClick(){
		cellphone_edittext.addTextChangedListener(cellphone_textWatcher);	
		cellphone_edittext.setTypeface(Typeface.SANS_SERIF);
	}
	
	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		countrycode_textview = findView(R.id.countrycode);
		cellphone_edittext = findView(R.id.cellphone);
		send_textview = findView(R.id.send);
		cellphone_ok_imageview = findView(R.id.cellphone_ok);
		indication_layout = findView(R.id.indication);
		countdown_textview = findView(R.id.countdown);

		if(from == Constants.FROM_SIGNUP){
			titlebar_name_textview.setText(getString(R.string.samchat_signup));
		}else{
			titlebar_name_textview.setText(getString(R.string.samchat_reset_password));
		}

		setupBackArrowClick();
		setupCountryCodeClick();
		setupCellphoneEditClick();
		setupSendClick();
		
	}

	private void updateCountryCode(){
		countrycode_textview.setText("+" + countrycode);
	}

	private void postCountdownMsg(){
		if(countdown > 0){
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSendButton();
					postCountdownMsg();
				}
			}, 1000);
			countdown--;
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_SELECT_COUNTRY_CODE && resultCode == RESULT_OK){
			countrycode = data.getStringExtra(Constants.CONFIRM_COUNTRYCODE);
			updateCountryCode();
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}

/********************************Data Flow Control*****************************************************/
	private void requestConfirmationCode(){
		if(!isMobileNo(cellphone)){
			Toast.makeText(SamchatRegisterCodeRequestActivity.this, R.string.samchat_cellphone_warning, Toast.LENGTH_SHORT).show();
			isRequesting = false;
			return;
		}

		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().register_code_request(countrycode,cellphone,deviceid, 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							countdown = COUNT_DOWN_MAXIUM;
							SamchatRegisterCodeVerifyActivity.start(SamchatRegisterCodeRequestActivity.this,countrycode,cellphone,deviceid,from,countdown);
							isRequesting = false;
							updateSendButton();
							postCountdownMsg();
						}
					}, 0);
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
							isRequesting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeRequestActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isRequesting = false;
						}
					}, 0);
				}

		});
	}

	private void requestFrogetPwdConfirmationCode(){
		if(!isMobileNo(cellphone)){
			Toast.makeText(SamchatRegisterCodeRequestActivity.this, R.string.samchat_cellphone_warning, Toast.LENGTH_SHORT).show();
			isRequesting = false;
			return;
		}

		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().findpwd_code_request(countrycode,cellphone,deviceid, 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							countdown = COUNT_DOWN_MAXIUM;
							SamchatRegisterCodeVerifyActivity.start(SamchatRegisterCodeRequestActivity.this,countrycode,cellphone,deviceid,from,countdown);
							isRequesting = false;
							updateSendButton();
							postCountdownMsg();
						}
					}, 0);
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
							isRequesting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatRegisterCodeRequestActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatRegisterCodeRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isRequesting = false;
						}
					}, 0);
				}

		});
	}

}

