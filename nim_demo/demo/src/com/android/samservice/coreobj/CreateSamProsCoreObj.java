package com.android.samservice.coreobj;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;

public class CreateSamProsCoreObj extends SamCoreObj{
	public String token;
	public ContactUser sam_pros;
	
	public CreateSamProsCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,ContactUser sam_pros){
		this.token = token;
		this.sam_pros = sam_pros;
	}
}

