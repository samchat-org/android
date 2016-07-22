package com.android.samservice;

public class GetAppKeyCoreObj extends SamCoreObj{
	public String token;
	
	public GetAppKeyCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}
}