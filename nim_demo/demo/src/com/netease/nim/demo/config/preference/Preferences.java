package com.netease.nim.demo.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.samchat.SamchatGlobal;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.string.StringUtil;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
	private static final String KEY_USER_ACCOUNT = "account";
	private static final String KEY_USER_TOKEN = "token";
	private static final String KEY_SYSTEM_AVRECALL = "avrecall";

	public static void saveAVRecall(int recall) {
		saveString(KEY_SYSTEM_AVRECALL, String.valueOf(recall));
	}

	public static int getAVRecall() {
		String str = getString(KEY_SYSTEM_AVRECALL);
		if(StringUtil.isEmpty(str)){
			return SamchatGlobal.app_advertisement_recall_minute;
		}
		
		return Integer.valueOf(str).intValue();
	}

    public static void saveUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static void saveUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }
}
