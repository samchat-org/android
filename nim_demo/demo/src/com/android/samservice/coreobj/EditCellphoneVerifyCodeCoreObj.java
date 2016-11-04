package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class EditCellphoneVerifyCodeCoreObj extends SamCoreObj{
	public String token;
	public String countrycode;
	public String cellphone;
	
	public EditCellphoneVerifyCodeCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, String countrycode,String cellphone){
		this.token = token;
		this.countrycode = countrycode;
		this.cellphone = cellphone;
	}
}