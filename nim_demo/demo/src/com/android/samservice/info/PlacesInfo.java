package com.android.samservice.info;

public class PlacesInfo {
	public String description;
	public String place_id;

	public PlacesInfo(){
		this.description = null;
		this.place_id = null;
	}
	
	public PlacesInfo(String description, String place_id){
		this.description = description;
		this.place_id = place_id;
	}
}

