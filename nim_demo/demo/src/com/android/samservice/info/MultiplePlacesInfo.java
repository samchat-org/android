package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;
import com.android.samservice.info.PlacesInfo;

public class MultiplePlacesInfo{
	private int count;
	private List<PlacesInfo> info;

	public MultiplePlacesInfo(){
		count = 0;
		info = new ArrayList<PlacesInfo>();
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
