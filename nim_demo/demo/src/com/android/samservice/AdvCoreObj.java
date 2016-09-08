package com.android.samservice;

import com.android.samservice.info.Advertisement;

public class AdvCoreObj extends SamCoreObj{
	public String token;
	public int type;
	public String content;
	public String content_thumb;
	public long sender_unique_id;
	
	public AdvCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int type, String content, String content_thumb ,long sender_unique_id){
		this.token = token;
		this.type = type;
		this.content = content;
		this.content_thumb = content_thumb;
		this.sender_unique_id = sender_unique_id;
	}
	
}