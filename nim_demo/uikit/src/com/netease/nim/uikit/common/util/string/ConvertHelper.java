package com.netease.nim.uikit.common.util.string;

import com.netease.nim.uikit.NimConstants;

public class ConvertHelper{
	static public long stringTolong(String s){
	 	long ret = -1;
		String account = s;
		if (s.startsWith(NimConstants.PUBLIC_ACCOUNT_PREFIX)) {
            account = s.substring(s.indexOf(NimConstants.PUBLIC_ACCOUNT_PREFIX) + NimConstants.PUBLIC_ACCOUNT_PREFIX.length());
		}
		try{
			ret = Long.valueOf(account);
		}catch(Exception e){
			e.printStackTrace();
			return ret;
		}
		return ret;
	}
	
}
