package com.android.samchat.fragment;

import android.os.Bundle;

import com.android.samchat.SamchatGlobal;
import com.android.samchat.activity.SamchatRequestDetailsActivity;
import com.android.samchat.service.SamDBManager;
import com.android.samchat.type.ModeEnum;
import com.android.samservice.Constants;
import com.android.samservice.QuestionInfo;
import com.android.samchat.R;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.android.samchat.callback.SendQuestionCallback;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.android.samservice.info.ReceivedQuestion;

public class SamchatRequestListFragment extends MainTabFragment {
	private SamchatRequestFragment fragment;

	public SamchatRequestListFragment() {
		setContainerId(MainTab.SAMCHAT_REQUEST.fragmentId);
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
				QuestionInfo qinfo = new QuestionInfo();
				qinfo.setquestion(sq.getquestion());
				qinfo.setquestion_id(sq.getquestion_id());
				qinfo.setdatetime(sq.getdatetime());
				qinfo.setaddress(sq.getaddress());
				SamchatRequestDetailsActivity.start(getActivity(),  qinfo);
			}

			@Override
			public void onDelete(SendQuestion sq){

			}

			@Override
			public void onAdd(SendQuestion sq){

			}

			@Override
			public void onUnreadCountChange(int unreadCount){
				if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
					ReminderManager.getInstance().updateRequestUnreadNum(unreadCount);
				} 
				((MainActivity)getActivity()).setrequest_unread_count_customer(unreadCount);
			}
			
		});

		fragment.setRqCallback(new ReceivedQuestionCallback(){
			@Override
			public void onReceivedQuestionLoaded(){

			}

			@Override
			public void onItemClick(ReceivedQuestion rq){
				if(rq.getstatus() == Constants.QUESTION_NOT_RESPONSED ){
					SamDBManager.getInstance().asyncInsertReceivedQuestionMessage(rq);
					SessionHelper.startP2PSessionWithReceiveQuestion(getActivity(),""+rq.getsender_unique_id(), rq.getquestion_id());
				}else{
					SessionHelper.startP2PSession(getActivity(), ""+rq.getsender_unique_id());
				}

				if(rq.getunread() == Constants.QUESTION_UNREAD){
					SamDBManager.getInstance().asyncClearReceivedQuestionUnreadCount(rq.getquestion_id());
				}
			}

			@Override
			public void onDelete(ReceivedQuestion rq){

			}

			@Override
			public void onAdd(ReceivedQuestion rq){

			}

			@Override
			public void onUnreadCountChange(int unreadCount){
				if(SamchatGlobal.getmode() == ModeEnum.SP_MODE){
					ReminderManager.getInstance().updateRequestUnreadNum(unreadCount);
				} 
				((MainActivity)getActivity()).setrequest_unread_count_sp(unreadCount);
			}
		});
	}
	@Override
	public void onCurrentTabClicked() {

	}

	
}


