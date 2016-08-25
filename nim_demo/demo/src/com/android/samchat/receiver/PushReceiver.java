package com.android.samchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.android.samchat.service.SamchatAppService;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.io.UnsupportedEncodingException;

public class PushReceiver extends BroadcastReceiver {
	public static StringBuilder payloadData = new StringBuilder();
	public static final String action_get_msg_data="samchat.service.msg.GET_MSG_DATA";
	public static final String action_get_client="samchat.service.msg.GET_CLIENTID";

    private String decodeString(byte[] payload){
        try{
            return new String(payload, "utf8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return new String(payload);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.e("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        LogUtil.e("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                //receive transparent MSG
                byte[] payload = bundle.getByteArray("payload");
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                //push feedback message to Push Server with action id: 90000-90999
                boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);

                if (payload != null) {		
                    String data = decodeString(payload);
                    Bundle param = new Bundle();
                    Log.d("test", "payload json:"+data);
                    param.putString("data", data);
                    param.putString("taskid",taskid);
                    param.putString("messageid",messageid);
                    Intent serviceIntent = new Intent(context,SamchatAppService.class);
                    serviceIntent.setAction(action_get_msg_data);
                    serviceIntent.putExtras(param);
                    context.startService(serviceIntent);
                }
                break;

            case PushConsts.GET_CLIENTID:
                // Get ClientID(CID): need push CID back to APP server 
                String cid = bundle.getString("clientid");
					Bundle param = new Bundle();
					param.putString("cid",cid);
					Intent serviceIntent = new Intent(context,SamchatAppService.class);
					serviceIntent.putExtras(param);
					serviceIntent.setAction(action_get_client);
					context.startService(serviceIntent);
                break;
								
            case PushConsts.GET_SDKONLINESTATE:
                boolean online = bundle.getBoolean("onlineState");
                Log.d("GetuiSdkDemo", "online = " + online);
                break;

            case PushConsts.THIRDPART_FEEDBACK:
                break;

            default:
                break;
        }
    }
}

