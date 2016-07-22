package com.android.samservice;

import com.android.samservice.info.Advertisement;

public class AdvCoreObj extends SamCoreObj{
	public String token;
	public Advertisement adv;
	
	public AdvCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, Advertisement adv){
		this.token = token;
		this.adv = adv;
	}
	
}