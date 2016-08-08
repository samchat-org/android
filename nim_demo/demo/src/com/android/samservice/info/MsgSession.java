package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;
import com.android.samchat.type.ModeEnum;
/*
	id(primary) | session_id | mode | msg_table_name | total_unread |recent_msg_type |recent_msg_uuid
*/
public class MsgSession implements Serializable
{
	private long id;
	private String session_id;
	private int mode;
	private String msg_table_name;
	private int total_unread;
	private int recent_msg_type;
	private String recent_msg_uuid;

	public MsgSession(){
		this.id = 0;
		this.session_id = null;
		this.mode = ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE);
		this.msg_table_name = null;
		this.total_unread = 0;
		this.recent_msg_type = Constants.MSG_TYPE_IM;
		this.recent_msg_uuid = null;
	}

	public MsgSession(String session_id, int mode, String msg_table_name, int total_unread, int recent_msg_type, String recent_msg_uuid){
		this.id = 0;
		this.session_id = session_id;
		this.mode = mode;
		this.msg_table_name = msg_table_name;
		this.total_unread = total_unread;
		this.recent_msg_type = recent_msg_type;
		this.recent_msg_uuid = recent_msg_uuid;
	}

	public long getid(){
		return this.id;
	}
	public void setid(long id){
		this.id = id;
	}

	public String getsession_id(){
		return this.session_id;
	}
	public void setsession_id(String session_id){
		this.session_id = session_id;
	}

	public int getmode(){
		return this.mode;
	}
	public void setmode(int mode){
		this.mode = mode;
	}

	public String getmsg_table_name(){
		return this.msg_table_name;
	}
	public void setmsg_table_name(String msg_table_name){
		this.msg_table_name = msg_table_name;
	}

	public int gettotal_unread(){
		return this.total_unread;
	}
	public void settotal_unread(int total_unread){
		this.total_unread = total_unread;
	}

	public int getrecent_msg_type(){
		return this.recent_msg_type;
	}
	public void setrecent_msg_type(int recent_msg_type){
		this.recent_msg_type = recent_msg_type;
	}

	public String getrecent_msg_uuid(){
		return this.recent_msg_uuid;
	}
	public void setrecent_msg_uuid(String recent_msg_uuid){
		this.recent_msg_uuid = recent_msg_uuid;
	}
	
}

