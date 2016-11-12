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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.R;
import com.android.samchat.common.BasicUserInfoHelper;
import com.android.samchat.common.FastBlurUtils;
import com.android.samservice.HttpCommClient;
import com.android.samservice.type.TypeEnum;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.widget.Toast;

import com.android.samservice.info.ContactUser;
public class SamchatContactUserSPNameCardActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatContactUserSPNameCardActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private HeadImageView avatar_headimageview;
	private TextView company_name_tv;
	private TextView service_category_tv;
	private TextView service_description_tv;
	private RelativeLayout chat_layout;
	private RelativeLayout qr_layout;
	private SwitchButton switchButtonFollow;
	private RelativeLayout op_layout;
	private TextView op_text_tv;
	private TextView work_phone_tv;
	private TextView email_tv;
	private TextView location_tv;
	private ImageView wall_iv;

	private ContactUser sp;
	private String account;
	private String username;

	private long unique_id;

	private boolean isFollowing = false;
	private boolean isOpting=false;

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
		isBroadcastRegistered = true;
	}
		

	
	private void unregisterBroadcastReceiver(){
	    if(isBroadcastRegistered){
			broadcastManager.unregisterReceiver(broadcastReceiver);
			isBroadcastRegistered = false;
		}
	}
	

	public static void start(Context context,ContactUser sp) {
		Intent intent = new Intent(context, SamchatContactUserSPNameCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("service_provider", sp);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	public static void start(Context context,String id, String name) {
		Intent intent = new Intent(context, SamchatContactUserNameCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putString("account",id);
		bundle.putString("username",name);
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
		
		if(onParseIntent()){
			query_user_precise(unique_id);
			setupPanel();
			registerBroadcastReceiver();
		}else{
			finish();
		}
		
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
		op_layout = findView(R.id.op_layout);
		switchButtonFollow = findView(R.id.follow_toggle);
		work_phone_tv = findView(R.id.work_phone);
		email_tv = findView(R.id.email);
		location_tv = findView(R.id.location);
		wall_iv = findView(R.id.wall);
		op_text_tv = findView(R.id.op_text);

		setupBackArrowClick();
		setupChatClick();
		setupFollowClick();
		setupOpServiceProviderClick();
		setupQrcodeClick();

		refresh();
		
	}

	private void refresh(){
		if(sp != null && !TextUtils.isEmpty(sp.getAvatar())){
			avatar_headimageview.loadBuddyAvatarByUrl(sp.getAccount(), sp.getAvatar(),(int) getResources().getDimension(R.dimen.samchat_avatar_size_in_namecard), new HeadImageView.OnImageLoadedListener(){
				@Override
				public void OnImageLoadedListener(Bitmap bitmap){
					FastBlurUtils.blur(bitmap, wall_iv);
				}
			});
		}else{
			avatar_headimageview.setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());
		}
				
		titlebar_name_textview.setText(sp !=null ?sp.getusername():username);
		company_name_tv.setText(sp!= null ? sp.getcompany_name():"");
		service_category_tv.setText(sp!=null ? sp.getservice_category():"");
		service_description_tv.setText(sp!=null ? sp.getservice_description():"");
		if(sp != null && !TextUtils.isEmpty(sp.getphone_sp())){
			if(!TextUtils.isEmpty(sp.getcountrycode_sp())){
				work_phone_tv.setText("+"+sp.getcountrycode_sp()+sp.getphone_sp());
			}else{
				work_phone_tv.setText(sp.getphone_sp());
			}
		}
		if(sp!= null && !TextUtils.isEmpty(sp.getemail_sp())){
			email_tv.setText(sp.getemail_sp());
		}
		if(sp!= null && !TextUtils.isEmpty(sp.getaddress_sp())){
			location_tv.setText(sp.getaddress_sp());
		}
	}

	private boolean onParseIntent() {
		sp = (ContactUser)getIntent().getSerializableExtra("service_provider");
		account = getIntent().getStringExtra("account");
		username = getIntent().getStringExtra("username");
		if(sp !=null){
			unique_id = sp.getunique_id();
		}else if(!TextUtils.isEmpty(account)){
			unique_id = BasicUserInfoHelper.stringTolong(account);
			if(unique_id == -1)
				return false;
		}else{
			return false;
		}

		return true;
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private SwitchButton.OnChangedListener onFollowChangedListener = new SwitchButton.OnChangedListener() {
		@Override
		public void OnChanged(View v, final boolean followState) {
			if (!NetworkUtil.isNetAvailable(SamchatContactUserSPNameCardActivity.this)) {
				Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
				switchButtonFollow.setCheck(!followState);
				return;
			}

			if(isFollowing || sp == null){
				switchButtonFollow.setCheck(!followState);
				return;
			}
			
			follow(followState);
        }
    };

	private void updateFollow(){
		if(FollowDataCache.getInstance().getFollowSPByUniqueID(unique_id) != null){
			switchButtonFollow.setCheck(true);
		}else{
			switchButtonFollow.setCheck(false);
		}
	}

	private void setupFollowClick(){
		switchButtonFollow.setOnChangedListener(onFollowChangedListener);
		updateFollow();
	}

	private void setupOpServiceProviderClick(){
		op_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isOpting){
					return;
				}

				if(sp != null){
					if(ContactDataCache.getInstance().getContactByUniqueID(unique_id) != null){
						removeServiceProvider();
					}else{
						addServiceProvider();
					}
				}
				
			}
		});
		updateOpServiceProvider();
	}

	private void updateOpServiceProvider(){
		if(ContactDataCache.getInstance().getContactByUniqueID(unique_id) != null){
			op_text_tv.setText(getString(R.string.samchat_delete_service_provider));
		}else{
			op_text_tv.setText(getString(R.string.samchat_add_to_service_provider));
		}
	}

	private void setupChatClick(){
		chat_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SessionHelper.startP2PSession(SamchatContactUserSPNameCardActivity.this, ""+unique_id);
				finish();
			}
		});
	}

	private void setupQrcodeClick(){
		qr_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(sp!= null)
					SamchatQRCodeActivity.start(SamchatContactUserSPNameCardActivity.this, Constants.SHOW_SP_INFO,sp);
			}
		});
	}

