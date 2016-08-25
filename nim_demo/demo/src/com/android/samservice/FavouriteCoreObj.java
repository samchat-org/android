package com.android.samservice;
import com.android.samservice.info.ContactUser;

public class FavouriteCoreObj extends SamCoreObj{
	public String token;
	public boolean isFavourite;
	public ContactUser sam_pros;

	public FavouriteCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(boolean isFavourite, String token, ContactUser sam_pros){
		this.isFavourite = isFavourite;
		this.token = token;
		this.sam_pros = sam_pros;
	}
	
}

