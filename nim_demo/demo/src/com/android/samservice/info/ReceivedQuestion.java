package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | question_id | question | sender_unique_id | sender_username | status | datetime | address | unread
	*/
public class ReceivedQuestion implements Serializable
{
	private long id;
	private long question_id;
	private long sender_unique_id;
	private String sender_username;
	private String question;
	private int status;
	private long datetime;
	private String address;
	private int unread;

	public ReceivedQuestion(){
		this.id = 0;
		this.question_id = 0;
		this.sender_unique_id = 0;
		this.sender_username = null;
		this.question = null;
		this.status = Constants.QUESTION_NOT_RESPONSED;
		this.datetime = 0;
		this.address = null;
		this.unread = Constants.QUESTION_UNREAD;
	}

	public ReceivedQuestion(long question_id, long sender_unique_id, String sender_username,String question, long datetime,String address){
		this.id = 0;
		this.question_id = question_id;
		this.sender_unique_id = sender_unique_id;
		this.sender_username = sender_username;
		this.question = question;
		this.status = Constants.QUESTION_NOT_RESPONSED;
		this.datetime = datetime;
		this.address = address;
		this.unread = Constants.QUESTION_UNREAD;
	}

	public String getaddress(){
		return address;
	}
	public void setaddress(String address){
		this.address = address;
	}

	public long getid(){
		return id;
	}
	public void setid(long id){
		this.id = id;
	}

	public long getquestion_id(){
		return question_id;
	}
	public void setquestion_id(long question_id){
		this.question_id = question_id;
	}

	public long getsender_unique_id(){
		return sender_unique_id;
	}
	public void setsender_unique_id(long sender_unique_id){
		this.sender_unique_id = sender_unique_id;
	}

	public String getsender_username(){
		return sender_username;
	}
	public void setsender_username(String sender_username){
		this.sender_username = sender_username;
	}

	public String getquestion(){
		return question;
	}
	public void setquestion(String question){
		this.question = question;
	}

	public int getstatus(){
		return status;
	}
	public void setstatus(int status){
		this.status = status;
	}

	public long getdatetime(){
		return datetime;
	}
	public void setdatetime(long datetime){
		this.datetime = datetime;
	}

	public int getunread(){
		return unread;
	}
	public void setunread(int unread){
		this.unread = unread;
	}

	
}



