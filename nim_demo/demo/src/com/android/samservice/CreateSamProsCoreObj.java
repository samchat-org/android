package com.android.samservice;
import com.android.samservice.info.SamProsUser;
public class CreateSamProsCoreObj extends SamCoreObj{
	public String token;
	public SamProsUser sam_pros;
	
	public CreateSamProsCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token,SamProsUser sam_pros){
		this.token = token;
		this.sam_pros = sam_pros;
	}
}

