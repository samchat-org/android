package com.netease.nim.uikit;

public abstract class  NIMCallback{
	public static int SUCCEED=0;
	public static int FAILED=-1;
	public static int EXCEPTION=-99;
	
	abstract public void onResult(Object obj1, Object obj2, int code);
}