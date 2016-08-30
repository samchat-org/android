package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleFollowUser{
	private int count;
	private List<FollowedSamPros> sps;

	public MultipleFollowUser(){
		count = 0;
		sps = new ArrayList<FollowedSamPros>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<FollowedSamPros> getsps(){
		return sps;
	}
	public void setsps(List<FollowedSamPros> sps){
		this.sps = sps;
	}

	public void addsp(FollowedSamPros sp){
		this.sps.add(sp);
	}
	
}

