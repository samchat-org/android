package com.android.samservice;

public class SamCoreObj{
	static final int  STATUS_INIT=0;
	static final int  STATUS_DONE=1;
	static final int  STATUS_TIMEOUT=2;

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






