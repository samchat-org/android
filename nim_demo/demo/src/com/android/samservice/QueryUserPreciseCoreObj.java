package com.android.samservice;

public class QueryUserPreciseCoreObj extends SamCoreObj{
	public String token;
	public TypeEnum type;
	public String cellphone;
	public long unique_id;
	public String username;
	public boolean persist;

	public QueryUserPreciseCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, TypeEnum type, String cellphone, long unique_id, String username, boolean persist){
		this.token = token;
		this.type = type;
		this.cellphone = cellphone;
		this.unique_id = unique_id;
		this.username = username;
		this.persist = persist;
	}
	
}

