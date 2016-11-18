package com.android.samservice.coreobj;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.netease.nim.uikit.common.util.string.MD5;

public class SignUpCoreObj extends SamCoreObj{
	public String countrycode;
	public String cellphone;
	public String verifycode;
	public String username;
	public String password;
	public String deviceid;
	public String app_version;
	public String device_type;
	
	public SignUpCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String countrycode,String cellphone, String verifycode, String username,String password,String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.verifycode = verifycode;
		this.username = username;
		this.password = MD5.getStringMD5(password+Constants.PWD_SUFFIX);;
		this.deviceid = deviceid;
		this.app_version = ""+ Constants.SVN;
		this.device_type = "android-"+android.os.Build.VERSION.RELEASE+"-"+android.os.Build.MODEL;
	}	
}