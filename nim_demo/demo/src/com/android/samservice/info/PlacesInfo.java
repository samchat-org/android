package com.android.samservice.info;

import java.io.Serializable;

public class PlacesInfo implements Serializable {
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

	public String getdescription(){
		return description;
	}
	public void setdescription(String description){
		this.description = description;
	}

	public String getplace_id(){
		return place_id;
	}
	public void setplace_id(String place_id){
		this.place_id = place_id;
	}
}

