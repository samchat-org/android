package com.android.samservice.info;

import java.io.Serializable;

import com.android.samservice.Constants;

	/*
	id(primary) | question_id | question | status | datetime | latest_answer_time
	*/
public class SendQuestion implements Serializable
{
	private long id;
	private long question_id;
	private String question;
	private int status;
	private long datetime;
	private long latest_answer_time;
	private String address;

	public SendQuestion(){
		this.id = 0;
		this.question_id = 0;
		this.question = null;
		this.status = Constants.QUESTION_STATUS_INACTIVE;
		this.datetime = 0;
		this.latest_answer_time = 0;
		this.address = null;
	}

	public SendQuestion(long question_id, String question, long datetime,String address){
		this.id = 0;
		this.question_id = question_id;
		this.question = question;
		this.status = Constants.QUESTION_STATUS_ACTIVE;
		this.datetime = datetime;
		this.latest_answer_time=0;
		this.address = address;
	}

	public String getaddress(){
		return address;
	}
	public void setaddress(String address){
		this.address = address;
	}

	public long getlatest_answer_time(){
		return latest_answer_time;
	}
	public void setlatest_answer_time(long latest_answer_time){
		this.latest_answer_time = latest_answer_time;
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

	
}


