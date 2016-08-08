package com.netease.nim.demo;

import android.content.Context;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by jezhee on 2/20/15.
 */
public class DemoCache {

    private static Context context;

    private static String account;

    private static StatusBarNotificationConfig notificationConfig;

    /*SAMC_BEGIN(temp account before login succeed)*/
    private static String taccount;
	 public static String getTAccount() {
        return taccount;
    }

    public static void setTAccount(String account) {
        DemoCache.taccount = account;
    }
    /*SAMC_BEGIN(temp account before login succeed)*/
    public static void clear() {
        account = null;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        DemoCache.account = account;
        NimUIKit.setAccount(account);
    }

    public static void setNotificationConfig(StatusBarNotificationConfig notificationConfig) {
        DemoCache.notificationConfig = notificationConfig;
    }

    public static StatusBarNotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        DemoCache.context = context.getApplicationContext();
    }

    /*SAMC_BEGIN(SamChat Switch Tag)*/
    static private int switchTag = 0;
    public static void addTag(){
        switchTag++;
    }

    public static int getTag(){
        return switchTag;
    }

    public static void clearTag(){
        switchTag = 0;
    }

    static private boolean switching = false;
    public static void setSwitching(boolean s){
        switching = s;
    }

    public static boolean getSwitching(){
        return switching;
    }
		
    /*SAMC_BEGIN(SamChat Switch Tag)*/
		
}
