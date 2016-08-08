package com.netease.nim.uikit.session.module;

import android.app.Activity;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

/**
 * Created by zhoujianghua on 2015/7/6.
 */
public class Container {
    public final Activity activity;
    public final String account;
    public final SessionTypeEnum sessionType;
    /*SAMC_BEGIN(support mode)*/
    public final int mode;
    /*SAMC_END(support mode)*/
    public final ModuleProxy proxy;

    public Container(Activity activity, String account, SessionTypeEnum sessionType, ModuleProxy proxy) {
        this.activity = activity;
        this.account = account;
        this.mode = 0;
        this.sessionType = sessionType;
        this.proxy = proxy;
    }

    public Container(Activity activity, String account, int mode,SessionTypeEnum sessionType, ModuleProxy proxy) {
        this.activity = activity;
        this.account = account;
        this.mode = mode;
        this.sessionType = sessionType;
        this.proxy = proxy;
    }
}
