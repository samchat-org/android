package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class DownloadCoreObj extends SamCoreObj {
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

