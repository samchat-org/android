package com.android.samservice;
import com.android.samservice.info.ContactUser;

public class UpdateAvatarCoreObj extends SamCoreObj{
	public String token;
	public ContactUser user;

	public UpdateAvatarCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, ContactUser user){
		this.token = token;
		this.user = user;
	}
}