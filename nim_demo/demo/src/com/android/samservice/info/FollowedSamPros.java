package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | unique_id | username | favourite_tag | block_tag | avatar | service_category
	*/
public class FollowedSamPros implements Serializable
{
	private long id;
	private long unique_id;
	private String username;
	private int favourite_tag;
	private int block_tag;
	private String avatar;
	private String service_category;
	private long lastupdate; //not stored in db, just used when sync user

	public FollowedSamPros(){
		this.id = 0;
		this.unique_id = 0;
		this.username = null;
		this.favourite_tag = Constants.NO_TAG;
		this.block_tag = Constants.NO_TAG;
		this.avatar = null;
		this.service_category = null;
		this.lastupdate = 0;
	}

	public FollowedSamPros(long unqiue_id, String username){
		this.id = 0;
		this.unique_id = unqiue_id;
		this.username = username;
		this.favourite_tag = Constants.NO_TAG;
		this.block_tag = Constants.NO_TAG;
		this.avatar = null;
		this.service_category = null;
		this.lastupdate = 0;
	}

	public FollowedSamPros(long unqiue_id, String username, int favourite_tag, int block_tag){
		this.id = 0;
		this.unique_id = unqiue_id;
		this.username = username;
		this.favourite_tag = favourite_tag;
		this.block_tag = block_tag;
		this.avatar = null;
		this.service_category = null;
		this.lastupdate = 0;
	}

	public FollowedSamPros(long unqiue_id, String username, int favourite_tag, int block_tag, String avatar, String service_category){
		this.id = 0;
		this.unique_id = unqiue_id;
		this.username = username;
		this.favourite_tag = favourite_tag;
		this.block_tag = block_tag;
		this.avatar = avatar;
		this.service_category = service_category;
		this.lastupdate = 0;
	}

	public long getid(){
		return id;
	}
	public void setid(long id){
		this.id = id;
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

	public int getfavourite_tag(){
		return favourite_tag;
	}
	public void setfavourite_tag(int favourite_tag){
		this.favourite_tag = favourite_tag;
	}

	public int getblock_tag(){
		return block_tag;
	}
	public void setblock_tag(int block_tag){
		this.block_tag = block_tag;
	}

	public String getavatar(){
		return avatar;
	}
	public void setavatar(String avatar){
		this.avatar = avatar;
	}

	public String getservice_category(){
		return service_category;
	}
	public void setservice_category(String service_category){
		this.service_category = service_category;
	}

	public long getlastupdate(){
		return lastupdate;
	}
	public void setlastupdate(long lastupdate){
		this.lastupdate = lastupdate;
	}
	
}
