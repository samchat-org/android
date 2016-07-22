package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleBasicUserInfo{
	private int count;
	private List<BasicUserInfo> userinfos;

	public MultipleBasicUserInfo(){
		count = 0;
		userinfos = new ArrayList<BasicUserInfo>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<BasicUserInfo> getuserinfos(){
		return userinfos;
	}
	public void setuserinfos(List<BasicUserInfo> userinfos){
		this.userinfos = userinfos;
	}

	public void addBasicUserInfo(BasicUserInfo userinfo){
		this.userinfos.add(userinfo);
	}
	
}