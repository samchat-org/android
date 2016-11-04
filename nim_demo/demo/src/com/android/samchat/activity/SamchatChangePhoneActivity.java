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
import com.netease.nim.demo.DemoCache;
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
public class SamchatChangePhoneActivity extends UI implements OnKeyListener{
	private static final String TAG = SamchatChangePhoneActivity.class.getSimpleName();

	private final int MSG_COUNT_DOWN=1;

	private FrameLayout back_arrow_layout;
	private TextView submit_tv;
	private ClearableEditTextWithIcon verifycode_ev;
	private TextView phonenumber_tv;
	private LinearLayout indication_layout;
	private TextView countdown_tv;

	private String countrycode;
	private String cellphone;
	private String verifycode;
	private int countdown;

	private boolean isSubmitting=false;
	private boolean verifyReady=false;

	public static void start(Context context,String cc, String phone, int count) {
		Intent intent = new Intent(context, SamchatChangePhoneActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("countrycode",cc);
		intent.putExtra("cellphone",phone);
		intent.putExtra("countdown", count);
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
		setContentView(R.layout.samchat_changephone_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		parseIntent();
		setupPanel();
		handler = new CountDownHandler();

		updateCountdown();
		startCountDown();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(handler != null)
			handler.removeCallbacksAndMessages(null);
	}

	private void parseIntent() {
		countrycode = getIntent().getStringExtra("countrycode");
		cellphone = getIntent().getStringExtra("cellphone");
		countdown = getIntent().getIntExtra("countdown",60);
    }

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		submit_tv = findView(R.id.submit);
		verifycode_ev = findView(R.id.verifycode);
		phonenumber_tv = findView(R.id.phonenumber);
		indication_layout = findView(R.id.indication);
		countdown_tv = findView(R.id.countdown);

		setupBackArrowClick();
		setupEditClearButton();
		setupSubmitClick();

		if(!TextUtils.isEmpty(countrycode)){
			phonenumber_tv.setText("+"+countrycode+cellphone);
		}else{
			phonenumber_tv.setText(cellphone);
		}
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

	private void setupSubmitClick(){
		submit_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSubmitting){
					return;
				}
				if(verifyReady){
					isSubmitting = true;
					changePhoneNumber();
				}
				
			}
		});
		updateSubmitButton();
	}

	private void updateSubmitButton(){
		if(verifyReady){
			submit_tv.setEnabled(true);
			submit_tv.setBackgroundResource(R.drawable.samchat_button_green_active);
		}else{
			submit_tv.setEnabled(false);
			submit_tv.setBackgroundResource(R.drawable.samchat_button_green_inactive);
		}
	}

	private void setupEditClearButton(){
		verifycode_ev.setDeleteImage(R.drawable.nim_grey_delete_icon);
       Editable etext = verifycode_ev.getText();
		Selection.setSelection(etext, etext.length());
		verifycode_ev.setAfterTextChangedListener(new ClearableEditTextWithIcon.afterTextChangedListener(){
			@Override
			public void afterTextChangedCallback(Editable s){
				verifycode = verifycode_ev.getText().toString().trim();
				if(verifycode.length() > 0){
					verifyReady = true;
				}else{
					verifyReady = false;
				}
				updateSubmitButton();
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

					updateCountdown();
					if(countdown > 0){
						startCountDown();
					}
					countdown --;
					
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

	private void updateCountdown(){
		if(countdown>0){
			indication_layout.setVisibility(View.VISIBLE);
			countdown_tv.setText(" "+countdown+" ");
		}else{
			indication_layout.setVisibility(View.GONE);
			countdown_tv.setText(" "+countdown+" ");
		}
	}


/*************************Data Flow Control***************************************************************/
	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void changePhoneNumber(){		
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);

		ContactUser user = new ContactUser(SamService.getInstance().get_current_user());
		user.setcountrycode(countrycode);
		user.setcellphone(cellphone);
		SamService.getInstance().edit_cellphone(user,verifycode,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							Toast.makeText(SamchatChangePhoneActivity.this, R.string.samchat_change_phone_succeed, Toast.LENGTH_SHORT).show();
							Intent intent = new Intent();
							intent.setAction(Constants.BROADCAST_CHANGE_PHONE_ALREADY);
							sendbroadcast(intent);
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatChangePhoneActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatChangePhoneActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSubmitting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatChangePhoneActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatChangePhoneActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSubmitting = false;
						}
					}, 0);
				}

		});
	}
}



