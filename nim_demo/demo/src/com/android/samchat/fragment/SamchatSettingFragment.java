package com.android.samchat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.samchat.activity.SamchatProfileCustomerActivity;
import com.android.samchat.activity.SamchatProfileServiceProviderActivity;
import com.android.samservice.callback.SMCallBack;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.android.samchat.R;
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
	private LinearLayout customer_setting_layout;
	private HeadImageView customer_avatar;
	private RelativeLayout customer_profile_layout;
	private RelativeLayout customer_qr_layout;
	private RelativeLayout customer_create_sp_layout;
	private RelativeLayout update_password_layout;

	private LinearLayout sp_setting_layout;
	private HeadImageView sp_avatar;
	private RelativeLayout sp_profile_layout;
	private RelativeLayout sp_qr_layout;

	private RelativeLayout signout_layout;
	private boolean isSignout = false;
	private boolean signouting = false;

	private RelativeLayout clear_cache_layout;
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
					customer_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
					sp_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
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

		customer_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
		sp_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());

		registerBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume(){
		super.onResume();
		if(!isSignout){
			updateCreateSPLayout();
			customer_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
			sp_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
		}
	}

	private void updateCreateSPLayout(){
		if(SamService.getInstance().get_current_user() == null){
			return;
		}
		
		if(SamService.getInstance().get_current_user().getusertype() == Constants.USER
			&& SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			customer_create_sp_layout.setVisibility(View.VISIBLE);
		}else{
			customer_create_sp_layout.setVisibility(View.GONE);
		}
	}

	private void findViews() {
		customer_setting_layout = findView(R.id.customer_setting_layout);
		customer_avatar = findView(R.id.customer_avatar);
		customer_profile_layout = findView(R.id.customer_profile_layout);
		customer_qr_layout = findView(R.id.customer_qr_layout);
		customer_create_sp_layout = findView(R.id.customer_create_sp_layout);
		update_password_layout = findView(R.id.update_password_layout);

		sp_setting_layout= findView(R.id.sp_setting_layout);
		sp_avatar= findView(R.id.sp_avatar);
		sp_profile_layout= findView(R.id.sp_profile_layout);
		sp_qr_layout= findView(R.id.sp_qr_layout);

		signout_layout = findView(R.id.signout_layout);
		clear_cache_layout = findView(R.id.clear_cache_layout);

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
		setupCustomerCreateSPClick();
		setupUpdatePasswordClick();
		//sp view
       setupSPProfileClick();
		setupSPQRCodeClick();

		//signout
		setupSignoutClick();

		//clear data cache
		setupClearCacheClick();
	}

/**********************************Profile View*******************************/
	private void setupCustomerProfileClick(){
		customer_profile_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileCustomerActivity.start(getActivity());
			}
		});
	}

/**********************************Setup QR Scan View*******************************/
	private void setupCustomerQRCodeClick(){
		customer_qr_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//ScanCaptureAct.start(getActivity());
				//CaptureActivity.start(getActivity());
				SamchatQRCodeActivity.start(getActivity(),SamchatGlobal.getmode().ordinal());
			}
		});
	}

/**********************************Setup Create Service Account View*******************************/
	private void setupCustomerCreateSPClick(){
		customer_create_sp_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCreateSPStepOneActivity.start(getActivity());
			}
		});
	}

/**********************************Reset Password  View*******************************/
	private void setupUpdatePasswordClick(){
		update_password_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatUpdatePasswordActivity.start(getActivity());
			}
		});
	}

/**********************************Setup QR Scan View*******************************/
	private void setupSPQRCodeClick(){
		sp_qr_layout.setOnClickListener(new OnClickListener() {
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
		sp_profile_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatProfileServiceProviderActivity.start(getActivity());
			}
		});
	}

/**********************************Signout*******************************/
	private void setupSignoutClick(){
		signout_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isSignout = true;
				samchatLogout();
			}
		});
	}

/**********************************Signout*******************************/
	private void setupClearCacheClick(){
		clear_cache_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clearCache();
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



