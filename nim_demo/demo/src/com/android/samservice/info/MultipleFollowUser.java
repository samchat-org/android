package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleFollowUser{
	private int count;
	private List<FollowUser> users;

	public MultipleFollowUser(){
		count = 0;
		users = new ArrayList<FollowUser>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<FollowUser> getusers(){
		return users;
	}
	public void setusers(List<FollowUser> users){
		this.users = users;
	}

	public void adduser(FollowUser user){
		this.users.add(user);
	}
	
}

