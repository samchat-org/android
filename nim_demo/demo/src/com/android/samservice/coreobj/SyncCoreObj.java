package com.android.samservice.coreobj;

import com.android.samservice.info.StateDateInfo;

public class SyncCoreObj{
	public boolean succeed;
	public StateDateInfo sdinfo;
	
	public SyncCoreObj(boolean succeed){
		this.succeed = succeed;
		this.sdinfo = null;
	}

	public SyncCoreObj(boolean succeed, StateDateInfo sdinfo){
		this.succeed = succeed;
		this.sdinfo = sdinfo;
	}

	
}







