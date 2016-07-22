package com.android.samservice;

public class QueryUserWithoutTokenCoreObj extends SamCoreObj{
	public TypeEnum type;
	public String countrycode;
	public String cellphone;
	public String username;

	public QueryUserWithoutTokenCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(TypeEnum type, String countrycode, String cellphone, String username){
		this.type = type;
		this.countrycode = countrycode;
		this.cellphone = cellphone;
		this.username = username;
	}
	
}