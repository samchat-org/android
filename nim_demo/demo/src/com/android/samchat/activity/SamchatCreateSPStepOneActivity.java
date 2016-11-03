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
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.info.ContactUser;
public class SamchatCreateSPStepOneActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCreateSPStepOneActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private EditText company_name_edittext;
	private EditText service_category_edittext;
	private TextView next_tv;

	String company_name;
	String service_category;

	boolean ready_company_name = false;
	boolean ready_service_category = false;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatCreateSPStepOneActivity.this);
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

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatCreateSPStepOneActivity.class);
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
		setContentView(R.layout.samchat_createspstepone_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();
		registerBroadcastReceiver();

	}

	@Override
	protected void onDestroy() {
		unregisterBroadcastReceiver();
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		company_name_edittext = findView(R.id.company_name);
		service_category_edittext = findView(R.id.service_category);
		next_tv = findView(R.id.next);
			
		setupBackArrowClick();
		setupNextClick();
		setupCompanyNameEditClick();
		setupServiceCategoryEditClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void updateNext(){
		boolean enable = ready_service_category & ready_company_name;
		next_tv.setEnabled(enable);
		if(enable){
			next_tv.setBackgroundResource(R.drawable.samchat_button_lake_active);
		}else{
			next_tv.setBackgroundResource(R.drawable.samchat_button_lake_inactive);
		}
	}

	private void setupNextClick(){
		updateNext();
		next_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ContactUser sp = new ContactUser(SamService.getInstance().get_current_user());
				sp.setcompany_name(company_name);
				sp.setservice_category(service_category);
				SamchatCreateSPStepTwoActivity.start(SamchatCreateSPStepOneActivity.this,sp);
			}
		});
	}

	private TextWatcher compnay_name_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			company_name = company_name_edittext.getText().toString().trim();
			if(company_name.length() > 0){
				ready_company_name = true;
			}else{
				ready_company_name = false;
			}
			updateNext();
		}
	};

	private void setupCompanyNameEditClick(){
		company_name_edittext.addTextChangedListener(compnay_name_textWatcher);	
	}

	private TextWatcher service_category_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			service_category = service_category_edittext.getText().toString().trim();
			if(service_category.length() > 0){
				ready_service_category= true;
			}else{
				ready_service_category = false;
			}
			updateNext();
		}
	};

	private void setupServiceCategoryEditClick(){
		service_category_edittext.addTextChangedListener(service_category_textWatcher);	
	}

}


