package com.android.samservice;

import java.util.List;

public class QueryUserMultipleCoreObj extends SamCoreObj{
	public String token;
	public List<Long> unique_id_list;

	public QueryUserMultipleCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, List<Long> unique_id_list){
		this.token = token;
		this.unique_id_list = unique_id_list;
	}
	
}

