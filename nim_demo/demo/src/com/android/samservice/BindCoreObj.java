package com.android.samservice;

public class BindCoreObj extends SamCoreObj{
	public String token;
	public String clientid;

	public BindCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, String clientid){
		this.token = token;
		this.clientid = clientid;
	}
	
}

