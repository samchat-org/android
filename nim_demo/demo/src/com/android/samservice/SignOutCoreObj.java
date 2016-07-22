package com.android.samservice;

public class SignOutCoreObj extends SamCoreObj{
	public String token;
	
	public SignOutCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}
}
