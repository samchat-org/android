package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class VerifyCodeCoreObj extends SamCoreObj{
	public String countrycode;
	public String cellphone;
	public String verifycode;
	public String new_password;
	public String deviceid;
	
	public VerifyCodeCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init_register_code_request(String countrycode,String cellphone, String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.deviceid = deviceid;
	}

	public void init_register_code_verify(String countrycode,String cellphone, String verifycode, String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.verifycode = verifycode;
		this.deviceid = deviceid;
	}

	public void init_findpwd_code_request(String countrycode,String cellphone,String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.deviceid = deviceid;
	}
	
	public void init_findpwd_code_verify(String countrycode,String cellphone,String verifycode, String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.verifycode = verifycode;
		this.deviceid = deviceid;
	}

	public void init_findpwd_update(String countrycode,String cellphone,String verifycode, String new_password, String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.verifycode = verifycode;
		this.new_password = new_password;
		this.deviceid = deviceid;
	}

	
	
	
}