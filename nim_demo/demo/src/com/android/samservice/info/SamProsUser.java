package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | unique_id | company_name | service_category | service_description 
	               | countrycode | phone | email | address 
	*/
public class SamProsUser extends ContactUser implements Serializable
{
	private long id_sampros;
	private String company_name;
	private String service_category;
	private String service_description;
	private String countrycode_sampros;
	private String phone_sampros;
	private String email_sampros;
	private String address_sampros;
	//private String avatar_sampros; 
	//private String avatar_original_sampros;

	public SamProsUser(){
		super(Constants.SAM_PROS);
		id_sampros = 0;
		company_name = null;
		service_category = null;
		service_description = null;
		countrycode_sampros = null;
		phone_sampros = null;
		email_sampros = null;
		address_sampros = null;
		//avatar_sampros = null;
		//avatar_original_sampros = null;
	}

	public SamProsUser(ContactUser user){
		super(user);
		id_sampros = 0;
		company_name = null;
		service_category = null;
		service_description = null;
		countrycode_sampros = null;
		phone_sampros = null;
		email_sampros = null;
		address_sampros = null;
		//avatar_sampros = null;
		//avatar_original_sampros = null;
	}

	public long getid_sampros(){
		return this.id_sampros;
	}
	public void setid_sampros(long id_sampros){
		this.id_sampros = id_sampros;
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

	public String getcountrycode_sampros(){
		return this.countrycode_sampros;
	}
	public void setcountrycode_sampros(String countrycode_sampros){
		this.countrycode_sampros = countrycode_sampros;
	}

	public String getphone_sampros(){
		return this.phone_sampros;
	}
	public void setphone_sampros(String phone_sampros){
		this.phone_sampros = phone_sampros;
	}

	public String getemail_sampros(){
		return this.email_sampros;
	}
	public void setemail_sampros(String email_sampros){
		this.email_sampros = email_sampros;
	}

	public String getaddress_sampros(){
		return this.address_sampros;
	}
	public void setaddress_sampros(String address_sampros){
		this.address_sampros = address_sampros;
	}

	/*
	public String getavatar_sampros(){
		return this.avatar_sampros;
	}
	public void setavatar_sampros(String avatar_sampros){
		this.avatar_sampros = avatar_sampros;
	}

	
	public String getavatar_original_sampros(){
		return this.avatar_original_sampros;
	}
	public void setavatar_original_sampros(String avatar_original_sampros){
		this.avatar_original_sampros = avatar_original_sampros;
	}
	*/
}

