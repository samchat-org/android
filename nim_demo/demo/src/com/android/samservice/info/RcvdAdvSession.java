package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | session | name |
	*/
public class RcvdAdvSession implements Serializable
{
	private long id;
	private long session;
	private String name;

	public RcvdAdvSession(){
		this.id = 0;
		this.session = 0;
		this.name = null;
	}

	public RcvdAdvSession(long session, String name){
		this.id = 0;
		this.session = session;
		this.name = name;
	}

	public long getid(){
		return id;
	}
	public void setid(long id){
		this.id = id;
	}

	public long getsession(){
		return session;
	}
	public void setsession(long session){
		this.session = session;
	}

	public String getname(){
		return name;
	}
	public void setname(String name){
		this.name = name;
	}
	
}




