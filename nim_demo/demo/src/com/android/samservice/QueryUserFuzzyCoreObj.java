package com.android.samservice;

public class QueryUserFuzzyCoreObj extends SamCoreObj{
	public String token;
	public String search_key;

	public QueryUserFuzzyCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, String search_key){
		this.token = token;
		this.search_key = search_key;
	}
	
}
