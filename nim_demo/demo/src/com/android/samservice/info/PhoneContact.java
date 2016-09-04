package com.android.samservice.info;

import android.graphics.Bitmap;

import com.android.samservice.utils.PinyinUtils;

public class PhoneContact
{
	private String name;
	private String phone;
	private Bitmap avatar;

	private String PinYin;
	private String FPinYin;

	public void setPinYin(String PinYin){
		this.PinYin = PinYin;
	}
	public String getPinYin(){
		return PinYin;
	}

	public void setFPinYin(String FPinYin){
		this.FPinYin = FPinYin;
	}
	public String getFPinYin(){
		return FPinYin;
	}

	private void translateToPinYin(){
		String py = PinyinUtils.getPingYin(name);
		String fpy = py.substring(0, 1).toUpperCase();

		setPinYin(py);
		if(fpy.matches("[A-Z]")) {
			setFPinYin(fpy);
		}else {
			setFPinYin("#");
		}
	}

	public PhoneContact(String name, String phone, Bitmap avatar){
		this.name = name;
		this.phone = phone;
		this.avatar = avatar;

		translateToPinYin();
	}

	public String getname(){
		return name;
	}
	public void setname(String name){
		this.name = name;
	}

	public String getphone(){
		return phone;
	}
	public void setphone(String phone){
		this.phone = phone;
	}

	public Bitmap getavatar(){
		return avatar;
	}
	public void setavatar(Bitmap avatar){
		this.avatar = avatar;
	}

	
}