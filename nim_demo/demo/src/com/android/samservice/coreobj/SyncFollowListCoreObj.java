package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SyncFollowListCoreObj extends SamCoreObj{
	public String token;

	public SyncFollowListCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}

}
