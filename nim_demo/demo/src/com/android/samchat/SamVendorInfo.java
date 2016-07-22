package com.android.samchat;

public class SamVendorInfo{
	private String bussiness_line;
	private String bussiness_location;
	private String bussiness_introduction;

	public SamVendorInfo( String bussiness_line,  String bussiness_location, String bussiness_introduction){
		this.bussiness_line = bussiness_line;
		this.bussiness_location = bussiness_location;
		this.bussiness_introduction = bussiness_introduction;
	}
	
	public String getArea(){
		return bussiness_line;
	}
	
	public String getLocation(){
		return bussiness_location;
	}
	
	public String getDesc(){
		return bussiness_introduction;
	}
	
}

