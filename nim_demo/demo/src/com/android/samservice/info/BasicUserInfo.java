package com.android.samservice.info;
import com.android.samservice.Constants;

public class BasicUserInfo{
	public long unique_id;
	public String username;
	public int type;
	public String avatar_thumb;
	public String avatar_original;
	public String company_name;
	public String service_category;
	public String service_description;

	public BasicUserInfo(){
		unique_id = 0;
		username = null;
		type = Constants.USER;
		avatar_thumb = null;
		avatar_original = null;
		company_name = null;
		service_category = null;
		service_description = null;
	}

	public BasicUserInfo(int usertype){
		unique_id = 0;
		username = null;
		type = usertype;
		avatar_thumb = null;
		avatar_original = null;
		company_name = null;
		service_category = null;
		service_description = null;
	}

	public long getunique_id(){
		return unique_id;
	}
	public void setunique_id(long unique_id){
		this.unique_id = unique_id;
	}

	public String getusername(){
		return username;
	}
	public void setusername(String username){
		this.username = username;
	}

	public int gettype(){
		return type;
	}
	public void settype(int type){
		this.type = type;
	}

	public String getavatar_thumb(){
		return avatar_thumb;
	}
	public void setavatar_thumb(String avatar_thumb){
		this.avatar_thumb = avatar_thumb;
	}

	public String getavatar_original(){
		return avatar_original;
	}
	public void setavatar_original(String avatar_original){
		this.avatar_original = avatar_original;
	}

	public String getcompany_name(){
		return company_name;
	}
	public void setcompany_name(String company_name){
		this.company_name = company_name;
	}

	public String getservice_category(){
		return service_category;
	}
	public void setservice_category(String service_category){
		this.service_category = service_category;
	}

	public String getservice_description(){
		return service_description;
	}
	public void setservice_description(String service_description){
		this.service_description = service_description;
	}

}

