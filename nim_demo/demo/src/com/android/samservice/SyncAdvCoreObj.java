package com.android.samservice;

public class SyncAdvCoreObj extends SamCoreObj{
	public String token;
	public long timestamp;
	public int fetch_count;
	
	public SyncAdvCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, long timestamp, int fetch_count){
		this.token = token;
		this.timestamp = timestamp;
		this.fetch_count = fetch_count;
	}
	
}