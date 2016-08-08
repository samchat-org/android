package com.android.samchat;
import com.android.samchat.type.ModeEnum;

public class SamchatGlobal {
	public static ModeEnum mode = ModeEnum.CUSTOMER_MODE;
	
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
}