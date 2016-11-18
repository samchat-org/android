package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.Advertisement;

public class RecallRequestCoreObj extends SamCoreObj{
	public String token;
	public int type;
	public long request_id;
	
	public RecallRequestCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int type, long request_id){
		this.token = token;
		this.type = type;
		this.request_id = request_id;
	}
	
}