package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SyncContactListCoreObj extends SamCoreObj{
	public String token;
	public boolean isCustomer;

	public SyncContactListCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isCustomer,String token){
		this.token = token;
		this.isCustomer = isCustomer;
	}

}