/**************************Data Flow Control***************************************************/
	private void follow(final boolean isFollow){
		isFollowing = true;
		SamService.getInstance().follow(isFollow,  sp, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isFollowing = false;
							if(isFollow){
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_follow_sp_succeed, Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_unfollow_sp_succeed, Toast.LENGTH_SHORT).show();
							}
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isFollowing = false;
							if(isFollow){
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_follow_sp_failed, Toast.LENGTH_SHORT).show();
							} else{
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_unfollow_sp_failed, Toast.LENGTH_SHORT).show();
							}
							switchButtonFollow.setCheck(!isFollow);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isFollowing = false;
							if(isFollow){
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_follow_sp_failed, Toast.LENGTH_SHORT).show();
							} else{
								Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_unfollow_sp_failed, Toast.LENGTH_SHORT).show();
							}
							switchButtonFollow.setCheck(!isFollow);
						}
					}, 0);
				}
		});

	}

	private void addServiceProvider(){
		isOpting = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		SamService.getInstance().add_contact(Constants.ADD_INTO_CONTACT,  sp, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isOpting = false;
							updateOpServiceProvider();
							Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_add_sp_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}
		});

	}

	private void removeServiceProvider(){
		isOpting = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		SamService.getInstance().remove_contact(Constants.REMOVE_OUT_CONTACT,  sp, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isOpting = false;
							updateOpServiceProvider();
							Toast.makeText(SamchatContactUserSPNameCardActivity.this, R.string.samchat_remove_sp_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatContactUserSPNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserSPNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}
		});

	}

	private void query_user_precise(long unique_id){
		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID, null, unique_id, null, true, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					final HttpCommClient hcc = (HttpCommClient)obj;
					if(hcc.users.getcount() > 0){
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if(!isDestroyedCompatible()){
									sp = hcc.users.getusers().get(0);
									refresh();
								}
							}
						}, 0);
					}
				}

				@Override
				public void onFailed(final int code) {}

				@Override
				public void onError(int code) {}
		});
	}
}



