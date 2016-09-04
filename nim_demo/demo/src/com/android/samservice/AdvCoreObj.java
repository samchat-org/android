package com.android.samservice;

import com.android.samservice.info.Advertisement;

public class AdvCoreObj extends SamCoreObj{
	public String token;
	public int type;
	public String content;
	public long sender_unique_id;
	
	public AdvCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int type, String content, long sender_unique_id){
		this.token = token;
		this.type = type;
		this.content = content;
		this.sender_unique_id = sender_unique_id;
	}
	
}