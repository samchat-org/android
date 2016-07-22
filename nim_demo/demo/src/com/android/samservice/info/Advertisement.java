package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.samservice.AdvContent;
import com.android.samservice.AdvContentTypeEnum;
import com.android.samservice.Constants;

public class Advertisement{
	public long id;
	public long adv_id;
	public int type;
	public String content;
	public long publish_timestamp;
	public long sender_unique_id;

	public Advertisement(){
		id = 0;
		adv_id = 0;
		type = Constants.ADV_TYPE_TEXT;
		content = null;
		publish_timestamp = 0;
		sender_unique_id = 0;
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

	public long getpublish_timestamp(){
		return publish_timestamp;
	}
	public void setpublish_timestamp(long publish_timestamp){
		this.publish_timestamp = publish_timestamp;
	}

	public long getsender_unique_id(){
		return sender_unique_id;
	}
	public void setsender_unique_id(long sender_unique_id){
		this.sender_unique_id = sender_unique_id;
	}

}

