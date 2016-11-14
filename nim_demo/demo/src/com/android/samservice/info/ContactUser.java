package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
	/*
	id(primary) | unique_id | username | usertype |question_notify | lastupdate | avatar | avatar_original |countrycode | cellphone | email | address 
						| company_name | service_category | service_description |countrycode_sp | phone_sp | email_sp | address_sp 
	*/
public class ContactUser implements UserInfoProvider.UserInfo
{
	private long id;
	private long unique_id;
	private String username;
	private int usertype;
	private int question_notify;
	private long lastupdate;
	private String avatar; 
	private String avatar_original;
	private String countrycode;
	private String cellphone;
	private String email;
	private String address;
	private String company_name;
	private String service_category;
	private String service_description;
	private String countrycode_sp;
	private String phone_sp;
	private String email_sp;
	private String address_sp;
	

	public ContactUser(){
		this.id = 0;
		this.unique_id = 0;
		this.username = "";
		this.usertype = Constants.USER;
		this.question_notify = 1;
		this.lastupdate = 0;
		this.avatar = "";
		this.avatar_original = "";
		this.countrycode = "";
		this.cellphone = "";
		this.email = "";
		this.address = "";
		this.company_name = "";
		this.service_category = "";
		this.service_description = "";
		this.countrycode_sp = "";
		this.phone_sp = "";
		this.email_sp = "";
		this.address_sp = "";
	}

	public ContactUser(int type){
		this.id = 0;
		this.unique_id = 0;
		this.username = "";
		this.usertype = type;
		this.question_notify = 1;
		this.lastupdate = 0;
		this.avatar = "";
		this.avatar_original = "";
		this.countrycode = "";
		this.cellphone = "";
		this.email = "";
		this.address = "";
		this.company_name = "";
		this.service_category = "";
		this.service_description = "";
		this.countrycode_sp = "";
		this.phone_sp = "";
		this.email_sp = "";
		this.address_sp = "";
	}

	public ContactUser(ContactUser user){
		this.id = user.getid();
		this.unique_id = user.getunique_id();
		this.username = user.getusername();
		this.usertype = user.getusertype();
		this.question_notify = user.getquestion_notify();
		this.lastupdate = user.getlastupdate();
		this.avatar = user.getavatar();
		this.avatar_original = user.getavatar_original();
		this.countrycode = user.getcountrycode();
		this.cellphone = user.getcellphone();
		this.email = user.getemail();
		this.address = user.getaddress();
		this.company_name = user.getcompany_name();
		this.service_category = user.getservice_category();
		this.service_description = user.getservice_description();
		this.countrycode_sp = user.getcountrycode_sp();
		this.phone_sp = user.getphone_sp();
		this.email_sp = user.getemail_sp();
		this.address_sp = user.getaddress_sp();
	}

	public long getid(){
		return this.id;
	}
	public void setid(long id){
		this.id = id;
	}

	public long getunique_id(){
		return this.unique_id;
	}
	public void setunique_id(long unique_id){
		this.unique_id = unique_id;
	}

	public String getusername(){
		return this.username;
	}
	public void setusername(String username){
		this.username = username;
	}

	public int getusertype(){
		return this.usertype;
	}
	public void setusertype(int usertype){
		this.usertype = usertype;
	}

	public int getquestion_notify(){
		return this.question_notify;
	}
	public void setquestion_notify(int question_notify){
		this.question_notify = question_notify;
	}

	public long getlastupdate(){
		return this.lastupdate;
	}
	public void setlastupdate(long lastupdate){
		this.lastupdate = lastupdate;
	}

	public String getavatar(){
		return this.avatar;
	}
	public void setavatar(String avatar){
		this.avatar = avatar;
	}

	public String getavatar_original(){
		return this.avatar_original;
	}
	public void setavatar_original(String avatar_original){
		this.avatar_original = avatar_original;
	}

	public String getcountrycode(){
		return this.countrycode;
	}
	public void setcountrycode(String countrycode){
		this.countrycode = countrycode;
	}

	public String getcellphone(){
		return this.cellphone;
	}
	public void setcellphone(String cellphone){
		this.cellphone = cellphone;
	}

	public String getemail(){
		return this.email;
	}
	public void setemail(String email){
		this.email = email;
	}

	public String getaddress(){
		return this.address;
	}
	public void setaddress(String address){
		this.address = address;
	}

	public String getcompany_name(){
		return this.company_name;
	}
	public void setcompany_name(String company_name){
		this.company_name = company_name;
	}

	public String getservice_category(){
		return this.service_category;
	}
	public void setservice_category(String service_category){
		this.service_category = service_category;
	}

	public String getservice_description(){
		return this.service_description;
	}
	public void setservice_description(String service_description){
		this.service_description = service_description;
	}

	public String getcountrycode_sp(){
		return this.countrycode_sp;
	}
	public void setcountrycode_sp(String countrycode_sp){
		this.countrycode_sp = countrycode_sp;
	}

	public String getphone_sp(){
		return this.phone_sp;
	}
	public void setphone_sp(String phone_sp){
		this.phone_sp = phone_sp;
	}

	public String getemail_sp(){
		return this.email_sp;
	}
	public void setemail_sp(String email_sp){
		this.email_sp = email_sp;
	}

	public String getaddress_sp(){
		return this.address_sp;
	}
	public void setaddress_sp(String address_sp){
		this.address_sp = address_sp;
	}


	@Override
	public String getAccount(){
		return (""+unique_id);
	}

	@Override
	public String getName(){
		return username;
	}

	@Override
	public String getAvatar(){
		if(avatar != null){
			return avatar;
		}else{
			return avatar_original;
		}
	}


	
}
