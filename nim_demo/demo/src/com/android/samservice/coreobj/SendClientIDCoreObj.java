package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SendClientIDCoreObj extends SamCoreObj{
	public String token;
	public String clientid;

	public SendClientIDCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,String clientid){
		this.token = token;
		this.clientid = clientid;
	}
	
}
