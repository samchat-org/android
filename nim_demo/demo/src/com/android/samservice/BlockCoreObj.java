package com.android.samservice;
import com.android.samservice.info.ContactUser;

public class BlockCoreObj extends SamCoreObj{
	public String token;
	public boolean isBlock;
	public ContactUser sam_pros;

	public BlockCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isBlock, String token, ContactUser sam_pros){
		this.isBlock = isBlock;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}
