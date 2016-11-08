package com.android.samchat.service;

import android.app.Activity;

public class StatusBarQuestionNotificationConfig{
	public String downTimeBegin;
	public String downTimeEnd;
	public boolean downTimeToggle;
	public boolean ring;
	public boolean vibrate;
	public java.lang.Class<? extends Activity>	notificationEntrance;
	public int notificationSmallIconId;
	public String notificationSound;
	public boolean hideContent;
	public int ledARGB;
	public int ledOnMs;
	public int ledOffMs;
	public boolean titleOnlyShowAppName;

	public StatusBarQuestionNotificationConfig(){
		downTimeBegin=null;
		downTimeEnd = null;
		downTimeToggle = false;
		ring = false;
		vibrate = false;
		notificationEntrance = null;
		notificationSmallIconId = -1;
		notificationSound = null;
		hideContent = false;
		ledARGB = 0;
		ledOnMs = 3000;
		ledOffMs = 3000;
		titleOnlyShowAppName=false;
	}
}
