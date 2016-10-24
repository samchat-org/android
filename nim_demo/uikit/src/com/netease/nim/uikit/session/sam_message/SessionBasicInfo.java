package com.netease.nim.uikit.session.sam_message;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

public class SessionBasicInfo{
	private SessionTypeEnum type;
	private String session_id;
	private int mode;

	public SessionBasicInfo(SessionTypeEnum type, String session_id, int mode){
		this.type = type;
		this.session_id = session_id;
		this.mode = mode;
	}

	public String getsession_id(){
		return session_id;
	}
	public void setsession_id(String session_id){
		this.session_id = session_id;
	}

	public int getmode(){
		return mode;
	}
	public void setmode(int mode){
		this.mode = mode;
	}

	public SessionTypeEnum gettype(){
		return type;
	}
	public void settype(SessionTypeEnum type){
		this.type = type;
	}
}