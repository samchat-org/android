package com.android.samservice.info;

import com.android.samservice.SMCallBack;
import com.android.samservice.SamCoreObj;

import java.util.List;

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

