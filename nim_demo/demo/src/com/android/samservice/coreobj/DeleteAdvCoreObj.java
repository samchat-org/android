package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

import java.util.List;

public class DeleteAdvCoreObj extends SamCoreObj{
	public String token;
	public List<Long> advs;
	
	public DeleteAdvCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, List<Long> advs){
		this.token = token;
		this.advs = advs;
	}
	
}
