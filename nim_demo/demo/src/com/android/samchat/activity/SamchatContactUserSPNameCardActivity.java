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

import com.android.samchat.cache.FollowDataCache;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
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
public class SamchatContactUserSPNameCardActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatContactUserSPNameCardActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private HeadImageView avatar_headimageview;
	private TextView follow_textview;
	private TextView chat_textview;
	private TextView service_description_textview;

	private ContactUser sp;

	private boolean isFollowing = false;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatContactUserSPNameCardActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_FOLLOWEDSP_UPDATE)){
					updateFollow();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}
	

	public static void start(Context context,ContactUser sp) {
		Intent intent = new Intent(context, SamchatContactUserSPNameCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("service_provider", sp);
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
		setContentView(R.layout.samchat_contactuserspnamecard_activity);

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

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		avatar_headimageview = findView(R.id.avatar);
		follow_textview = findView(R.id.follow);
		chat_textview = findView(R.id.chat);
		service_description_textview =  findView(R.id.service_description);

		setupBackArrowClick();
		setFollowClick();
		setChatClick();

		updateFollow();
		avatar_headimageview.loadBuddyAvatar(sp.getAccount(), 90);
		titlebar_name_textview.setText(sp.getusername());

	}

	private void onParseIntent() {
		sp = (ContactUser)getIntent().getSerializableExtra("service_provider");
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void updateFollow(){
		if(FollowDataCache.getInstance().getFollowSPByUniqueID(sp.getunique_id()) != null){
			follow_textview.setText(getString(R.string.samchat_unfollow));
		}else{
			follow_textview.setText(getString(R.string.samchat_follow));
		}
	}

	private void setFollowClick(){
		follow_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isFollowing){
					return;
				}

				isFollowing = true;
				if(FollowDataCache.getInstance().getFollowSPByUniqueID(sp.getunique_id()) != null){
					follow(false);
				}else{
					follow(true);
				}
			}
		});
	}

	private void setChatClick(){
		chat_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SessionHelper.startP2PSession(SamchatContactUserSPNameCardActivity.this, sp.getAccount());
			}
		});
	}

/**************************Data Flow Control***************************************************/
	private void follow(boolean isFollow){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		
		SamService.getInstance().follow(isFollow,  sp, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							updateFollow();
							isFollowing = false;
						}
					}, 0);
					
					
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isFollowing = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isFollowing = false;
						}
					}, 0);
				}
		});

	}

}



