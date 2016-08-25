package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;
import com.netease.nim.uikit.NimConstants;

/*
	id(primary) | type | uuid | data_id
*/
public class Message implements Serializable
{
	private long id;
	private int type;
	private String uuid;
	private long data_id;

	public Message(){
		this.id = 0;
		this.type = NimConstants.MSG_TYPE_IM;
		this.uuid = null;
		this.data_id = 0;
	}

	public Message(int type, String uuid){
		this.id = 0;
		this.type = type;
		this.uuid = uuid;
		this.data_id = 0;
	}

	public Message(int type, String uuid,long data_id){
		this.id = 0;
		this.type = type;
		this.uuid = uuid;
		this.data_id = data_id;
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

	public long getdata_id(){
		return this.data_id;
	}
	public void setdata_id(long data_id){
		this.data_id = data_id;
	}
	
}

