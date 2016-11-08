package com.android.samchat.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import com.igexin.sdk.PushManager;
import com.android.samservice.SamService;
import com.android.samchat.receiver.PushReceiver;
import com.netease.nim.demo.receiver.CustomNotificationReceiver;

public class SamchatAppService extends IntentService {
	private Context mContext = null;
	
	public SamchatAppService(){
		super("SamchatAppService");  
	}

	@Override
	public IBinder onBind(Intent intent) {
		return super.onBind(intent);  
   }
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getBaseContext();
	}  

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);  
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);  
   }
	
	@Override
	public void setIntentRedelivery(boolean enabled) {  
		super.setIntentRedelivery(enabled);
	}  

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();

		if(action.equals(PushReceiver.action_get_msg_data)){
			String data = intent.getExtras().getString("data");
			String taskid = intent.getExtras().getString("taskid");
			String messageid = intent.getExtras().getString("messageid");
			SamService.getInstance().handlePushCmd(mContext,data);
			PushManager.getInstance().sendFeedbackMessage(mContext, taskid, messageid, 90001);
		}else if(action.equals(PushReceiver.action_get_client)){
			String clientid = intent.getExtras().getString("cid");
			SamService.getInstance().client_id_ready(clientid);
		}else if(action.equals(CustomNotificationReceiver.action_get_msg_data)){
			String data = intent.getExtras().getString("data");
			SamService.getInstance().handlePushCmd(mContext,data);
		}
   }
	
	@Override
	public void onDestroy() {
		super.onDestroy();  
	}
	
} 