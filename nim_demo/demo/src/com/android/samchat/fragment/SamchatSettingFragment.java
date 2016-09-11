package com.android.samchat.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.activity.SamchatProfileCustomerActivity;
import com.android.samchat.activity.SamchatProfileServiceProviderActivity;
import com.karics.library.zxing.android.CaptureActivity;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.netease.nim.demo.R;
import com.android.samchat.callback.SendQuestionCallback;
import android.widget.LinearLayout;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.adapter.ReceivedQuestionAdapter;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.adapter.SendQuestionAdapter;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import android.widget.RelativeLayout;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.DemoCache;
import android.view.View.OnClickListener;
import com.android.samchat.activity.SamchatCreateSPStepOneActivity;
import com.android.samchat.activity.SamchatUpdatePasswordActivity;
import com.zbar.scan.ScanCaptureAct;
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
		updateCreateSPLayout();
		customer_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
		sp_avatar.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
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


/*******************************************************************************************/



/**********************************Data Flow Control*********************************************************/


}



