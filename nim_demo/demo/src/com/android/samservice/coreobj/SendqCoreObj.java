package com.android.samservice.coreobj;

import com.android.samchat.common.SCell;
import com.android.samservice.callback.SMCallBack;

public class SendqCoreObj extends SamCoreObj{
	public String token;
	public String question;
	public double latitude;
	public double longitude;
	public String place_id;
	public String address;
	public SCell cell;

	public SendqCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,String question,double latitude, double longitude, String place_id, String address, SCell cell){
		this.token = token;
		this.question = question;
		this.latitude = latitude;
		this.longitude = longitude;
		this.place_id = place_id;
		this.address = address;
		this.cell = cell;
	}
	
}
