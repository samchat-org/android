package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | unique_id | username
	*/
public class Contact implements Serializable
{
	private long id;
	private long unique_id;
	private String username;

	public Contact(){
		this.id = 0;
		this.unique_id = 0;
		this.username = null;
	}

	public Contact(long unique_id, String username){
		this.id = 0;
		this.unique_id = unique_id;
		this.username = username;
	}

	public long getid(){
		return id;
	}
	public void setid(long id){
		this.id = id;
	}

	public long getunique_id(){
		return unique_id;
	}
	public void setunique_id(long unique_id){
		this.unique_id = unique_id;
	}

	public String getusername(){
		return username;
	}
	public void setusername(String username){
		this.username = username;
	}
	
}