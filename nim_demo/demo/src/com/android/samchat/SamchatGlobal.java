package com.android.samchat;

import com.netease.nim.uikit.common.type.ModeEnum;

public class SamchatGlobal {
	public static ModeEnum mode = ModeEnum.CUSTOMER_MODE;
	public static int app_advertisement_recall_minute = 2;// default 2 mins
	
	public static ModeEnum getmode(){
		return mode;
	}

	public static void switchMode(){
		if(mode == ModeEnum.CUSTOMER_MODE){
			mode = ModeEnum.SP_MODE;
		}else{
			mode = ModeEnum.CUSTOMER_MODE;
		}
	}

	public static void setmode(ModeEnum m){
		mode = m;
	}

	public static long getoneWeekSysTime(){
		return (System.currentTimeMillis() - 7*24*60*60*1000L);
	}

	public static boolean isCustomerMode(){
		return (mode == ModeEnum.CUSTOMER_MODE);
	}

	public static void setapp_advertisement_recall_minute(int min){
		app_advertisement_recall_minute = min;
	}

	public static int getapp_advertisement_recall_minute(){
		return app_advertisement_recall_minute;
	}
}