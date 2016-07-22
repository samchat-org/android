package com.android.samservice;
import com.android.samservice.info.SamProsUser;

public class FavouriteCoreObj extends SamCoreObj{
	public String token;
	public boolean isFavourite;
	public SamProsUser sam_pros;

	public FavouriteCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isFavourite, String token, SamProsUser sam_pros){
		this.isFavourite = isFavourite;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}

