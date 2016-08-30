package com.android.samchat.fragment;

import android.os.Bundle;

import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.demo.R;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.android.samchat.callback.SendQuestionCallback;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.callback.CustomerPublicCallback;

public class SamchatPublicListFragment extends MainTabFragment {
	private SamchatPublicFragment fragment;

	public SamchatPublicListFragment() {
		setContainerId(MainTab.SAMCHAT_PUBLIC.fragmentId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onCurrent(); 
	}

	@Override
	protected void onInit() {
		addSamchatPublicFragment();  
	}

	private void addSamchatPublicFragment() {
		fragment = new SamchatPublicFragment();
		fragment.setContainerId(R.id.samchat_public_fragment);

		UI activity = (UI) getActivity();
		fragment = (SamchatPublicFragment) activity.addFragment(fragment);  
		fragment.setCpCallback(new CustomerPublicCallback(){
			@Override
			public void onCustomerPublicLoaded() {
				//all followed public loaded
			}

			@Override
			public void onItemClick(FollowedSamPros fsp){
				
			}

			@Override
			public void onDelete(){

			}

			@Override
			public void onAdd(){

			}	
		});


	}
	@Override
	public void onCurrentTabClicked() {

	}
}





