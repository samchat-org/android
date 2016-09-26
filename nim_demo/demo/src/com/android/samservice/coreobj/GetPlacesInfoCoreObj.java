package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class GetPlacesInfoCoreObj extends SamCoreObj{
	public String token;
	public String key;

	public GetPlacesInfoCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, String key){
		this.token = token;
		this.key = key;
	}
	
}