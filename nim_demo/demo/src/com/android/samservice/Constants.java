package com.android.samservice;


public class Constants{


	
	/*
		true: server use username to be easemob id
		false: server use cellphone to be easemob id
	*/
	public static final boolean USERNAME_EQUAL_EASEMOB_ID = true;
	public static final String COUNTRY_CODE = "country_code";
	public static final String CELLPHONE_NUMBER = "cellphone";

	public static final String SEX_SELECT = "sex_select";
	public static final String BASIC_INFO_TYPE = "basic_info_type";
	public static final String BASIC_INFO_DEFAULT_VALUE="basic_info_default_value";

	public static final String ACTION_FOLLOWER_CHANAGED = "action_follower_changed";

	public static final String NEED_BACK_PREV_FRAGMENT= "need_back_prev_fragment";

	
	public static final String ACTION_CONTACT_CHANAGED = "action_contact_changed";
	public static final String ACCOUNT_CONFLICT = "conflict";
	public static final String ACCOUNT_REMOVED = "account_removed";

	public static final String ACTION_AVATAR_UPDATE = "action_avatar_update";

	public static final String ACTION_QAACTIVITY_DESTROYED = "qa_activity_destroyed";
	
	public static final String GROUP_MEMBER_INFO_UPDATE="group_member_info_update";

	public static final String ACTION_NEW_MSG_FROM_SERVICE = "new_msg_from_service";
	public static final String ACTION_NEW_MSG_FROM_CHAT = "new_msg_from_chat";
	public static final String ACTION_NEW_MSG_FROM_VENDOR = "new_msg_from_vendor";

	public static final String ACTION_NEW_MSG_FROM_GROUP = "new_msg_from_group";


	public static final boolean DEBUG=true;

	/****************************User Info***********************************************/	
	public static final int USER = 0;
	public static final int SAM_PROS=1;

	public static final int NO_TAG = 0;
	public static final int TAG = 1;

	/****************************msg***********************************************/
	public static final int MSG_READ = 0;
	public static final int MSG_UNREAD = 1;

	/****************************Avatar update of type***********************************************/
	public static final int AVATAR_UPDATE_USER = 0;
	public static final int AVATAR_UPDATE_SAM_PROS = 1;

	/****************************Http Command Config***********************************************/
	public static final boolean POST_CMD = false;

	/****************************Login Final Config***********************************************/
	public static final int MIN_MPHONE_NUMBER_LENGTH = 6;
	public static final int MAX_MPHONE_NUMBER_LENGTH = 15;	
	public static final int MIN_USERNAME_LENGTH = 3;
	public static final int MAX_USERNAME_LENGTH = 15;
	public static final int MIN_PASSWORD_LENGTH = 6;
	public static final int MAX_PASSWORD_LENGTH = 32;

	/****************************Storage Path***********************************************/
	public static String AVATAR_FOLDER = "/avfolder";
	public static String AVATAR="/avatar";
	public static String FG_PIC_FOLDER = "/fgfolder";
	public static String FG_PIC="/fgpic";

	/****************************code request from which page***********************************************/
	public static final int FROM_SIGNUP = 0;
	public static final int FROM_FORGETPWD = 1;

	public static final int FROM_CUSTOMER_ACTIVITY_LAUNCH = 0;
	public static final int FROM_SP_ACTIVITY_LAUNCH = 1;

	/****************************Broadcast***********************************************/
	public static final String BROADCAST_SWITCH_MODE = "com.android.samchat.switchmode";
	public static final String BROADCAST_SIGN_IN_ALREADY = "com.android.samchat.signinalready";
	public static final String BROADCAST_SIGN_UP_ALREADY = "com.android.samchat.signupalready";
	public static final String BROADCAST_FINDPWD_ALREADY = "com.android.samchat.findpwdalready";
	public static final String BROADCAST_CREATE_SP_SUCCESS = "com.android.samchat.createspsuccess";
	public static final String BROADCAST_CUSTOMER_ITEMS_UPDATE = "com.android.samchat.customeritemsupdate";
	public static final String BROADCAST_USER_INFO_UPDATE = "com.android.samchat.userinfoupdate";
	public static final String BROADCAST_FOLLOWEDSP_UPDATE = "com.android.samchat.followedspupdate";
	public static final String BROADCAST_CONTACTLIST_UPDATE = "com.android.samchat.contactlistupdate";
	public static final String BROADCAST_CUSTOMERLIST_UPDATE = "com.android.samchat.customerlistupdate";
	public static final String BROADCAST_MYSELF_AVATAR_UPDATE = "com.android.samchat.myselfavatarupdate";

	/****************************Message From***********************************************/
	public static final String MSG_FROM = "msg_from";
	public static final int FROM_CUSTOMER = 0;
	public static final int FROM_SP = 1;



