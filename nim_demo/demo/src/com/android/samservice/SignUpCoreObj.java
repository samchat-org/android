package com.android.samservice;

public class SignUpCoreObj extends SamCoreObj{
	public String countrycode;
	public String cellphone;
	public String verifycode;
	public String username;
	public String password;
	public String deviceid;
	
	public SignUpCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String countrycode,String cellphone, String verifycode, String username,String password,String deviceid){
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.verifycode = verifycode;
		this.username = username;
		this.password = password;
		this.deviceid = deviceid;
	}	
}