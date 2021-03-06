package com.netease.nim.demo.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.android.samchat.service.StatusBarQuestionNotificationConfig;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class UserPreferences {
    private final static String KEY_DOWNTIME_TOGGLE ="down_time_toggle";
    private final static String KEY_SB_NOTIFY_TOGGLE="sb_notify_toggle";
    private final static String KEY_TEAM_ANNOUNCE_CLOSED = "team_announce_closed";
    private final static String KEY_STATUS_BAR_NOTIFICATION_CONFIG = "KEY_STATUS_BAR_NOTIFICATION_CONFIG";

    private final static String KEY_AVCHAT_SERVER_AUDIO_RECORD = "KEY_AVCHAT_SERVER_AUDIO_RECORD";
    private final static String KEY_AVCHAT_SERVER_VIDEO_RECORD = "KEY_AVCHAT_SERVER_VIDEO_RECORD";

    // 测试过滤通知
    private final static String KEY_MSG_IGNORE = "KEY_MSG_IGNORE";
    // 响铃配置
    private final static String KEY_RING_TOGGLE = "KEY_RING_TOGGLE";
    // 呼吸灯配置
    private final static String KEY_LED_TOGGLE = "KEY_LED_TOGGLE";
    // 通知栏标题配置
    private final static String KEY_NOTICE_CONTENT_TOGGLE = "KEY_NOTICE_CONTENT_TOGGLE";

    private final static String KEY_VIBRATE_TOGGLE = "KEY_VIBRATE_TOGGLE";

    private final static String KEY_SP_REQUEST_TOGGLE = "KEY_REQUEST_TOGGLE";

    public static void setMsgIgnore(boolean enable) {
        saveBoolean(KEY_MSG_IGNORE, enable);
    }

    public static boolean getMsgIgnore() {
        return getBoolean(KEY_MSG_IGNORE, false);
    }

    public static void setAVChatServerAudioRecord(boolean enable) {
        saveBoolean(KEY_AVCHAT_SERVER_AUDIO_RECORD, enable);
    }

    public static boolean getAVChatServerAudioRecord() {
        return getBoolean(KEY_AVCHAT_SERVER_AUDIO_RECORD, false);
    }

    public static void setAVChatServerVideoRecord(boolean enable) {
        saveBoolean(KEY_AVCHAT_SERVER_VIDEO_RECORD, enable);
    }

    public static boolean getAVChatServerVideoRecord() {
        return getBoolean(KEY_AVCHAT_SERVER_VIDEO_RECORD, false);
    }

    public static void setNotificationToggle(boolean on) {
        saveBoolean(KEY_SB_NOTIFY_TOGGLE, on);
    }

    public static boolean getNotificationToggle() {
        return getBoolean(KEY_SB_NOTIFY_TOGGLE, true);
    }

    public static void setRingToggle(boolean on) {
        saveBoolean(KEY_RING_TOGGLE, on);
    }

    public static boolean getRingToggle() {
        return getBoolean(KEY_RING_TOGGLE, true);
    }

    public static void setVibrateToggle(boolean on) {
        saveBoolean(KEY_VIBRATE_TOGGLE, on);
    }

    public static boolean getVibrateToggle() {
        return getBoolean(KEY_VIBRATE_TOGGLE, true);
    }

    public static void setRequestToggle(boolean on) {
        saveBoolean(KEY_SP_REQUEST_TOGGLE, on);
    }

    public static boolean getRequestToggle() {
        return getBoolean(KEY_SP_REQUEST_TOGGLE, true);
    }

    public static void setLedToggle(boolean on) {
        saveBoolean(KEY_LED_TOGGLE, on);
    }

    public static boolean getLedToggle() {
        return getBoolean(KEY_LED_TOGGLE, false);
    }

    public static boolean getNoticeContentToggle() {
        return getBoolean(KEY_NOTICE_CONTENT_TOGGLE, false);
    }

    public static void setNoticeContentToggle(boolean on) {
        saveBoolean(KEY_NOTICE_CONTENT_TOGGLE, on);
    }

    public static void setDownTimeToggle(boolean on) {
        saveBoolean(KEY_DOWNTIME_TOGGLE, on);
    }

    public static boolean getDownTimeToggle() {
        return getBoolean(KEY_DOWNTIME_TOGGLE, false);
    }

    public static void setStatusConfig(StatusBarNotificationConfig config) {
        saveStatusBarNotificationConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG, config);
    }

    public static StatusBarNotificationConfig getStatusConfig() {
        return getConfig(KEY_STATUS_BAR_NOTIFICATION_CONFIG);
    }

    public static void setTeamAnnounceClosed(String teamId, boolean closed) {
        saveBoolean(KEY_TEAM_ANNOUNCE_CLOSED + teamId, closed);
    }

    public static boolean getTeamAnnounceClosed(String teamId) {
        return getBoolean(KEY_TEAM_ANNOUNCE_CLOSED + teamId, false);
    }

    private static StatusBarNotificationConfig getConfig(String key) {
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        String jsonString = getSharedPreferences().getString(key, "");
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            if (jsonObject == null) {
                return null;
            }
            config.downTimeBegin = jsonObject.getString("downTimeBegin");
            config.downTimeEnd = jsonObject.getString("downTimeEnd");
            config.downTimeToggle = jsonObject.getBoolean("downTimeToggle");
            config.ring = jsonObject.getBoolean("ring");
            config.vibrate = jsonObject.getBoolean("vibrate");
            config.notificationSmallIconId = jsonObject.getIntValue("notificationSmallIconId");
            config.notificationSound = jsonObject.getString("notificationSound");
            config.hideContent = jsonObject.getBoolean("hideContent");
            config.ledARGB = jsonObject.getIntValue("ledargb");
            config.ledOnMs = jsonObject.getIntValue("ledonms");
            config.ledOffMs = jsonObject.getIntValue("ledoffms");
            config.titleOnlyShowAppName = jsonObject.getBoolean("titleOnlyShowAppName");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    private static void saveStatusBarNotificationConfig(String key , StatusBarNotificationConfig config) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("downTimeBegin", config.downTimeBegin);
            jsonObject.put("downTimeEnd", config.downTimeEnd);
            jsonObject.put("downTimeToggle", config.downTimeToggle);
            jsonObject.put("ring", config.ring);
            jsonObject.put("vibrate", config.vibrate);
            jsonObject.put("notificationSmallIconId", config.notificationSmallIconId);
            jsonObject.put("notificationSound", config.notificationSound);
            jsonObject.put("hideContent", config.hideContent);
            jsonObject.put("ledargb", config.ledARGB);
            jsonObject.put("ledonms", config.ledOnMs);
            jsonObject.put("ledoffms", config.ledOffMs);
            jsonObject.put("titleOnlyShowAppName", config.titleOnlyShowAppName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(key, jsonObject.toString());
        editor.commit();
    }
		
    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo." + DemoCache.getAccount(), Context.MODE_PRIVATE);
    }

	private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, "");
    }
		
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
}
