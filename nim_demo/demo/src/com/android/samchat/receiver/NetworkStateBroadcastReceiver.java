package com.android.samchat.receiver;

import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.net.ConnectivityManager;  
import android.net.NetworkInfo.State;

import com.android.samservice.NetworkMonitor;
import com.android.samservice.SamService;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {  
  
	@Override  
	public void onReceive(Context context, Intent intent) {  
		if(NetworkMonitor.isNetworkAvailable()){
			SamService.getInstance().network_available();
		}
	}  
} 