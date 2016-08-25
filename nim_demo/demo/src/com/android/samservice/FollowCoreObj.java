package com.android.samservice;
import com.android.samservice.info.ContactUser;

public class FollowCoreObj extends SamCoreObj{
	public String token;
	public boolean isFollow;
	public ContactUser sam_pros;

	public FollowCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isFollow, String token, ContactUser sam_pros){
		this.isFollow = isFollow;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}

