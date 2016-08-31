package com.netease.nim.demo.login;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.chatroom.helper.ChatRoomHelper;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.NimUIKit;

/*SAMC_BEGIN(stop push service when logout)*/
import com.igexin.sdk.PushManager;
import com.android.samservice.SamService;
import com.android.samchat.service.SamDBManager;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.netease.nim.uikit.common.util.log.LogUtil;
/*SAMC_END(stop push service when logout)*/

/**
 * 注销帮助类
 * Created by huangjun on 2015/10/8.
 */
public class LogoutHelper {
    public static void logout() {
        // 清理缓存&注销监听&清除状态
        NimUIKit.clearCache();
        ChatRoomHelper.logout();
        DemoCache.clear();
        LoginSyncDataStatusObserver.getInstance().reset();

        /*SAMC_BEGIN(stop push service when logout)*/
        SamDBManager.getInstance().registerObservers(false);
        SamService.getInstance().stopSamService();
        PushManager.getInstance().unBindAlias(DemoCache.getContext(),Preferences.getUserAlias(),true);
        PushManager.getInstance().stopService(DemoCache.getContext());
        Preferences.saveUserAlias("");
        SamchatDataCacheManager.clearDataCache();
        /*SAMC_END(stop push service when logout)*/
    }
}
