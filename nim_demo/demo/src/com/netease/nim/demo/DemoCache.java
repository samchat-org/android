package com.netease.nim.demo;

import android.content.Context;

import com.android.samchat.service.StatusBarQuestionNotificationConfig;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by jezhee on 2/20/15.
 */
public class DemoCache {

    private static Context context;

    private static String account;

    private static StatusBarNotificationConfig notificationConfig;

    private static StatusBarQuestionNotificationConfig questionNotificationConfig;

    private static NimApplication app;

    public static void setQuestionNotificationConfig(StatusBarQuestionNotificationConfig questionNotificationConfig) {
        DemoCache.questionNotificationConfig = questionNotificationConfig;
    }

    public static StatusBarQuestionNotificationConfig getQuestionNotificationConfig() {
        return questionNotificationConfig;
    }

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

    public static void setApp(NimApplication app){
		DemoCache.app = app;
    }

    public static NimApplication getApp(){
		return DemoCache.app;
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
