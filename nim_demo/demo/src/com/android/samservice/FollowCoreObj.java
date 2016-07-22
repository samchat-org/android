package com.android.samservice;
import com.android.samservice.info.SamProsUser;

public class FollowCoreObj extends SamCoreObj{
	public String token;
	public boolean isFollow;
	public SamProsUser sam_pros;

	public FollowCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isFollow, String token, SamProsUser sam_pros){
		this.isFollow = isFollow;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}

