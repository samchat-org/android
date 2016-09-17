package com.android.samservice;

import java.util.List;

public class DownloadCoreObj extends SamCoreObj{
	public String url;
	public String path;
	
	public DownloadCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String url,String path){
		this.url = url;
		this.path = path;
	}
	
}

