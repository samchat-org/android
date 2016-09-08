package com.android.samservice;

import java.util.List;

public class DownloadCoreObj extends SamCoreObj{
	public String url;
	
	public DownloadCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String url){
		this.url = url;
	}
	
}

