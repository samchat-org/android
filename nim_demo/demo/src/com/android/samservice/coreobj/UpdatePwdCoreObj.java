package com.android.samservice.coreobj;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.netease.nim.uikit.common.util.string.MD5;

public class UpdatePwdCoreObj extends SamCoreObj{
	public String token;
	public String old_pwd;
	public String new_pwd;
	
	public UpdatePwdCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,String old_pwd,String new_pwd){
		this.token = token;
		this.old_pwd = MD5.getStringMD5(old_pwd+Constants.PWD_SUFFIX);
		this.new_pwd = MD5.getStringMD5(new_pwd+ Constants.PWD_SUFFIX);
	}
	
}
