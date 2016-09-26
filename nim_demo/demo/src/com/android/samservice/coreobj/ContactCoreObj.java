package com.android.samservice.coreobj;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;

public class ContactCoreObj extends SamCoreObj{
	public String token;
	public int opt;
	public int type;
	public ContactUser user;

	public ContactCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int opt, int type, ContactUser user){
		this.token = token;
		this.opt = opt;
		this.type = type;
		this.user = user;
	}

}


