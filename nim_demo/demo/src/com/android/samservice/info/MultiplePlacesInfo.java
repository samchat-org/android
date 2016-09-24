package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;
import com.android.samservice.info.PlacesInfo;

public class MultiplePlacesInfo{
	private String key;
	private int count;
	private List<PlacesInfo> info;

	public MultiplePlacesInfo(String key){
		this.key = key;
		this.count = 0;
		this.info = new ArrayList<PlacesInfo>();
	}

	public String getkey(){
		return key;
	}

	public void setkey(String key){
		this.key = key;
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<PlacesInfo> getinfo(){
		return info;
	}
	public void setinfo(List<PlacesInfo> info){
		this.info = info;
	}

	public void addinfo(PlacesInfo pinfo){
		this.info.add(pinfo);
	}
	
}
