package com.android.samchat.fragment;

import android.os.Bundle;
import com.netease.nim.demo.R;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.android.samchat.callback.SendQuestionCallback;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.test.TestCase;

public class SamchatRequestListFragment extends MainTabFragment {
	private SamchatRequestFragment fragment;

	public SamchatRequestListFragment() {
		setContainerId(MainTab.SAMCHAT_REQUEST.fragmentId);
        TestCase.testInitDB();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onCurrent(); 
	}

	@Override
	protected void onInit() {
		addSamchatRequestFragment();  
	}

	private void addSamchatRequestFragment() {
		fragment = new SamchatRequestFragment();
		fragment.setContainerId(R.id.samchat_request_fragment);

		UI activity = (UI) getActivity();
		fragment = (SamchatRequestFragment) activity.addFragment(fragment);  
		fragment.setSqCallback(new SendQuestionCallback(){
			@Override
			public void onSendQuestionLoaded() {
				//all send questions of current user loaded
			}

			@Override
			public void onItemClick(SendQuestion sq){

			}

			@Override
			public void onDelete(SendQuestion sq){

			}

			@Override
			public void onAdd(SendQuestion sq){

			}
			
		});

		fragment.setRqCallback(new ReceivedQuestionCallback(){
			@Override
			public void onReceivedQuestionLoaded(){

			}

			@Override
			public void onItemClick(ReceivedQuestion rq){

			}

			@Override
			public void onDelete(ReceivedQuestion rq){

			}

			@Override
			public void onAdd(ReceivedQuestion rq){

			}
		});
	}
	@Override
	public void onCurrentTabClicked() {

	}
}


