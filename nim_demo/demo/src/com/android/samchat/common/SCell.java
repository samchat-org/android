package com.android.samchat.common;
public class SCell{
	public static int RADIO_TYPE_GSM=0;
	public static int RADIO_TYPE_WCDMA=1;
	public static int RADIO_TYPE_LTE=2;
	public static int RADIO_TYPE_CDMA=3;
	
	public int radioType;
	public int mcc;
	public int mnc;
	public int cid;
	public int lac;

		
	public SCell(){
		mcc = 0;
		mnc = 0;
		cid = 0;
		lac = 0;
		radioType = -1;
	}
}