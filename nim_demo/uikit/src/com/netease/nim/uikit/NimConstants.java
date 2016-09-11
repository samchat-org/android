package com.netease.nim.uikit;


public class NimConstants{
	
	public static final int MSG_TYPE_IM = 0;
	public static final int MSG_TYPE_SQ = 1; 
	public static final int MSG_TYPE_RQ = 2; 
	public static final int MSG_TYPE_SEND_ADV = 3;
	public static final int MSG_TYPE_RCVD_ADV = 4;
	
	public static final int MSG_SUBTYPE_DEFAULT = 99;
	public static final int MSG_STATUS_DEFAULT = 99;

	public static final String MSG_FROM="msg_from";
	public static final int FROM_CUSTOMER = 0;
	public static final int FROM_SP = 1;

	
	public static final String QUEST_ID = "quest_id";
	public static final String SQ_QUEST_ID = "sq_quest_id";

	public static final String ADV_ID = "adv_id";
	public static final String SA_ADV_ID = "sa_adv_id";

	public static final String MSG_TYPE="msg_type";

	public static final int AVCHAT_DISCONNECTED=0;
	public static final int AVCHAT_CONNECTED=1;

	public static final String AVCHAT_MODE_COUSTOMER="customer";
	public static final String AVCHAT_MODE_SP="sp";


	public static final long RECENT_TAG_STICKY = 1;
	public static final long RECENT_TAG_CUSTOMER_ROLE = 2;
	public static final long RECENT_TAG_SP_ROLE = 4;
	public static final long RECENT_TAG_STICKY_CUSTOMER_ROLE = 8;
	public static final long RECENT_TAG_STICKY_SP_ROLE = 16;


	public static final String SESSION_ACCOUNT_ADVERTISEMENT = "999999";

	public static final String S3_BUCKETNAME = "samchat2";
	public static final String S3_URL = "https://s3-us-west-2.amazonaws.com/samchat2/";
	public static final String S3_PATH_ADV = "advertisement/";
	public static final String S3_FOLDER_ORIGIN = "origin/";
	public static final String S3_FOLDER_THUMB = "thumb/";

	public static final String S3_PATH_AVATAR = "avatar/";

	public static final String S3_ORIG="s3_orig";

}

