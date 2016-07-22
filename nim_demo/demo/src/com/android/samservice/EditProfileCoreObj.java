package com.android.samservice;
import com.android.samservice.info.ContactUser;
public class EditProfileCoreObj extends SamCoreObj{
	public String token;
	public ContactUser user;

	public EditProfileCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, ContactUser user){
		this.token = token;
		this.user = user;
	}
}
