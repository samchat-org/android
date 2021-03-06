package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | session | name |recent_adv_id |recent_adv_type |recent_adv_content |rcent_adv_content_thumb| recent_adv_publish_timestamp | unread
	*/
public class RcvdAdvSession implements Serializable
{
	private long id;
	private long session;
	private String name;
	private long recent_adv_id;
	private int recent_adv_type;
	private String recent_adv_content;
	private String recent_adv_content_thumb;
	private long recent_adv_publish_timestamp;
	private int unread;

	public RcvdAdvSession(){
		this.id = 0;
		this.session = 0;
		this.name = null;
		this.recent_adv_id = 0;
		this.recent_adv_type = Constants.ADV_TYPE_TEXT;
		this.recent_adv_content = null;
		this.recent_adv_content_thumb = null;
		this.recent_adv_publish_timestamp = 0;
		this.unread = 0;
	}

	public RcvdAdvSession(long session, String name){
		this.id = 0;
		this.session = session;
		this.name = name;
		this.recent_adv_id = 0;
		this.recent_adv_type = Constants.ADV_TYPE_TEXT;
		this.recent_adv_content = null;
		this.recent_adv_content_thumb = null;
		this.recent_adv_publish_timestamp = 0;
		this.unread = 0;
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

	public long getrecent_adv_id(){
		return recent_adv_id;
	}
	public void setrecent_adv_id(long recent_adv_id){
		this.recent_adv_id = recent_adv_id;
	}

	public int getrecent_adv_type(){
		return recent_adv_type;
	}
	public void setrecent_adv_type(int recent_adv_type){
		this.recent_adv_type = recent_adv_type;
	}

	public String getrecent_adv_content(){
		return recent_adv_content;
	}
	public void setrecent_adv_content(String recent_adv_content){
		this.recent_adv_content = recent_adv_content;
	}

	public String getrecent_adv_content_thumb(){
		return recent_adv_content_thumb;
	}
	public void setrecent_adv_content_thumb(String recent_adv_content_thumb){
		this.recent_adv_content_thumb = recent_adv_content_thumb;
	}

	public long getrecent_adv_publish_timestamp(){
		return recent_adv_publish_timestamp;
	}
	public void setrecent_adv_publish_timestamp(long recent_adv_publish_timestamp){
		this.recent_adv_publish_timestamp = recent_adv_publish_timestamp;
	}

	public int getunread(){
		return unread;
	}
	public void setunread(int unread){
		this.unread = unread;
	}
	
}




