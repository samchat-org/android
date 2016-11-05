package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class QueryPublicCoreObj extends SamCoreObj{
	public String token;
	public int count;
	public String key;
	public double latitude;
	public double longitude;
	public String place_id;
	public String address;

	public QueryPublicCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int count, String key , double latitude, double longitude, String place_id, String address){
		this.token = token;
		this.count = count;
		this.key = key;
		this.latitude = latitude;
		this.longitude = longitude;
		this.place_id = place_id;
		this.address = address;
	}

}

