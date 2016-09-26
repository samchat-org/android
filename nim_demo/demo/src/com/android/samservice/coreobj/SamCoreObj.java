package com.android.samservice.coreobj;

import com.android.samservice.callback.SMCallBack;

public class SamCoreObj{
	static public final int  STATUS_INIT=0;
	static public final int  STATUS_DONE=1;
	static public final int  STATUS_TIMEOUT=2;

	public SMCallBack callback;
	public int request_status;
	public int retry_count;

	public SamCoreObj(){
		this.callback = null;
		this.request_status = STATUS_INIT;
		this.retry_count = 0;
	}

	public SamCoreObj(SMCallBack callback){
		this.callback = callback;
		this.request_status = STATUS_INIT;
		this.retry_count = 0;
	}

	public void setRetryCount(int retry){
		this.retry_count = retry;
	}
	
}






