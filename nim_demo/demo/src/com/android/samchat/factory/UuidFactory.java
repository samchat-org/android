package com.android.samchat.factory;
import android.telephony.TelephonyManager;
import android.content.Context;
import java.util.UUID;

import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.demo.DemoCache;
public class UuidFactory {
	private static String TAG="UuidFactory";
	private static UuidFactory factory;
	public UuidFactory(){

	}
	
	public static UuidFactory getInstance(){
		if(factory == null){
			factory = new UuidFactory();
		}
		return factory;
	}
	
    public String getDeviceId(){
		try{
			final TelephonyManager tm = (TelephonyManager) DemoCache.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			final String tmDevice, tmSerial, tmPhone, androidId;
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			androidId = "" + android.provider.Settings.Secure.getString(DemoCache.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
			String uniqueId = (StringUtil.makeMd5(deviceUuid.toString())).substring(11, 17);
			LogUtil.i(TAG,"id:"+uniqueId);
			return uniqueId;
		}catch(Exception e){
			LogUtil.i(TAG,"id:032792");
			return "032792";
		}
	}  
}