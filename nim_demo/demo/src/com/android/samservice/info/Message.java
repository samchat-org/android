package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;
/*
	id(primary) | type | uuid | time
*/
public class Message implements Serializable
{
	private long id;
	private int type;
	private String uuid;

	public Message(){
		this.id = 0;
		this.type = Constants.MSG_TYPE_IM;
		this.uuid = null;
	}

	public Message(int type, String uuid){
		this.id = 0;
		this.type = type;
		this.uuid = uuid;
	}

	public long getid(){
		return this.id;
	}
	public void setid(long id){
		this.id = id;
	}

	public int gettype(){
		return this.type;
	}
	public void settype(int type){
		this.type = type;
	}

	public String getuuid(){
		return this.uuid;
	}
	public void setuuid(String uuid){
		this.uuid = uuid;
	}
	
}

