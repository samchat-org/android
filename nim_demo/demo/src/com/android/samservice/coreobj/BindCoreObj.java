package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class BindCoreObj extends SamCoreObj{
	public String token;

	public BindCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}
	
}

