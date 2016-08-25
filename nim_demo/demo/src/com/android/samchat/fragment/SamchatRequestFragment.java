package com.android.samchat.fragment;

import android.app.Activity;
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

import com.android.samchat.service.SamDBManager;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimConstants;
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
import java.util.Map;

import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samchat.activity.SamchatNewRequestActivity;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import android.view.View.OnClickListener;
/**
 * Main Fragment in SamchatRequestListFragment
 */
public class SamchatRequestFragment extends TFragment {
	public static final int REQUEST_CODE_NEW_QUESTION = 0x1000;
	/*customer mode*/
	//view
	private LinearLayout customer_request_layout;
	private TextView make_new_service_request;
	private ListView customer_request_list;
	
	//data
	private List<SendQuestion> sendquestions;
	private SendQuestionAdapter sqadapter;
	private boolean sendQuestionsLoaded = false;
	private SendQuestionCallback sqcallback;
	
	/*sp mode*/
	//view
	private LinearLayout sp_request_layout;
	private ListView sp_request_list;
	//data
	private List<ReceivedQuestion> rcvdquestions;
	private ReceivedQuestionAdapter rqadapter;
	private boolean rcvdQuestionsLoaded = false;
	private ReceivedQuestionCallback rqcallback;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void switchMode(ModeEnum to){
		if(to == ModeEnum.SP_MODE){
			
		}else{

		}
	}

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_CUSTOMER_ITEMS_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.SP_MODE)){
						customer_request_layout.setVisibility(View.GONE);
						sp_request_layout.setVisibility(View.VISIBLE);
					}else{
						customer_request_layout.setVisibility(View.VISIBLE);
						sp_request_layout.setVisibility(View.GONE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_CUSTOMER_ITEMS_UPDATE)){
					SamDBManager.getInstance().asyncQuerySendQuestion(new NIMCallback(){
					@Override
					public void onResult(Object obj1, Object obj2, int code) {
						if(obj1 == null){
							return;
						}
						final List<SendQuestion> sqs = (List<SendQuestion>)obj1;
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if(isAdded()){
									loadedSendQuestions = sqs;
									onSendQuestionsLoaded();
								}
							}
						},0);
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

	public SamchatRequestFragment(){
		super();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.e("test", "requestCode:"+requestCode+"data:"+data);
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_NEW_QUESTION && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				SendQuestion sq = (SendQuestion)data.getSerializableExtra("send_question");
				updateSendQuestionItems(sq);
				refreshSendQuestionList();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.samchat_request_fragment_layout, container, false);
	}

    @Override
    public void onDestroyView(){
		unregisterBroadcastReceiver();
		registerObservers(false);
		super.onDestroyView();
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setupPanel();
		
		initSendQuestionList();
		initRcvdQuestionList();
		
		LoadSendQuestions(true);
		LoadRcvdQuestions(true);

		registerBroadcastReceiver();
		registerObservers(true);
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void findViews() {
		//customer mode views
		customer_request_layout = (LinearLayout) findView(R.id.customer_request_layout);
		customer_request_list = (ListView) findView(R.id.customer_request_list);
		make_new_service_request = (TextView) findView(R.id.make_new_service_request);
		//sp mode views
		sp_request_layout = (LinearLayout) findView(R.id.sp_request_layout);
		sp_request_list = (ListView) findView(R.id.sp_request_list);

		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			customer_request_layout.setVisibility(View.VISIBLE);
			sp_request_layout.setVisibility(View.GONE);
		}else{
			customer_request_layout.setVisibility(View.GONE);
			sp_request_layout.setVisibility(View.VISIBLE);
		}
    }

	private void setupPanel(){
		findViews();

		setupMakeNewServiceRequestClick();
	}

	private void setupMakeNewServiceRequestClick(){
		make_new_service_request.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatNewRequestActivity.startActivityForResult(getActivity(),SamchatRequestFragment.this, REQUEST_CODE_NEW_QUESTION);
			}
		});
	}

	private void initSendQuestionList(){
		sendquestions = new ArrayList<SendQuestion>();
		sqadapter = new SendQuestionAdapter(getActivity(),sendquestions);
		customer_request_list.setAdapter(sqadapter);
		customer_request_list.setItemsCanFocus(true);
		customer_request_list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (sqcallback != null) {
					SendQuestion sq = (SendQuestion) parent.getAdapter().getItem(position);
					if(sq != null){
						sqcallback.onItemClick(sq);
					}
				}
			}
		});

		customer_request_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
		
	}

	private List<SendQuestion> loadedSendQuestions;
	private void LoadSendQuestions(boolean delay){
		if(sendQuestionsLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(sendQuestionsLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}
				
				loadedSendQuestions = SamService.getInstance().getDao().query_SendQuestion_db_ALL();
				sendQuestionsLoaded = true;
				if(isAdded()){
					onSendQuestionsLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onSendQuestionsLoaded(){
		sendquestions.clear();
		if(loadedSendQuestions!=null){
			sendquestions.addAll(loadedSendQuestions);
			loadedSendQuestions = null;
		}

		refreshSendQuestionList();

		if(sqcallback!=null){
			sqcallback.onSendQuestionLoaded();
		}
		
	}

	private void notifyDataSetChangedSQ() {
		int history = findSqHistoryItemId();
		sqadapter.sethistory(history);
		sqadapter.notifyDataSetChanged();
	}

	private void refreshSendQuestionList(){
		sortSendQuestion(sendquestions);
		notifyDataSetChangedSQ();
	}

	private int findSqHistoryItemId(){
		int found=-1;
		long history = SamchatGlobal.getoneWeekSysTime();
		for(int i=0;i<sendquestions.size();i++){
			if(sendquestions.get(i).getlatest_answer_time() == 0){
				if(sendquestions.get(i).getdatetime() < history ){
					found = i;
					break;
				}
			}else{
				if(sendquestions.get(i).getlatest_answer_time() < history){
					found = i;
					break;
				}
			}
		}

		return found;
	}

	public void setSqCallback(SendQuestionCallback callback) {
		sqcallback = callback;
	}

	//Sort Send Question By latest received answer time
	private void sortSendQuestion(List<SendQuestion> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, sqcomp);
	}

	private static Comparator<SendQuestion> sqcomp = new Comparator<SendQuestion>() {
		@Override
		public int compare(SendQuestion o1, SendQuestion o2) {
			long o1Time = (o1.getlatest_answer_time() == 0? o1.getdatetime() : o1.getlatest_answer_time());
			long o2Time = (o2.getlatest_answer_time() == 0? o2.getdatetime() : o2.getlatest_answer_time());
			long time = o1Time - o2Time;
			return time == 0 ? 0 : (time > 0 ? -1 : 1);
		}
	};

	private void updateSendQuestionItems(SendQuestion sq){
		int index = -1;
		for(int i = 0; i<sendquestions.size();i++){
			if(sendquestions.get(i).getquestion_id() == sq.getquestion_id()){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			sendquestions.remove(index);
		}

		sendquestions.add(sq);
	}

/*************************************Service Provide Mode***************************/
	private void initRcvdQuestionList(){
		rcvdquestions = new ArrayList<ReceivedQuestion>();
		rqadapter = new ReceivedQuestionAdapter(getActivity(),rcvdquestions);
		sp_request_list.setAdapter(rqadapter);
		sp_request_list.setItemsCanFocus(true);
		sp_request_list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (rqcallback != null) {
					ReceivedQuestion rq = (ReceivedQuestion) parent.getAdapter().getItem(position);
					if(rq != null){
						rqcallback.onItemClick(rq);
					}
				}
			}
		});

		sp_request_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
	}

	private List<ReceivedQuestion> loadedRcvdQuestions;
	private void LoadRcvdQuestions(boolean delay){
		if(rcvdQuestionsLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(rcvdQuestionsLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}
				
				loadedRcvdQuestions = SamService.getInstance().getDao().query_ReceivedQuestion_db_by_timestamp(SamchatGlobal.getoneWeekSysTime(),true);
				rcvdQuestionsLoaded = true;
				if(isAdded()){
					onRcvdQuestionsLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onRcvdQuestionsLoaded(){
		rcvdquestions.clear();
		if(loadedRcvdQuestions!=null){
			rcvdquestions.addAll(loadedRcvdQuestions);
			loadedRcvdQuestions = null;
		}

		refreshRcvdQuestionList();

		if(rqcallback!=null){
			rqcallback.onReceivedQuestionLoaded();
		}
		
	}

	private void notifyDataSetChangedRQ() {
		rqadapter.notifyDataSetChanged();
	}

	private int findRqAnsweredItemId(){
		int found=-1;
		for(int i=0;i<rcvdquestions.size();i++){
			if(rcvdquestions.get(i).getstatus() == Constants.QUESTION_RESPONSED){
				found = i;
				break;
			}
		}

		return found;
	}

	private void refreshRcvdQuestionList(){
		sortRcvdQuestion(rcvdquestions);
		int answered = findRqAnsweredItemId();
		rqadapter.setanswered(answered);
		notifyDataSetChangedRQ();
	}

	public void setRqCallback(ReceivedQuestionCallback callback) {
		rqcallback = callback;
	}

	//Sort Received Question By received question time
	private void sortRcvdQuestion(List<ReceivedQuestion> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, rqcomp);
	}

	private static Comparator<ReceivedQuestion> rqcomp = new Comparator<ReceivedQuestion>() {
		@Override
		public int compare(ReceivedQuestion o1, ReceivedQuestion o2) {
			int answered = o1.getstatus() - o2.getstatus();
			if(answered > 0){
				return answered > 0 ? 1 : -1;
			}else{
				long o1Time = o1.getdatetime();
				long o2Time = o2.getdatetime();;
				long time = o1Time - o2Time;
				return time == 0 ? 0 : (time > 0 ? -1 : 1);
			}
		}
	};

	private void updateReceivedQuestionItems(ReceivedQuestion rq){
		int index = -1;
		for(int i = 0; i<rcvdquestions.size();i++){
			if(rcvdquestions.get(i).getquestion_id() == rq.getquestion_id()){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			rcvdquestions.remove(index);
		}

		rcvdquestions.add(rq);
	}


/*************************************Observers*******************************************************/
	private void registerObservers(boolean register) {
		MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
		service.observeMsgStatus(statusObserver, register);
		SamDBManager.getInstance().registerReceivedQuestionObserver(receviedQuestionObserver,  register);
		SamDBManager.getInstance().registerSendQuestionUnreadClearObserver(sendQuestionUnreadClearObserver,register);
	}

	SamchatObserver< ReceivedQuestion> receviedQuestionObserver = new SamchatObserver < ReceivedQuestion >(){
		@Override
		public void onEvent(final ReceivedQuestion rq){
			getActivity().runOnUiThread(new Runnable() {
           	@Override
            	public void run() {
					updateReceivedQuestionItems(rq);
					refreshRcvdQuestionList();
				}
			});
		}
	};

	Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
		@Override
		public void onEvent(IMMessage message){
			if(message.	getDirect() == MsgDirectionEnum.Out && message.getStatus() == MsgStatusEnum.success){
				Map<String, Object> content = message.getRemoteExtension();
				if(content != null && content.containsKey(NimConstants.QUEST_ID)){
					String quest_id = (String)content.get(NimConstants.QUEST_ID);
					SamDBManager.getInstance().asyncUpdateReceivedQuestionStatusToResponse(Long.valueOf(quest_id));
				}
			}
		}
	};

	SamchatObserver< SendQuestion> sendQuestionUnreadClearObserver = new SamchatObserver < SendQuestion >(){
		@Override
		public void onEvent(final SendQuestion sq){
			getActivity().runOnUiThread(new Runnable() {
           	@Override
            	public void run() {
					updateSendQuestionItems(sq);
					refreshSendQuestionList();
				}
			});
		}
	};
 
}

