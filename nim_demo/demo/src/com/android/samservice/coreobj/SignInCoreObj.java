package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SignInCoreObj extends SamCoreObj{
	public String countrycode;
	public String account;
	public String password;
	public String deviceid;
	
	
	public SignInCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String countrycode,String account, String password,String deviceid){
		this.countrycode = countrycode;
		this.account = account;
		this.password = password;
		this.deviceid = deviceid;
	}
}