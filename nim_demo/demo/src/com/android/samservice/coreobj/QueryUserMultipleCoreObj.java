package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

import java.util.List;

public class QueryUserMultipleCoreObj extends SamCoreObj{
	public String token;
	public List<Long> unique_id_list;
	public boolean persist;

	public QueryUserMultipleCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, List<Long> unique_id_list, boolean persist){
		this.token = token;
		this.unique_id_list = unique_id_list;
		this.persist = persist;
	}
	
}

