package com.android.samservice;

public class SyncContactListCoreObj extends SamCoreObj{
	public String token;

	public SyncContactListCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token){
		this.token = token;
	}

}

