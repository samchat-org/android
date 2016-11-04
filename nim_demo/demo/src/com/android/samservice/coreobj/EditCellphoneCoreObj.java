package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;

public class EditCellphoneCoreObj extends SamCoreObj{
	public String token;
	public ContactUser user;
	public String verifycode;
	
	public EditCellphoneCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, ContactUser user, String verifycode){
		this.token = token;
		this.user = user;
		this.verifycode = verifycode;
	}
}
