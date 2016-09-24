package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;
import com.android.samchat.type.ModeEnum;
import com.netease.nim.uikit.NimConstants;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

/*
	id(primary) | session_id | mode | msg_table_name | total_unread 
	|recent_msg_type |recent_msg_uuid | recent_msg_subtype | recent_msg_content | recent_msg_time | recent_msg_status
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
	private int recent_msg_subtype;
	private String recent_msg_content;
	private long recent_msg_time;
	private int recent_msg_status;

	public static String makeKey(String s_id, int mode){
		return s_id+"_"+mode;
	}

	public MsgSession(){
		this.id = 0;
		this.session_id = null;
		this.mode = ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE);
		this.msg_table_name = null;
		this.total_unread = 0;
		this.recent_msg_type = NimConstants.MSG_TYPE_IM;
		this.recent_msg_uuid = null;
		this.recent_msg_subtype = MsgTypeEnum.undef.getValue();
		this.recent_msg_content = null;
		this.recent_msg_time = 0;
		this.recent_msg_status = MsgStatusEnum.success.getValue();
	}

	public MsgSession(String session_id, int mode, String msg_table_name){
		this.id = 0;
		this.session_id = session_id;
		this.mode = mode;
		this.msg_table_name = msg_table_name;
		this.total_unread = 0;
		this.recent_msg_type = NimConstants.MSG_TYPE_IM;
		this.recent_msg_uuid = null;
		this.recent_msg_subtype = MsgTypeEnum.undef.getValue();
		this.recent_msg_content = null;
		this.recent_msg_time = 0;
		this.recent_msg_status = MsgStatusEnum.success.getValue();
	}

	public MsgSession(MsgSession session){
		this.id = session.getid();
		this.session_id = session.getsession_id();
		this.mode = session.getmode();
		this.msg_table_name = session.getmsg_table_name();
		this.total_unread = session.gettotal_unread();
		this.recent_msg_type = session.getrecent_msg_type();
		this.recent_msg_uuid = session.getrecent_msg_uuid();
		this.recent_msg_subtype = session.getrecent_msg_subtype();
		this.recent_msg_content = session.getrecent_msg_content();
		this.recent_msg_time = session.getrecent_msg_time();
		this.recent_msg_status = session.getrecent_msg_status();
	}

	public int getrecent_msg_subtype(){
		return this.recent_msg_subtype;
	}
	public void setrecent_msg_subtype(int recent_msg_subtype){
		this.recent_msg_subtype = recent_msg_subtype;
	}

	public String getrecent_msg_content(){
		return this.recent_msg_content;
	}
	public void setrecent_msg_content(String recent_msg_content){
		this.recent_msg_content = recent_msg_content;
	}

	public long getrecent_msg_time(){
		return this.recent_msg_time;
	}
	public void setrecent_msg_time(long recent_msg_time){
		this.recent_msg_time = recent_msg_time;
	}

	public int getrecent_msg_status(){
		return this.recent_msg_status;
	}
	public void setrecent_msg_status(int recent_msg_status){
		this.recent_msg_status = recent_msg_status;
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

