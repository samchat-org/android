package com.android.samservice.coreobj;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.netease.nim.uikit.common.util.string.MD5;

public class SignInCoreObj extends SamCoreObj{
	public String countrycode;
	public String account;
	public String password;
	public String deviceid;
	public String app_version;
	public String device_type;
	
	
	public SignInCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String countrycode,String account, String password,String deviceid){
		this.countrycode = countrycode;
		this.account = account;
		this.password = MD5.getStringMD5(password+Constants.PWD_SUFFIX);
		this.deviceid = deviceid;
		this.app_version = ""+ Constants.SVN;
		this.device_type = "android-"+android.os.Build.VERSION.RELEASE+"-"+android.os.Build.MODEL;
	}
}