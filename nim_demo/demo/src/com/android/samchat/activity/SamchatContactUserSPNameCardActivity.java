package com.android.samchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.R;
import com.android.samchat.common.FastBlurUtils;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import com.android.samservice.info.ContactUser;
public class SamchatContactUserSPNameCardActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatContactUserSPNameCardActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private HeadImageView avatar_headimageview;
	private TextView company_name_tv;
	private TextView service_category_tv;
	private TextView service_description_tv;
	private LinearLayout chat_layout;
	private LinearLayout qr_layout;
	private LinearLayout follow_layout;
	private ImageView follow_button_iv;
	private TextView work_phone_tv;
	private TextView email_tv;
	private TextView location_tv;
	private ImageView wall_iv;

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
		company_name_tv = findView(R.id.company_name);
		service_category_tv = findView(R.id.service_category);
		service_description_tv = findView(R.id.service_description);
		chat_layout = findView(R.id.chat_layout);
		qr_layout = findView(R.id.qr_layout);
		follow_layout = findView(R.id.follow_layout);
		follow_button_iv = findView(R.id.follow_button);
		work_phone_tv = findView(R.id.work_phone);
		email_tv = findView(R.id.email);
		location_tv = findView(R.id.location);
		wall_iv = findView(R.id.wall);

		setupBackArrowClick();
		setupChatClick();
		setupFollowClick();
		updateFollow();
		
		avatar_headimageview.loadBuddyAvatar(sp.getAccount(), 90, new HeadImageView.OnImageLoadedListener(){
			@Override
			public void OnImageLoadedListener(Bitmap bitmap){
				FastBlurUtils.blur(bitmap, wall_iv);
			}
		});
		titlebar_name_textview.setText(sp.getusername());
		company_name_tv.setText(sp.getcompany_name());
		service_category_tv.setText(sp.getservice_category());
		service_description_tv.setText(sp.getservice_description());
		if(!TextUtils.isEmpty(sp.getphone_sp())){
			if(TextUtils.isEmpty(sp.getcountrycode_sp())){
				work_phone_tv.setText("+"+sp.getcountrycode_sp()+sp.getphone_sp());
			}else{
				work_phone_tv.setText(sp.getphone_sp());
			}
		}
		if(!TextUtils.isEmpty(sp.getemail_sp())){
			email_tv.setText(sp.getemail_sp());
		}
		if(!TextUtils.isEmpty(sp.getaddress_sp())){
			location_tv.setText(sp.getaddress_sp());
		}

		
		

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
			follow_button_iv.setImageResource(R.drawable.samchat_follow_toggle_on);
		}else{
			follow_button_iv.setImageResource(R.drawable.samchat_follow_toggle_off);
		}
	}

	private void setupFollowClick(){
		follow_button_iv.setOnClickListener(new OnClickListener() {
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

	private void setupChatClick(){
		chat_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SessionHelper.startP2PSession(SamchatContactUserSPNameCardActivity.this, sp.getAccount());
			}
		});
	}

	private void setupQrcodeClick(){
		qr_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatQRCodeActivity.start(SamchatContactUserSPNameCardActivity.this, Constants.FROM_SP_ACTIVITY_LAUNCH);
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



