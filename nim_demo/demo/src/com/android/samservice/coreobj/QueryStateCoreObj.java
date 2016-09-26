package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class QueryStateCoreObj extends SamCoreObj {
	public String token;
	
	public QueryStateCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}
	
}