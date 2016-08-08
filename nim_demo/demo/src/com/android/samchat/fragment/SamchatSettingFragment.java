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
import com.android.samservice.info.FollowUser;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.SamProsUser;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import android.widget.RelativeLayout;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.DemoCache;
/**
 * Main Fragment in SamchatSettingListFragment
 */
public class SamchatSettingFragment extends TFragment {
	private TextView account_label;
	private HeadImageView avatar;
	private RelativeLayout profile_layout;
	private TextView profile;
	private RelativeLayout qr_layout;
	private TextView qr_code;
	private TextView preference_label;
	private TextView notification;
	private TextView option;
	private TextView about_label;
	private TextView about;
	private TextView faq;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void switchMode(ModeEnum to){
		if(to == ModeEnum.CUSTOMER_MODE){
			account_label.setText(getString(R.string.samchat_account));
			profile.setText(getString(R.string.samchat_myprofile));
			qr_code.setText(getString(R.string.samchat_myqr));
			preference_label.setText(getString(R.string.samchat_preference));
			notification.setText(getString(R.string.samchat_notification));
			option.setText(getString(R.string.samchat_option));
			about_label.setText(getString(R.string.samchat_about));
			about_label.setVisibility(View.VISIBLE);
			
			about.setText(getString(R.string.samchat_aboutSamchat));
			about.setVisibility(View.VISIBLE);
			
			faq.setText(getString(R.string.samchat_faq));
			faq.setVisibility(View.VISIBLE);

			avatar.loadBuddyAvatar(DemoCache.getTAccount());
		}else{
			account_label.setText(getString(R.string.samchat_service_account));
			profile.setText(getString(R.string.samchat_service_profile));
			qr_code.setText(getString(R.string.samchat_service_qr));
			preference_label.setText(getString(R.string.samchat_service_preference));
			notification.setText(getString(R.string.samchat_service_notification));
			option.setText(getString(R.string.samchat_service_subscription));
			about_label.setVisibility(View.GONE);
			about.setVisibility(View.GONE);
			faq.setVisibility(View.GONE);
			avatar.loadBuddyAvatar(DemoCache.getTAccount());
		}
	}

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);

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
		findViews();

		registerBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void findViews() {
		account_label = (TextView) findView(R.id.account_label);
		avatar = (HeadImageView) findView(R.id.avatar);
		profile_layout = (RelativeLayout) findView(R.id.profile_layout);
		profile = (TextView) findView(R.id.profile);
		qr_layout = (RelativeLayout) findView(R.id.qr_layout);
		qr_code = (TextView) findView(R.id.qr_code);
		preference_label = (TextView) findView(R.id.preference_label);
		notification =  (TextView) findView(R.id.notification);
		option =  (TextView) findView(R.id.option);
		about_label = (TextView) findView(R.id.about_label);
		about = (TextView) findView(R.id.about);
		faq = (TextView) findView(R.id.faq);

		if(SamchatGlobal.getmode()== ModeEnum.CUSTOMER_MODE){
			switchMode(ModeEnum.CUSTOMER_MODE);
		}else{
			switchMode(ModeEnum.SP_MODE);
		}
    }


}



