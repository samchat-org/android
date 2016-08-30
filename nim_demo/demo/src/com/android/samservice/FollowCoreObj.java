package com.android.samservice;
import com.android.samservice.info.ContactUser;

public class FollowCoreObj extends SamCoreObj{
	public String token;
	public boolean isFollow;
	public ContactUser sp;

	public FollowCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isFollow, String token, ContactUser sp){
		this.isFollow = isFollow;
		this.token = token;
		this.sp = sp;
	}
	
}

