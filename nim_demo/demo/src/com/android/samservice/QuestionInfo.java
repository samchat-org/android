package com.android.samservice;

public class QuestionInfo {
	//send question info
	public String question;
	public long question_id;
	public long datetime;


	public QuestionInfo(){
		this.question = null;
		this.question_id = 0;
		this.datetime = 0;
	}
	
	public QuestionInfo(String question){
		this.question = question;
		this.question_id = 0;
		this.datetime = 0;
	}
}
