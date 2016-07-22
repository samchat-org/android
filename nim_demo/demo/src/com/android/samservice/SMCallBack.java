package com.android.samservice;

public abstract class  SMCallBack{
	abstract public void onSuccess(Object obj, int WarningCode);

	abstract public void onError(int errCode);

	abstract public void onFailed(int code);


}