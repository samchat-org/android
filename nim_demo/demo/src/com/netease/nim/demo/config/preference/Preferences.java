package com.netease.nim.demo.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.string.StringUtil;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
		
    /*SAMC_BEGIN(GETU Alias)*/
    private static final String KEY_USER_ALIAS = "alias";
    public static void saveUserAlias(String alias) {
        saveString(KEY_USER_ALIAS, alias);
    }

    public static String getUserAlias() {
        return getString(KEY_USER_ALIAS);
    }
    /*SAMC_END(GETU Alias)*/

    /*SAMC_BEGIN(GETU Alias)*/
    private static final String KEY_FOLLOW_LIST_UPDATE = "fldate";
    public static void saveFldate(String date) {
        saveString(KEY_FOLLOW_LIST_UPDATE, date);
    }

    public static String getFldate() {
        return getString(KEY_FOLLOW_LIST_UPDATE);
    }

	private static final String KEY_CONTACT_LIST_UPDATE = "ccdate";
    public static void saveCcdate(String date) {
        saveString(KEY_CONTACT_LIST_UPDATE, date);
    }

    public static String getCcdate() {
        return getString(KEY_CONTACT_LIST_UPDATE);
    }

    private static final String KEY_CUSTOMER_LIST_UPDATE = "cudate";
    public static void saveCudate(String date) {
        saveString(KEY_CUSTOMER_LIST_UPDATE, date);
    }

    public static String getCudate() {
        return getString(KEY_CUSTOMER_LIST_UPDATE);
    }

    public static void clearSyncDate(){
		SharedPreferences.Editor editor = getSharedPreferences().edit();
       editor.remove(KEY_FOLLOW_LIST_UPDATE);
		editor.remove(KEY_CONTACT_LIST_UPDATE);
		editor.remove(KEY_CUSTOMER_LIST_UPDATE);
		editor.commit();
    }

    public static void clearAllDate(){
       SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.remove(KEY_USER_ACCOUNT);
		editor.remove(KEY_USER_TOKEN);
		editor.remove(KEY_USER_ALIAS);
       editor.remove(KEY_FOLLOW_LIST_UPDATE);
		editor.remove(KEY_CONTACT_LIST_UPDATE);
		editor.remove(KEY_CUSTOMER_LIST_UPDATE);
		editor.remove(KEY_MODE);
		editor.commit();
    }
    /*SAMC_END(GETU Alias)*/

	/*SAMC_BEGIN(current mode)*/
	private static final String KEY_MODE = "mode";
	public static void saveMode(int mode) {
		saveString(KEY_MODE, String.valueOf(mode));
	}

	public static int getMode() {
		String str = getString(KEY_MODE);
		if(StringUtil.isEmpty(str)){
			return ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE);
		}
		
		return Integer.valueOf(str).intValue();
	}
	/*SAMC_END(current mode)*/

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
