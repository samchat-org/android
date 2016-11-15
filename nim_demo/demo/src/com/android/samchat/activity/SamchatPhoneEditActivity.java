package com.android.samchat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.samchat.R;
import com.android.samchat.adapter.PlaceInfoAdapter;
import com.android.samservice.HttpCommClient;
import com.android.samservice.info.PlacesInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import com.android.samservice.info.ContactUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//SamchatRegisterCodeRequestActivity
public class SamchatPhoneEditActivity extends UI implements OnKeyListener{
	private static final String TAG = SamchatPhoneEditActivity.class.getSimpleName();

	private final int MSG_COUNT_DOWN=1;

	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 0x100;
	public static final int COUNT_DOWN_MAXIUM = 60;

	private FrameLayout back_arrow_layout;
	private TextView next_tv;	
	private TextView countrycode_tv;
	private ClearableEditTextWithIcon phone_ev;

	private LinearLayout edit_layout;
	private LinearLayout show_layout;
	private TextView show_countrycode_tv;
	private TextView show_phone_tv;

	private String old_countrycode;
	private String new_countrycode;

	private String old_phonenumber;
	private String new_phonenumber;

	private boolean ready_send = false;
	private boolean isRequesting = false;
	private int countdown=0;

	private String deviceid="000000";

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatPhoneEditActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_CHANGE_PHONE_ALREADY);
		
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_CHANGE_PHONE_ALREADY)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
		isBroadcastRegistered = true;
	}
		

	
	private void unregisterBroadcastReceiver(){
		if(isBroadcastRegistered){
			broadcastManager.unregisterReceiver(broadcastReceiver);
			isBroadcastRegistered = false;
		}
	}

	public static void start(Context context,String countrycode, String phonenumber) {
		Intent intent = new Intent(context, SamchatPhoneEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("countrycode",countrycode);
		intent.putExtra("phonenumber", phonenumber);
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
		setContentView(R.layout.samchat_phoneedit_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		parseIntent();
		setupPanel();
		handler = new CountDownHandler();

		registerBroadcastReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterBroadcastReceiver();
		if(handler != null)
			handler.removeCallbacksAndMessages(null);
	}

	private void parseIntent() {
		old_countrycode = getIntent().getStringExtra("countrycode");
		old_phonenumber = getIntent().getStringExtra("phonenumber");
		new_countrycode = old_countrycode;
		new_phonenumber = old_phonenumber;
    }

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		next_tv = findView(R.id.next);
		countrycode_tv = findView(R.id.countrycode);
		phone_ev = findView(R.id.phone);
		edit_layout = findView(R.id.edit_layout);
		show_layout = findView(R.id.show_layout);
		show_countrycode_tv = findView(R.id.show_countrycode);
		show_phone_tv = findView(R.id.show_phone);

		setupBackArrowClick();
		setupCountryCodeClick();
		setupEditClearButton();
		setupNextClick();

		edit_layout.setVisibility(View.VISIBLE);
		show_layout.setVisibility(View.GONE);
	}

	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(back_arrow_layout.getWindowToken(), 0);
				finish();
			}
		});
	}

	private void setupNextClick(){
		next_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isRequesting){
					return;
				}

				if(old_phonenumber.equals(new_phonenumber)
					&& old_countrycode.equals(new_countrycode)){
					Toast.makeText(SamchatPhoneEditActivity.this, R.string.samchat_same_cellphone_error, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(!ready_send && countdown == 0){
					
				}else if(countdown > 0){
					SamchatChangePhoneActivity.start(SamchatPhoneEditActivity.this, new_countrycode, new_phonenumber, countdown);
				}else{
					isRequesting = true;
					requestConfirmationCode();
				}
			}
		});
		next_tv.setEnabled(false);
		updateNextButtonBackground(false);
	}

	private void updateNextButtonBackground(boolean clickable){
		next_tv.setBackgroundResource(clickable?R.drawable.samchat_button_green_active:R.drawable.samchat_button_green_inactive);
	}

	private void updateNextButton(){
		if(!ready_send && countdown == 0){
			next_tv.setEnabled(false);
			next_tv.setBackgroundResource(R.drawable.samchat_button_green_inactive);
		}else if(countdown > 0){
			next_tv.setEnabled(true);
			next_tv.setBackgroundResource(R.drawable.samchat_button_green_active);
		}else{
			next_tv.setEnabled(true);
			next_tv.setBackgroundResource(R.drawable.samchat_button_green_active);
		}
		
	}

	private void setupCountryCodeClick(){
		countrycode_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatPhoneEditActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
		updateCountryCode(old_countrycode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_SELECT_COUNTRY_CODE && resultCode == RESULT_OK){
			new_countrycode = data.getStringExtra(Constants.CONFIRM_COUNTRYCODE);
			updateCountryCode(new_countrycode);
			if(new_phonenumber.length()>=Constants.MIN_MPHONE_NUMBER_LENGTH
				&& new_countrycode.length()>=1){
				ready_send = true;
			}else{
				ready_send = false;
			}
			updateNextButton();
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}

	private void updateCountryCode(String c){
		if(TextUtils.isEmpty(c)){
			countrycode_tv.setText("+");
		}else{
			countrycode_tv.setText("+"+c);
		}
	}

	private void setupEditClearButton(){
		phone_ev.setDeleteImage(R.drawable.nim_grey_delete_icon);
		phone_ev.setText(old_phonenumber);
       Editable etext = phone_ev.getText();
		Selection.setSelection(etext, etext.length());
		phone_ev.setAfterTextChangedListener(new ClearableEditTextWithIcon.afterTextChangedListener(){
			@Override
			public void afterTextChangedCallback(Editable s){
				new_phonenumber = phone_ev.getText().toString().trim();
				if(new_phonenumber.length()>=Constants.MIN_MPHONE_NUMBER_LENGTH
					&& new_countrycode.length()>=1){
					ready_send = true;
				}else{
					ready_send = false;
				}
				updateNextButton();
			}
		});
	}

	private CountDownHandler handler;

	private class CountDownHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_COUNT_DOWN:
					if(isDestroyedCompatible()){
						return;
					}
					
					if(countdown-- > 0){
						startCountDown();
					}else{
						show_layout.setVisibility(View.GONE);
						edit_layout.setVisibility(View.VISIBLE);
					}
					break;
				}
		}
	}

	private void cancelCountDown() {
		handler.removeMessages(MSG_COUNT_DOWN);
	}

	private void startCountDown() {
		Message msg = handler.obtainMessage(MSG_COUNT_DOWN);
		handler.sendMessageDelayed(msg, 1000);
	}
		



