package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.factory.LocationFactory;
import com.android.samservice.QuestionInfo;
import com.android.samservice.info.PlacesInfo;
import com.android.samservice.info.SendQuestion;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
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
import com.android.samservice.HttpCommClient;
public class SamchatNewRequestActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatNewRequestActivity.class.getSimpleName();
	private static final int MSG_COUNT_DOWN = 100;
	private static final int TIME_COUNT_DOWN = 2000;
	
	private FrameLayout back_arrow_layout;
	private TextView send_textview;
	private EditText question_edittext;
	private EditText location_edittext;

	private String question = null;
	private String location = null;
	private String locationInput = null;
	private String pre_locationInput=null;

	private boolean ready_send = false;

	private boolean isSending = false;
	
	public static void startActivityForResult(Activity activity, TFragment fragment, int requestCode) {
		Intent intent = new Intent(activity, SamchatNewRequestActivity.class);
        fragment.startActivityForResult(intent, requestCode);
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
		setContentView(R.layout.samchat_newrequest_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

		LocationFactory.getInstance().startLocationMonitor();

		handler = new LocationHandler();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationFactory.getInstance().stopLocationMonitor();
		handler.removeCallbacksAndMessages(null);
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		send_textview = findView(R.id.send);
		question_edittext = findView(R.id.question);
		location_edittext = findView(R.id.location);

		setupBackArrowClick();
		setupSendClick();
		setupQuestionEditClick();
		setupLocationEditClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void updateSend(){
		boolean enable = ready_send;
		send_textview.setEnabled(enable);
	}

	private void setupSendClick(){
		updateSend();
		send_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSending){
					return;
				}

				isSending = true;
				sendQuestion();
			}
		});
	}

	private TextWatcher question_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			question = question_edittext.getText().toString().trim();
			if(question.length() > 0){
				ready_send = true;
			}else{
				ready_send = false;
			}
			updateSend();
		}
	};

	private void setupQuestionEditClick(){
		question_edittext.addTextChangedListener(question_textWatcher);	
	}

	private boolean stringEquals(String s1, String s2){
		if(s1 == null && s2 == null){
			return true;
		}else if(s1 == null && s2 != null){
			return false;
		}else if(s1 != null && s2 == null){
			return false;
		}else{
			return s1.equals(s2);
		}
	}

	private TextWatcher location_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			pre_locationInput = locationInput;
			locationInput = location_edittext.getText().toString().trim();
			if(!stringEquals(pre_locationInput,locationInput)){
				cancelQueryCountDown();
				startQueryCountDown();
			}else if(TextUtils.isEmpty(locationInput)){
				cancelQueryCountDown();
			}
		}
	};

	private void setupLocationEditClick(){
		location_edittext.addTextChangedListener(location_textWatcher);	
	}

	private LocationHandler handler;

	private class LocationHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_COUNT_DOWN:
					getPlacesInfo(locationInput);	
					break;
				}
		}
	}

	private void cancelQueryCountDown() {
		handler.removeMessages(MSG_COUNT_DOWN);
	}

	private void startQueryCountDown() {
		Message msg = handler.obtainMessage(MSG_COUNT_DOWN);
		handler.sendMessageDelayed(msg, TIME_COUNT_DOWN);
	}

/******************************************Data Flow Control***********************************************************/
	private void sendQuestion(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().send_question(question,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,
				null,location,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							QuestionInfo qinfo = ((HttpCommClient)obj).qinfo;
							SamchatRequestDetailsActivity.start(SamchatNewRequestActivity.this, qinfo);
							SendQuestion sq = new SendQuestion(qinfo.question_id,qinfo.question, qinfo.datetime,location);
							Intent data = new Intent();
							Bundle bundle = new Bundle();
							bundle.putSerializable("send_question", sq);
							data.putExtras(bundle);
							SamchatNewRequestActivity.this.setResult(SamchatNewRequestActivity.this.RESULT_OK, data);
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatNewRequestActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatNewRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending= false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatNewRequestActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatNewRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

		} );
	}


	private void getPlacesInfo(String key){
		if(key == null){
			return;
		}

		SamService.getInstance().get_places_info(key, new SMCallBack(){
			@Override
			public void onSuccess(final Object obj, final int WarningCode) {
				HttpCommClient hcc = (HttpCommClient)obj;
				for(PlacesInfo info: hcc.placesinfos.getinfo()){
					LogUtil.e(TAG,"description:"+info.description+" place_id:"+info.place_id);
				}
				if(hcc.placesinfos.getkey().equals(locationInput)){
					LogUtil.e(TAG,"show drag down menu");
				}
			}
			@Override
			public void onFailed(int code) {}
			@Override
			public void onError(int code) {}
		 });
	}
}



