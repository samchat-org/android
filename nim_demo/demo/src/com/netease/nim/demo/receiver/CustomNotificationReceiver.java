package com.netease.nim.demo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.android.samchat.service.SamchatAppService;
import com.android.samservice.Constants;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.demo.main.helper.CustomNotificationCache;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

/**
 * 自定义通知消息广播接收器
 */
public class CustomNotificationReceiver extends BroadcastReceiver {
	public static final String SERVER_ACCOUNT="200000";
	
	private static final String TAG="SamchatPushReceiver";
	public static StringBuilder payloadData = new StringBuilder();
	public static final String action_get_msg_data="samchat.service.custom.msg.GET_MSG_DATA";
    @Override
    public void onReceive(Context context, Intent intent) {
		String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION;
		if (action.equals(intent.getAction())) {
			CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
			try {
				JSONObject obj = JSONObject.parseObject(notification.getContent());
				if (obj != null && obj.getIntValue("id") == 2) {
					CustomNotificationCache.getInstance().addCustomNotification(notification);

					// Toast
					String content = obj.getString("content");
					String tip = String.format("customer notification from %s, content:", notification.getFromAccount(), content);
					Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
				}else if(obj != null && obj.getIntValue("id") == Constants.SAMCHAT_REQUEST_PUSH_ID){
					String content = obj.getString("content");
					Bundle param = new Bundle();
					LogUtil.i(TAG, "content:"+content);
					param.putString("data", content);
					Intent serviceIntent = new Intent(context,SamchatAppService.class);
					serviceIntent.setAction(action_get_msg_data);
					serviceIntent.putExtras(param);
					context.startService(serviceIntent);
				}
			} catch (JSONException e) {
				LogUtil.e("demo", e.getMessage());
			}
			LogUtil.i(TAG, "receive custom notification: " + notification.getContent() + " from :" + notification.getSessionId() + "/" + notification.getSessionType());
        }
    }
}
