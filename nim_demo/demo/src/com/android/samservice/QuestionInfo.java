package com.android.samservice;
import java.io.Serializable;
public class QuestionInfo implements Serializable{
	//send question info
	public String question;
	public long question_id;
	public long datetime;
	public double latitude;
	public double longitude;
	public String place_id;
	public String address;


	public QuestionInfo(){
		this.question = null;
		this.question_id = 0;
		this.datetime = 0;
		this.latitude = Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		this.longitude = Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		this.place_id = null;
		this.address = null;
	}
	
	public QuestionInfo(String question){
		this.question = question;
		this.question_id = 0;
		this.datetime = 0;
		this.latitude = Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		this.longitude = Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		this.place_id = null;
		this.address = null;
	}


	public void setquestion(String question){
		this.question = question;
	}
	public String getquestion(){
		return question;
	}

	public void setquestion_id(long question_id){
		this.question_id = question_id;
	}
	public long getquestion_id(){
		return question_id;
	}
	
	public void setdatetime(long datetime){
		this.datetime = datetime;
	}
	public long getdatetime(){
		return datetime;
	}
	
	public void setlatitude(double latitude){
		this.latitude = latitude;
	}
	public double getlatitude(){
		return latitude;
	}

	public void setlongitude(double longitude){
		this.longitude = longitude;
	}
	public double getlongitude(){
		return longitude;
	}

	public void setplace_id(String place_id){
		this.place_id = place_id;
	}
	public String getplace_id(){
		return place_id;
	}

	public void setaddress(String address){
		this.address = address;
	}
	public String getaddress(){
		return address;
	}
}
