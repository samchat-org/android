package com.android.samservice.coreobj;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.PhoneNumber;

import java.util.List;

public class SendInviteMsgCoreObj extends SamCoreObj{
	public String token;
	public List<PhoneNumber> phones;
	public String msg;

	public SendInviteMsgCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, List<PhoneNumber> phones, String msg){
		this.token = token;
		this.phones = phones;
		this.msg = msg;
	}
	
}
