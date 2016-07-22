package com.android.samservice;

public class UpdatePwdCoreObj extends SamCoreObj{
	public String token;
	public String old_pwd;
	public String new_pwd;
	
	public UpdatePwdCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,String old_pwd,String new_pwd){
		this.token = token;
		this.old_pwd = old_pwd;
		this.new_pwd = new_pwd;
	}
	
}
