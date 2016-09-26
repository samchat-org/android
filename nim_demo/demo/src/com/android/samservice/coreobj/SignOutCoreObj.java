package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SignOutCoreObj extends SamCoreObj{
	public String token;
	
	public SignOutCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}
}
