package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleSyncAdv{
	private int count;
	private List<Advertisement> advs;

	public MultipleSyncAdv(){
		count = 0;
		advs = new ArrayList<Advertisement>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<Advertisement> getadvs(){
		return advs;
	}
	public void setadvs(List<Advertisement> advs){
		this.advs = advs;
	}

	public void addadv(Advertisement adv){
		this.advs.add(adv);
	}
	
}

