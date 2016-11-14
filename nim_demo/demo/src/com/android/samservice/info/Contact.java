package com.android.samservice.info;

import com.android.samchat.cache.SamchatUserInfoCache;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.android.samservice.utils.PinyinUtils;
	/*
	id(primary) | unique_id | username | avatar | service_category
	*/
public class Contact implements UserInfoProvider.UserInfo
{
	private long id;
	private long unique_id;
	private String username;
	private String avatar;
	private String service_category;

	private String PinYin;
	private String FPinYin;
	private long lastupdate;

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

	public void updateFPinYin(){
		translateToPinYin();
	}

	private void translateToPinYin(){
		String py = PinyinUtils.getPingYin(getusername());
		String fpy = py.substring(0, 1).toUpperCase();

		setPinYin(py);
		if(fpy.matches("[A-Z]")) {
			setFPinYin(fpy);
		}else {
			setFPinYin("#");
		}
	}

	public Contact(long unique_id, String username, String avatar){
		this.id = 0;
		this.unique_id = unique_id;
		this.username = username;
		this.avatar = avatar;
		this.service_category = "";

		translateToPinYin();
	}

	public Contact(long unique_id, String username, String avatar,String service_category){
		this.id = 0;
		this.unique_id = unique_id;
		this.username = username;
		this.avatar = avatar;
		this.service_category = service_category;

		translateToPinYin();
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
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(unique_id);
		if(user != null){
			return user.getusername();
		}else{
			return username;
		}
	}
	public void setusername(String username){
		this.username = username;
		translateToPinYin();
	}

	public String getavatar(){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(unique_id);
		if(user != null){
			return user.getavatar();
		}else{
			return avatar;
		}
	}
	public void setavatar(String avatar){
		this.avatar = avatar;
	}

	public String getservice_category(){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(unique_id);
		if(user != null){
			return user.getservice_category();
		}else{
			return service_category;
		}
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

	@Override
	public String getAccount(){
		return (""+unique_id);
	}

	@Override
	public String getName(){
		return getusername();
	}

	@Override
	public String getAvatar(){
		return getavatar();
	}
	
}