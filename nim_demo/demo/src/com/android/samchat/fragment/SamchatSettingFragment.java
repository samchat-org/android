package com.android.samchat.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.samchat.activity.SamchatProfileCustomerActivity;
import com.android.samchat.activity.SamchatProfileServiceProviderActivity;
import com.android.samchat.common.FastBlurUtils;
import com.android.samservice.callback.SMCallBack;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.android.samchat.R;

import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.samservice.SamService;
import com.android.samchat.SamchatGlobal;

import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import android.widget.RelativeLayout;
import com.netease.nim.demo.config.preference.Preferences;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.activity.SamchatCreateSPStepOneActivity;
import com.android.samchat.activity.SamchatUpdatePasswordActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.android.samchat.activity.SamchatQRCodeActivity;
/**
 * Main Fragment in SamchatSettingListFragment
 */
public class SamchatSettingFragment extends TFragment {
	private RelativeLayout avatar_layout;
	private HeadImageView avatar_hiv;
	private ImageView wall_iv;

	private LinearLayout customer_setting_layout;
	private LinearLayout customer_my_profile_layout;
	private LinearLayout customer_my_qrcode_layout;
	private LinearLayout customer_change_password_layout;
	private LinearLayout customer_notificaition_layout;
	private LinearLayout customer_logout_layout;
	private LinearLayout customer_clear_cache_layout;
	private LinearLayout customer_create_sp_layout;
	private LinearLayout customer_about_layout;
	private LinearLayout customer_faq_layout;
	private TextView learn_more_tv;
	private ImageView create_sp_img_iv;
	private TextView create_sp_text_tv;

	private LinearLayout sp_setting_layout;
	private LinearLayout sp_my_profile_layout;
	private LinearLayout sp_my_qrcode_layout;
	private LinearLayout sp_switch_layout;
	
	private boolean isSignout = false;
	private boolean signouting = false;

	private boolean clearing = false;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void switchMode(ModeEnum to){
		if(to == ModeEnum.CUSTOMER_MODE){
			customer_setting_layout.setVisibility(View.VISIBLE);
			sp_setting_layout.setVisibility(View.GONE);
		}else{
			customer_setting_layout.setVisibility(View.GONE);
			sp_setting_layout.setVisibility(View.VISIBLE);
		}
	}

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_MYSELF_AVATAR_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
						switchMode(ModeEnum.CUSTOMER_MODE);
					}else{
						switchMode(ModeEnum.SP_MODE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_MYSELF_AVATAR_UPDATE)){
					avatar_hiv.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90, new HeadImageView.OnImageLoadedListener(){
						@Override
						public void OnImageLoadedListener(Bitmap bitmap){
							FastBlurUtils.blur(bitmap, wall_iv);
						}
					});
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	public SamchatSettingFragment(){
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.samchat_setting_fragment_layout, container, false);
	}

    @Override
    public void onDestroyView(){
        unregisterBroadcastReceiver();
        super.onDestroyView();
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupSettingPanel();

		avatar_hiv.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 90, new HeadImageView.OnImageLoadedListener(){
			@Override
			public void OnImageLoadedListener(Bitmap bitmap){
				FastBlurUtils.blur(bitmap, wall_iv);
			}
		});

		registerBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume(){
		super.onResume();
		if(!isSignout && SamService.getInstance().get_current_user()!=null){
			updateCustomerCreateSpLayout();
		}
	}

	private void updateCustomerCreateSpLayout(){
		if(SamService.getInstance().get_current_user() == null){
			return;
		}
		
		if(SamService.getInstance().get_current_user().getusertype() == Constants.USER){
			create_sp_img_iv.setImageResource(R.drawable.samchat_setting_list_my_service);
			create_sp_text_tv.setText(R.string.samchat_create_sp);
			learn_more_tv.setVisibility(View.VISIBLE);
		}else if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			create_sp_img_iv.setImageResource(R.drawable.samchat_setting_switch_icon);
			create_sp_text_tv.setText(R.string.samchat_switch_to_service_account);
			learn_more_tv.setVisibility(View.GONE);
		}
	}

	private void findViews() {
		avatar_layout = findView(R.id.avatar_layout);
		avatar_hiv = findView(R.id.avatar);
		wall_iv = findView(R.id.wall);

		customer_setting_layout= findView(R.id.customer_setting_layout);
		customer_my_profile_layout= findView(R.id.customer_my_profile_layout);
		customer_my_qrcode_layout= findView(R.id.customer_my_qrcode_layout);
		customer_change_password_layout= findView(R.id.customer_change_password_layout);
		customer_notificaition_layout= findView(R.id.customer_notificaition_layout);
		customer_logout_layout= findView(R.id.customer_logout_layout);
		customer_clear_cache_layout= findView(R.id.customer_clear_cache_layout);
		customer_create_sp_layout= findView(R.id.customer_create_sp_layout);
		customer_about_layout= findView(R.id.customer_about_layout);
		customer_faq_layout= findView(R.id.customer_faq_layout);

		sp_setting_layout = findView(R.id.sp_setting_layout);
		sp_my_profile_layout= findView(R.id.sp_my_profile_layout);
		sp_my_qrcode_layout= findView(R.id.sp_my_qrcode_layout);
		sp_switch_layout= findView(R.id.sp_switch_layout);

		learn_more_tv = findView(R.id.learn_more);
		create_sp_img_iv = findView(R.id.create_sp_img);
		create_sp_text_tv = findView(R.id.create_sp_text);

		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			switchMode(ModeEnum.CUSTOMER_MODE);
		}else{
			switchMode(ModeEnum.SP_MODE);
		}
    }

	private void setupSettingPanel(){
		findViews();
		//customer view
		setupCustomerProfileClick();
		setupCustomerQRCodeClick();
		setupCustomerChangePasswordClick();
		setupCustomerNotificationClick();
		setupCustomerSignoutClick();
		setupCustomerClearCacheClick();
		setupCustomerCreateSPClick();
		setupCustomerLearnMoreClick();
		setupCustomerAboutClick();
		setupCustomerFAQClick();
		
		//sp view
       setupSPProfileClick();
		setupSPQRCodeClick();
		setupSPSwitchClick();
	}

