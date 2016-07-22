package com.netease.nim.demo.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.demo.DemoCache;

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
