package com.android.samservice.coreobj;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PlacesInfo;

public class CreateSamchatIDCoreObj extends SamCoreObj{
	public String token;
	public String samchat_id;

	public CreateSamchatIDCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, String samchat_id){
		this.token = token;
		this.samchat_id = samchat_id;
	}
}