/**********************************Profile View*******************************/
	private void setupCustomerProfileClick(){
		customer_my_profile_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileCustomerActivity.start(getActivity());
			}
		});
	}

/**********************************Setup QR Scan View*******************************/
	private void setupCustomerQRCodeClick(){
		customer_my_qrcode_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//ScanCaptureAct.start(getActivity());
				//CaptureActivity.start(getActivity());
				SamchatQRCodeActivity.start(getActivity(),SamchatGlobal.getmode().ordinal());
			}
		});
	}
/**********************************Setup Change paasword View*******************************/
	private void setupCustomerChangePasswordClick(){
		customer_change_password_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatUpdatePasswordActivity.start(getActivity());
			}
		});
	}
/**********************************Setup Notification View*******************************/
	private void setupCustomerNotificationClick(){
		customer_notificaition_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			
			}
		});
	}

/**********************************Signout*******************************/
	private void setupCustomerSignoutClick(){
		customer_logout_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isSignout = true;
				samchatLogout();
			}
		});
	}

/**********************************Signout*******************************/
	private void setupCustomerClearCacheClick(){
		customer_clear_cache_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clearCache();
			}
		});
	}

/**********************************Setup Create Service Account View*******************************/
	private void setupCustomerCreateSPClick(){
		customer_create_sp_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(SamService.getInstance().get_current_user().getusertype() == Constants.USER){
					SamchatCreateSPStepOneActivity.start(getActivity());
				}else{
					((MainActivity)getActivity()).switchMode();
				}
			}
		});
	}

/**********************************Setup learn more*******************************/
	private void setupCustomerLearnMoreClick(){
		learn_more_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}

/**********************************Setup About*******************************/
	private void setupCustomerAboutClick(){
		customer_about_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}

/**********************************Setup About*******************************/
	private void setupCustomerFAQClick(){
		customer_faq_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}



/**********************************Setup QR Scan View*******************************/
	private void setupSPQRCodeClick(){
		sp_my_qrcode_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//ScanCaptureAct.start(getActivity());
				//CaptureActivity.start(getActivity());
				SamchatQRCodeActivity.start(getActivity(),SamchatGlobal.getmode().ordinal());
			}
		});
	}
/**********************************Profile View*******************************/
	private void setupSPProfileClick(){
		sp_my_profile_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileServiceProviderActivity.start(getActivity());
			}
		});
	}

/**********************************Profile View*******************************/
	private void setupSPSwitchClick(){
		sp_switch_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((MainActivity)getActivity()).switchMode();
			}
		});
	}





/*******************************************************************************************/



/**********************************Data Flow Control*********************************************************/
	private void logout() {
		removeLoginState();
		MainActivity.logout(getActivity(), false);
		NIMClient.getService(AuthService.class).logout();
		DialogMaker.dismissProgressDialog();
	}

	private void removeLoginState() {
		Preferences.saveUserToken("");
		Preferences.saveUserAccount("");
	}
		
	private void samchatLogout(){
		if(signouting){
			return;
		}
		signouting = true;
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().signout(new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							logout();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							logout();
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							logout();
						}
					}, 0);
				}

		});
    }

	private void clearCache(){
		if(clearing){
			return;
		}
		clearing = true;
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().clear_cache(new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							Toast.makeText(getActivity(), getString(R.string.samchat_clear_cache_finish), Toast.LENGTH_SHORT).show();
							clearing = false;
						}
					}, 0);
					
				}

				@Override
				public void onFailed(int code) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							Toast.makeText(getActivity(), getString(R.string.samchat_clear_cache_finish), Toast.LENGTH_SHORT).show();
							clearing = false;
						}
					}, 0);
					
				}

				@Override
				public void onError(int code) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							Toast.makeText(getActivity(), getString(R.string.samchat_clear_cache_finish), Toast.LENGTH_SHORT).show();
							clearing = false;
						}
					}, 0);
					
				}

		});
    }
}



