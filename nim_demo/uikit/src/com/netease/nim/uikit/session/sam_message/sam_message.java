package com.netease.nim.uikit.session.sam_message;

import java.io.Serializable;
import com.netease.nimlib.sdk.msg.model.IMMessage;
/*
	id(primary) | type | uuid
*/
public class sam_message implements Serializable
{
	public static final int MSG_TYPE_IM = 0;
	public static final int MSG_TYPE_SQ = 1; 
	public static final int MSG_TYPE_RQ = 2; 
	public static final int MSG_TYPE_ADV = 3; 
	
	private long id;
	private int type;
	private String uuid;
	private IMMessage im_msg;
	

	public sam_message(){
		this.id = 0;
		this.type = MSG_TYPE_IM;
		this.uuid = null;
		this.im_msg = null;
	}

	public sam_message(long id, int type, String uuid,IMMessage im_msg){
		this.id = id;
		this.type = type;
		this.uuid = uuid;
		this.im_msg = im_msg;
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

	public IMMessage getim_msg(){
		return this.im_msg;
	}
	public void setim_msg(IMMessage im_msg){
		this.im_msg = im_msg;
	}
	
}

