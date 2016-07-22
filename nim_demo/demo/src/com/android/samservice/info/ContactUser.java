package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | unique_id | username | usertype | lastupdate | avatar | avatar_original |countrycode | cellphone | email | address 
	*/
public class ContactUser implements Serializable
{
	private long id;
	private long unique_id;
	private String username;
	private int usertype;
	private long lastupdate;
	private String avatar; 
	private String avatar_original;
	private String countrycode;
	private String cellphone;
	private String email;
	private String address;

	public ContactUser(){
		this.id = 0;
		this.unique_id = 0;
		this.username = null;
		this.usertype = Constants.USER;
		this.lastupdate = 0;
		this.avatar = null;
		this.avatar_original = null;
		this.countrycode = null;
		this.cellphone = null;
		this.email = null;
		this.address = null;
	}

	public ContactUser(int type){
		this.id = 0;
		this.unique_id = 0;
		this.username = null;
		this.usertype = type;
		this.lastupdate = 0;
		this.avatar = null;
		this.avatar_original = null;
		this.countrycode = null;
		this.cellphone = null;
		this.email = null;
		this.address = null;
	}

	public ContactUser(ContactUser user){
		this.id = user.getid();
		this.unique_id = user.getunique_id();
		this.username = user.getusername();
		this.usertype = user.getusertype();
		this.lastupdate = user.getlastupdate();
		this.avatar = user.getavatar();
		this.avatar_original = user.getavatar_original();
		this.countrycode = user.getcountrycode();
		this.cellphone = user.getcellphone();
		this.email = user.getemail();
		this.address = user.getemail();
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
}
