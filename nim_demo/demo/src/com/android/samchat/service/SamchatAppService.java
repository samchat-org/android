package com.android.samchat.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import com.android.samservice.SamService;
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

		if(action.equals(CustomNotificationReceiver.action_get_msg_data)){
			String data = intent.getExtras().getString("data");
			SamService.getInstance().handlePushCmd(mContext,data);
		}
   }
	
	@Override
	public void onDestroy() {
		super.onDestroy();  
	}
	
} 