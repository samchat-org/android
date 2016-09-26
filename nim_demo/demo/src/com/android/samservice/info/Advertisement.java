package com.android.samservice.info;

import com.android.samservice.Constants;

public class Advertisement{
	public long id;
	public long adv_id;
	public int type;
	public String content;
	public String content_thumb;
	public long publish_timestamp;
	public int response;
	public long sender_unique_id;
	

	public Advertisement(){
		id = 0;
		adv_id = 0;
		type = Constants.ADV_TYPE_TEXT;
		content = null;
		content_thumb = null;
		publish_timestamp = 0;
		response = Constants.ADV_NOT_RESPONSED;
		sender_unique_id = 0;
	}

	public Advertisement(int type, String content, String content_thumb, long sender_unique_id){
		this.id = 0;
		this.adv_id = 0;
		this.type = type;
		this.content = content;
		this.content_thumb = content_thumb;
		this.publish_timestamp = 0;
		this.response = Constants.ADV_NOT_RESPONSED;
		this.sender_unique_id = sender_unique_id;
	}

	public long getid(){
		return id;
	}
	public void setid(long id){
		this.id = id;
	}

	public long getadv_id(){
		return adv_id;
	}
	public void setadv_id(long adv_id){
		this.adv_id = adv_id;
	}

	public int gettype(){
		return type;
	}
	public void settype(int type){
		this.type = type;
	}

	public String getcontent(){
		return content;
	}
	public void setcontent(String content){
		this.content = content;
	}

	public String getcontent_thumb(){
		return content_thumb;
	}
	public void setcontent_thumb(String content_thumb){
		this.content_thumb = content_thumb;
	}

	public long getpublish_timestamp(){
		return publish_timestamp;
	}
	public void setpublish_timestamp(long publish_timestamp){
		this.publish_timestamp = publish_timestamp;
	}

	public int getresponse(){
		return response;
	}
	public void setresponse(int response){
		this.response = response;
	}

	public long getsender_unique_id(){
		return sender_unique_id;
	}
	public void setsender_unique_id(long sender_unique_id){
		this.sender_unique_id = sender_unique_id;
	}

}

