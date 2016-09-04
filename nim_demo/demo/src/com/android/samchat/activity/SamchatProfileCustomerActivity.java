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
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
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
import com.android.samservice.info.ContactUser;
public class SamchatProfileCustomerActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatProfileCustomerActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private TextView phonenumber_textview;
	private TextView username_textview;
	private TextView email_textview;
	private TextView address_textview;

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatProfileCustomerActivity.class);
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
		setContentView(R.layout.samchat_profilecustomer_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

	}

	@Override
	public void onResume(){
		super.onResume();
		update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void update(){
		avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90);
		phonenumber_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode()+" "+SamService.getInstance().get_current_user().getcellphone());
		username_textview.setText(SamService.getInstance().get_current_user().getusername());
		email_textview.setText(SamService.getInstance().get_current_user().getemail());
		address_textview.setText(SamService.getInstance().get_current_user().getaddress());
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		avatar_headimageview= findView(R.id.avatar);
		phonenumber_textview= findView(R.id.phonenumber);
		username_textview= findView(R.id.username);
		email_textview= findView(R.id.email);
		address_textview= findView(R.id.address);

		setupBackArrowClick();
		setupEmailClick();
		setupAddressClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupEmailClick(){
		email_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_EMAIL, email_textview.getText().toString().trim());
			}
		});
	}

	private void setupAddressClick(){
		address_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileEditActivity.start(SamchatProfileCustomerActivity.this, SamchatProfileEditActivity.EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS, address_textview.getText().toString().trim());
			}
		});
	}

}



