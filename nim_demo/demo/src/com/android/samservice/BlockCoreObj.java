package com.android.samservice;
import com.android.samservice.info.SamProsUser;
public class BlockCoreObj extends SamCoreObj{
	public String token;
	public boolean isBlock;
	public SamProsUser sam_pros;

	public BlockCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isBlock, String token, SamProsUser sam_pros){
		this.isBlock = isBlock;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}