/*************************Data Flow Control***************************************************************/
	private boolean isMobileNo(String phone){
		Pattern p = Pattern.compile("[0-9]*");
		Matcher m = p.matcher(phone);
		return m.matches();

	}

	private void requestConfirmationCode(){
		final String cc = new_countrycode;
		final String cellphone = new_phonenumber;
		if(!isMobileNo(new_phonenumber)){
			Toast.makeText(SamchatPhoneEditActivity.this, R.string.samchat_cellphone_warning, Toast.LENGTH_SHORT).show();
			isRequesting = false;
			return;
		}

		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().edit_cellphone_code_request(cc,cellphone,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							countdown = COUNT_DOWN_MAXIUM;
							SamchatChangePhoneActivity.start(SamchatPhoneEditActivity.this,  cc, cellphone, countdown);
							isRequesting = false;
							
							show_countrycode_tv.setText(countrycode_tv.getText().toString().trim());
							show_phone_tv.setText(phone_ev.getText().toString().trim());
							show_layout.setVisibility(View.VISIBLE);
							edit_layout.setVisibility(View.GONE);

							startCountDown();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatPhoneEditActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatPhoneEditActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isRequesting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatPhoneEditActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatPhoneEditActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isRequesting = false;
						}
					}, 0);
				}

		});
	}
}


