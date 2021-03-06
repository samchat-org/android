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
import com.android.samchat.cache.CustomerDataCache;
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
import com.netease.nim.uikit.common.util.string.ConvertHelper;
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
public class SamchatContactUserNameCardActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatContactUserNameCardActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private RelativeLayout titlebar_layout;
	private TextView titlebar_name_textview;
	private ImageView back_icon_iv;
	private HeadImageView avatar_headimageview;
	private RelativeLayout chat_layout;
	private RelativeLayout username_layout;
	private RelativeLayout op_layout;
	private TextView op_text_tv;
	private TextView phone_tv;
	private TextView email_tv;
	private TextView location_tv;
	private ImageView wall_iv;
	private TextView username_tv;

	private ContactUser user;
	private String account;
	private String username;

	private long unique_id;
	
	private boolean opposite=false;

	private boolean isOpting=false;

	public static void start(Context context,ContactUser user, boolean oppo) {
		Intent intent = new Intent(context, SamchatContactUserNameCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("customer", user);
		bundle.putBoolean("opposite",oppo);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	public static void start(Context context,String id, String name, boolean oppo) {
		Intent intent = new Intent(context, SamchatContactUserNameCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putString("account",id);
		bundle.putString("username",name);
		bundle.putBoolean("opposite",oppo);
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
		setContentView(R.layout.samchat_contactusernamecard_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		if(onParseIntent()){
			query_user_precise(unique_id);
			setupPanel();
		}else{
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_layout = findView(R.id.titlebar_layout);
		back_icon_iv = findView(R.id.back_icon);
		titlebar_name_textview = findView(R.id.titlebar_name);
		avatar_headimageview = findView(R.id.avatar);
		chat_layout = findView(R.id.chat_layout);
		op_layout = findView(R.id.op_layout);
		username_layout = findView(R.id.username_layout);
		phone_tv = findView(R.id.phone);
		email_tv = findView(R.id.email);
		location_tv = findView(R.id.location);
		wall_iv = findView(R.id.wall);
		op_text_tv = findView(R.id.op_text);
		username_tv = findView(R.id.username_text);

		setupBackArrowClick();
		setupChatClick();
		setupOpCustomerClick();
		setupUsernameClick();
		
		if(opposite){
			titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
			back_icon_iv.setImageResource(R.drawable.samchat_arrow_left);
			back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
			titlebar_name_textview.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));	
		}

		refresh();
	}

	private void refresh(){
		if(user != null && !TextUtils.isEmpty(user.getAvatar())){
				avatar_headimageview.loadBuddyAvatarByUrl(user.getAccount(), user.getAvatar(),(int) getResources().getDimension(R.dimen.samchat_avatar_size_in_namecard), new HeadImageView.OnImageLoadedListener(){
				@Override
				public void OnImageLoadedListener(Bitmap bitmap){
					FastBlurUtils.blur(bitmap, wall_iv);
				}
			});
		}else{
			avatar_headimageview.setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());
		}
		
		titlebar_name_textview.setText(user!=null ?user.getusername():username);
		username_tv.setText(user!=null ?user.getusername():username);
		if(user!= null && !TextUtils.isEmpty(user.getcellphone())){
			if(!TextUtils.isEmpty(user.getcountrycode())){
				phone_tv.setText("+"+user.getcountrycode()+user.getcellphone());
			}else{
				phone_tv.setText(user.getcellphone());
			}
		}
		if(user != null && !TextUtils.isEmpty(user.getemail())){
			email_tv.setText(user.getemail());
		}
		if(user != null  && !TextUtils.isEmpty(user.getaddress())){
			location_tv.setText(user.getaddress());
		}		
	}

	private boolean onParseIntent() {
		user = (ContactUser)getIntent().getSerializableExtra("customer");
		account = getIntent().getStringExtra("account");
		username = getIntent().getStringExtra("username");
		opposite = getIntent().getBooleanExtra("username",false);

		if(user!=null){
			unique_id = user.getunique_id();
		}else if(!TextUtils.isEmpty(account)){
			unique_id = ConvertHelper.stringTolong(account);
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

	private void setupOpCustomerClick(){
		op_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isOpting){
					return;
				}

				if(user != null){
					if(CustomerDataCache.getInstance().getCustomerByUniqueID(unique_id) != null){
						removeCustomer();
					}else{
						addCustomer();
					}
				}
			}
		});
		updateOpCustomer();
		if(opposite){
			op_layout.setVisibility(View.GONE);
		}
	}

	private void updateOpCustomer(){
		if(CustomerDataCache.getInstance().getCustomerByUniqueID(unique_id) != null){
			op_text_tv.setText(getString(R.string.samchat_delete_customer));
		}else{
			op_text_tv.setText(getString(R.string.samchat_add_to_customer));
		}
	}

	private void setupChatClick(){
		chat_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SessionHelper.startP2PSession(SamchatContactUserNameCardActivity.this, ""+unique_id);
				finish();
			}
		});
		if(opposite){
			chat_layout.setVisibility(View.GONE);
		}
	}

	private void setupUsernameClick(){
		username_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(user != null){
					SamchatQRCodeActivity.start(SamchatContactUserNameCardActivity.this, Constants.SHOW_CUSTOMER_INFO, user);
				}
			}
		});
	}

/**************************Data Flow Control***************************************************/
	private void addCustomer(){
		isOpting = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		SamService.getInstance().add_contact(Constants.ADD_INTO_CUSTOMER,  user, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isOpting = false;
							updateOpCustomer();
							Toast.makeText(SamchatContactUserNameCardActivity.this, R.string.samchat_add_sp_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatContactUserNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatContactUserNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}
		});

	}

	private void removeCustomer(){
		isOpting = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		SamService.getInstance().remove_contact(Constants.REMOVE_OUT_CUSTOMER,  user, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isOpting = false;
							updateOpCustomer();
							Toast.makeText(SamchatContactUserNameCardActivity.this, R.string.samchat_remove_sp_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatContactUserNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserNameCardActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isOpting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatContactUserNameCardActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatContactUserNameCardActivity.this, null,
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
									user = hcc.users.getusers().get(0);
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




