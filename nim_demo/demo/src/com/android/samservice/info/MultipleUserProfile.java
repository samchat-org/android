package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleUserProfile{
	private int count;
	private List<ContactUser> users;

	public MultipleUserProfile(){
		count = 0;
		users = new ArrayList<ContactUser>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<ContactUser> getusers(){
		return users;
	}
	public void setusers(List<ContactUser> users){
		this.users = users;
	}

	public void adduser(ContactUser user){
		this.users.add(user);
	}
	
}