	/****************************Start Activity Confirm ID***********************************************/
	public static final String CONFIRM_COUNTRYCODE = "countrycode";





	/****************************Http Error Code***********************************************/
	/*Connection Error Code*/
	public static final int CONNECTION_HTTP_ERROR = -1000;
	public static final int CONNECTION_TIMEOUT_ERROR = -1001;

	/*DB Warning Code*/
	public static final int DB_OPT_ERROR = -1100;


	/*Exception Code*/
	public static final int EXCEPTION_ERROR = -2000;

	/*Ret Code*/
	//-1 ~ -99: coding error
	public static final int RET_HTTP_PARSED_FAILED_ERROR = -1;
	public static final int RET_ACTION_NOT_SUPPORT_ERROR = -2;
	public static final int RET_PARAM_NOT_SUPPORT_ERROR = -3;
	public static final int RET_TOKEN_FORMAT_ERROR = -4;

	//-100 ~ -199: server error
	public static final int RET_SERVER_INTERNAL_ERROR = -103;

	//-200 ~ -299: 
	public static final int RET_NUMBER_ALREADY_REGISTERED_ERROR = -201;
	public static final int RET_NUMBER_ILLEAGE_ERROR = -202;
	public static final int RET_PASSWORD_ERROR = -203;
	public static final int RET_NUMBER_NOT_REGISTERED_ERROR = -204;
	public static final int RET_VERIFY_CODE_ERROR = -205;
	public static final int RET_REQUEST_VERIFY_CODE_TOO_OFTEN = -206;
	public static final int RET_USER_NOT_EXISTED = -207;
	public static final int RET_PASSWORD_ERROR_TOO_OFTEN = -208;
	public static final int RET_QUERY_USER_TOO_OFTEN = -209;
	public static final int RET_SEND_INVITE_MSG_TOO_OFTEN = -210;
	public static final int RET_VERIFY_CODE_EXPIRATION = -211;

	//-300 ~ -399:

	//-400 ~ -499: securitye error
	public static final int RET_TOKEN_ILLEAGE_ERROR = -401;

	//-500 ~ -599: 
	public static final int RET_ALREADY_UPGRADE_ERROR = -501;
	public static final int RET_ORGINAL_PASSWORD_ERROR = -502;
	public static final int RET_PUBLIC_NOT_EXISTED_ERROR = -503;
	public static final int RET_ADV_NOT_EXISTED_ERROR = -504;
	public static final int RET_WAIT_FOR_AUDIT_ERROR = -505;
	public static final int RET_FOLLOW_MAXIUM_ERROR = -506;
	public static final int RET_HAVE_NOT_FOLLOW_ERROR = -507;
	public static final int RET_HAVE_NOT_ADD_CONTACT_ERROR = -508;
	public static final int RET_SEND_QUESTION_TOO_OFFTEN = -509;
	public static final int RET_NOT_SERVICE_PROVIDER_ERROR = -510;

	/****************************Longitude/Latitude null value***********************************************/
	public static final double CONSTANTS_LONGITUDE_LATITUDE_NULL = -400;

	/****************************Question Status***********************************************/
	public static final int QUESTION_STATUS_INACTIVE = 0;
	public static final int QUESTION_STATUS_ACTIVE = 1;

	public static final int QUESTION_NOT_RESPONSED = 0;
	public static final int QUESTION_RESPONSED = 1;

	public static final int QUESTION_UNREAD = 0;
	public static final int QUESTION_READ = 1;

	/****************************Advertisement Type***********************************************/
	public static final int ADV_TYPE_TEXT = 0;
	public static final int ADV_TYPE_PIC = 1;
	public static final int ADV_TYPE_VEDIO = 2;

	public static final int ADV_NOT_RESPONSED = 0;
	public static final int ADV_RESPONSED = 1;

	/****************************Contact OPT***********************************************/
	public static final int CONTACT_OPT_ADD = 0;
	public static final int CONTACT_OPT_REMOVE = 1;
	/****************************Contact TYPE ***********************************************/
	public static final int ADD_INTO_CONTACT = 0;
	public static final int ADD_INTO_CUSTOMER = 1;
	public static final int REMOVE_OUT_CONTACT = 0;
	public static final int REMOVE_OUT_CUSTOMER = 1;

	/****************************Push Category Type ***********************************************/
	public static final int PUSH_CATEGORY_UNKONW = 0;
	public static final int PUSH_CATEGORY_QUESTION = 1;
	public static final int PUSH_CATEGORY_ADV = 2;

	/****************************Advertisement Max Picutre pixel***********************************************/
	public static final int ADV_PIC_MAX = 1280;
	/****************************Advertisement Max Picutre pixel***********************************************/
	public static final int AVATAR_PIC_MAX = 200;
}

