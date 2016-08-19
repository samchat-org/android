package com.android.samchat.common;
import com.android.samservice.utils.PinyinUtils;

public class CountryInfo{
	public String name;
	public String code;
	private String PinYin;
	private String FPinYin;

	public CountryInfo(String name, String code){
		this.name = name;
		this.code = code;
		translateToPinYin();
	}

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
}