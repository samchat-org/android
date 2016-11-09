package com.android.samservice;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.common.SCell;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.dao.SamDBDao;
import com.android.samservice.info.*;
import com.android.samservice.type.TypeEnum;
import com.netease.nim.demo.DemoCache;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.demo.config.preference.Preferences;
import com.igexin.sdk.PushManager;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.coreobj.*;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class SamService{
	public static final String TAG="SamService";

	public static final int SAMSERVICE_RETRY_WAIT=3000;
	public static final int SAMSERVICE_HANDLE_TIMEOUT=30000;
    public static final int SAMSERVICE_STATUS_CHECK_TIMOUT=360000;

	public static String sam_cache_path;
	public static String sam_download_path;

	private static SamService mSamService;
	private static Context mContext;

	private HandlerThread mHandlerTimeOutThread;
	private SamServiceTimeOutHandler mHandlerTimeOutHandler;
	
	private HandlerThread mHandlerThread;
	private SamServiceHandler mSamServiceHandler;

	private ExecutorService mFixedHttpThreadPool;
	
	private String current_token;
	private ContactUser current_user;

	private SamDBDao dao;
	private String dbFolder;

	private String clientID;
	private static final int  INITED=0;
	private static final int  SYNCING=1;
	private static final int  SYNCED=2;

	private static int SYNC_QUERY_STATE=0;
	private static int SYNC_FOLLOW_LIST=1;
	private static int SYNC_CONTACT_LIST=2;
	private static int SYNC_CUSTOMER_LIST=3;
	private static int SYNC_BIND=4;
	private static int SYNC_SEND_CLIENT_ID=5;
	private boolean startSync = false;
	private boolean clientidReady = false;
	private int [] syncStatus = {INITED,INITED,INITED,INITED,INITED,INITED};
	private Object syncStatusObject = new Object();
	private StateDateInfo sdinfo;
	
	/*MSG ID*/
	//timeout msg:
	public static final int MSG_HANDLE_TIMEOUT = 1;
	//sign msg:
	public static final int MSG_SIGN_START = 100;
	public static final int MSG_REGISTER_CODE_REQEUST = MSG_SIGN_START + 1;
	public static final int MSG_REGISTER_CODE_VERIFY = MSG_REGISTER_CODE_REQEUST + 1;
	public static final int MSG_SIGN_UP = MSG_REGISTER_CODE_VERIFY + 1;
	public static final int MSG_SIGN_IN = MSG_SIGN_UP + 1;
	public static final int MSG_AUTO_SIGN_IN = MSG_SIGN_IN + 1;
	public static final int MSG_SIGN_OUT = MSG_AUTO_SIGN_IN + 1;

	//bussiness msg:
	public static final int MSG_ID_START = 200;
	public static final int MSG_GET_PUSH_APP_KEY = MSG_ID_START + 1;
	public static final int MSG_CREATE_SAM_PROS = MSG_GET_PUSH_APP_KEY + 1;
	public static final int MSG_FINDPWD_CODE_REQUEST = MSG_CREATE_SAM_PROS + 1;
	public static final int MSG_FINDPWD_CODE_VERIFY = MSG_FINDPWD_CODE_REQUEST + 1;
	public static final int MSG_FINDPWD_UPDATE = MSG_FINDPWD_CODE_VERIFY + 1;
	public static final int MSG_UPDATE_PASSWORD = MSG_FINDPWD_UPDATE + 1;
	public static final int MSG_SEND_QUESTION = MSG_UPDATE_PASSWORD + 1;
	public static final int MSG_GET_PLACES_INFO = MSG_SEND_QUESTION + 1;
	public static final int MSG_PUBLIC_FOLLOW = MSG_GET_PLACES_INFO + 1;
	public static final int MSG_PUBLIC_BLOCK = MSG_PUBLIC_FOLLOW + 1;
	public static final int MSG_PUBLIC_FAVOURITE = MSG_PUBLIC_BLOCK + 1;
	public static final int MSG_QUERY_USER_FUZZY = MSG_PUBLIC_FAVOURITE + 1;
	public static final int MSG_QUERY_USER_PRECISE = MSG_QUERY_USER_FUZZY + 1;
	public static final int MSG_QUERY_USER_MULT = MSG_QUERY_USER_PRECISE + 1;
	public static final int MSG_QUERY_USER_WITHOUT_TOKEN = MSG_QUERY_USER_MULT + 1;
	public static final int MSG_SEND_INVITE_MSG = MSG_QUERY_USER_WITHOUT_TOKEN + 1;
	public static final int MSG_EDIT_PROFILE = MSG_SEND_INVITE_MSG + 1;
	public static final int MSG_UPDATE_AVATAR = MSG_EDIT_PROFILE + 1;
	public static final int MSG_QUERY_PUBLIC = MSG_UPDATE_AVATAR + 1;
	public static final int MSG_ADD_CONTACT = MSG_QUERY_PUBLIC + 1;
	public static final int MSG_REMOVE_CONTACT = MSG_ADD_CONTACT + 1;
	public static final int MSG_SYNC_CONTACT_LIST = MSG_REMOVE_CONTACT + 1;
	public static final int MSG_SYNC_FOLLOW_LIST = MSG_SYNC_CONTACT_LIST + 1;
	public static final int MSG_WRITE_ADV = MSG_SYNC_FOLLOW_LIST + 1;
	public static final int MSG_DELETE_ADV = MSG_WRITE_ADV + 1;
	public static final int MSG_DOWNLOAD = MSG_DELETE_ADV + 1;
	public static final int MSG_BIND_ALIAS = MSG_DOWNLOAD + 1;
	public static final int MSG_SEND_CLIENTID = MSG_BIND_ALIAS + 1;
	public static final int MSG_QUERY_STATE = MSG_SEND_CLIENTID + 1;
	public static final int MSG_EDITCELLPHONE_CODE_REQUEST = MSG_QUERY_STATE + 1;
	public static final int MSG_EDITCELLPHONE_UPDATE = MSG_EDITCELLPHONE_CODE_REQUEST + 1;
	public static final int MSG_UPDATE_QUESTION_NOTIFY = MSG_EDITCELLPHONE_UPDATE + 1;
	
	public static final int MSG_SYNC_START = MSG_UPDATE_QUESTION_NOTIFY + 1;
	public static final int MSG_SYNC_CHECK = MSG_SYNC_START + 1;
	public static final int MSG_NETWORK_AVAILABLE = MSG_SYNC_CHECK + 1;
	public static final int MSG_CLIENTID_READY = MSG_NETWORK_AVAILABLE + 1;
	public static final int MSG_SYNC_QUERY_STATE_UPDATE = MSG_CLIENTID_READY + 1;
	public static final int MSG_SYNC_FOLLOW_LIST_UPDATE = MSG_SYNC_QUERY_STATE_UPDATE + 1;
	public static final int MSG_SYNC_CONTACT_LIST_UPDATE = MSG_SYNC_FOLLOW_LIST_UPDATE + 1;
	public static final int MSG_SYNC_CUSTOMER_LIST_UPDATE = MSG_SYNC_CONTACT_LIST_UPDATE + 1;
	public static final int MSG_SYNC_BIND_ALIAS_UPDATE = MSG_SYNC_CUSTOMER_LIST_UPDATE + 1;
	public static final int MSG_SYNC_SEND_CLIENT_ID_UPDATE = MSG_SYNC_BIND_ALIAS_UPDATE + 1;

	public static final int MSG_CLEAR_CACHE = MSG_SYNC_SEND_CLIENT_ID_UPDATE + 1;
	

	private boolean isTimeOut(SamCoreObj samobj){
		cancelTimeOut(samobj);
		synchronized(samobj){
			if(samobj.request_status == SamCoreObj.STATUS_INIT){
				samobj.request_status = SamCoreObj.STATUS_DONE;
			}else if(samobj.request_status == SamCoreObj.STATUS_TIMEOUT){
				return true;
			}
		}
		return false;
	}

/***************************************register-code-request*******************************************************/
	private void do_register_code_request(SamCoreObj samobj){
		VerifyCodeCoreObj vcobj = (VerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.register_code_request(vcobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_register_code_request(vcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
				samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}

	}

	private void retry_register_code_request(VerifyCodeCoreObj samobj){
		VerifyCodeCoreObj  retryobj = new VerifyCodeCoreObj(samobj.callback);
		retryobj.init_register_code_request(samobj.countrycode, samobj.cellphone,samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REGISTER_CODE_REQEUST, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

	public void register_code_request(String countrycode, String cellphone, String deviceid,SMCallBack callback){
		VerifyCodeCoreObj samobj = new VerifyCodeCoreObj(callback);
		samobj.init_register_code_request(countrycode, cellphone,deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REGISTER_CODE_REQEUST, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

/***************************************register-code-verify*******************************************************/
	private void do_register_code_verify(SamCoreObj samobj){
		VerifyCodeCoreObj vcobj = (VerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.register_code_verify(vcobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_register_code_verify(vcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
				samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}

	}

	private void retry_register_code_verify(VerifyCodeCoreObj samobj){
		VerifyCodeCoreObj  retryobj = new VerifyCodeCoreObj(samobj.callback);
		retryobj.init_register_code_verify(samobj.countrycode, samobj.cellphone,samobj.verifycode,samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REGISTER_CODE_VERIFY, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

	public void register_code_verify(String countrycode, String cellphone, String verifycode, String deviceid,SMCallBack callback){
		VerifyCodeCoreObj samobj = new VerifyCodeCoreObj(callback);
		samobj.init_register_code_verify(countrycode, cellphone,verifycode,deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REGISTER_CODE_VERIFY, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}
	
/***************************************signup*******************************************************/
	private void do_sign_up(SamCoreObj samobj){
		SignUpCoreObj suobj = (SignUpCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.signup(suobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				store_current_token(hcc.token_id+suobj.deviceid);
				initDao(StringUtil.makeMd5(""+hcc.userinfo.getunique_id()));
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_signup(suobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
		
    }

	private void retry_signup(SignUpCoreObj samobj){
		SignUpCoreObj  retryobj = new SignUpCoreObj(samobj.callback);
		retryobj.init(samobj.countrycode, samobj.cellphone, samobj.verifycode, samobj.username, samobj.password, samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_UP, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

	public void signup(String countrycode,String cellphone, String verifycode, String pwd, String username, String deviceid, SMCallBack callback)
	{
		SignUpCoreObj  samobj = new SignUpCoreObj(callback);
		samobj.init(countrycode, cellphone, verifycode, username, pwd,deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_UP, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

/***************************************signin*******************************************************/
	public void signin(String countrycode,String account, String pwd, String deviceid, SMCallBack callback){
		SignInCoreObj  samobj = new SignInCoreObj(callback);
		samobj.init( countrycode,  account,  pwd,  deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_IN, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_sign_in(SamCoreObj samobj){
		SignInCoreObj siobj = (SignInCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.signin(siobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				store_current_token(hcc.token_id+siobj.deviceid);
				initDao(StringUtil.makeMd5(""+hcc.userinfo.getunique_id()));
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_signin(siobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_signin(SignInCoreObj samobj){
		SignInCoreObj  retryobj = new SignInCoreObj(samobj.callback);

		retryobj.init( samobj.countrycode, samobj.account, samobj.password, samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_IN, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}
	
/***************************************get push app key*******************************************************/
	public void get_app_key(SMCallBack callback ){
		GetAppKeyCoreObj  samobj = new GetAppKeyCoreObj(callback);
		samobj.init(get_current_token());
		Message msg = mSamServiceHandler.obtainMessage(MSG_GET_PUSH_APP_KEY, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_get_app_key(SamCoreObj samobj){
		GetAppKeyCoreObj gakobj = (GetAppKeyCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.get_app_key(gakobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

/***************************************signout*******************************************************/
	public void signout(SMCallBack callback ){
		SignOutCoreObj  samobj = new SignOutCoreObj(callback);
		samobj.init(get_current_token());
		samobj.retry_count = 3;
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_OUT, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_sign_out(SamCoreObj samobj){
		SignOutCoreObj soobj = (SignOutCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.signout(soobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_signout(soobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_signout(SignOutCoreObj samobj){
		SignOutCoreObj  retryobj = new SignOutCoreObj(samobj.callback);
		retryobj.init(samobj.token);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SIGN_OUT, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************create-sam-pros*******************************************************/
	public void create_sam_pros(ContactUser sam_pros,SMCallBack callback ){
		CreateSamProsCoreObj samobj = new CreateSamProsCoreObj(callback);
		samobj.init(get_current_token(),  sam_pros);
		Message msg = mSamServiceHandler.obtainMessage(MSG_CREATE_SAM_PROS, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_create_sam_pros(SamCoreObj samobj){
		CreateSamProsCoreObj cspobj = (CreateSamProsCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.create_sam_pros(cspobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_create_sam_pros(cspobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_create_sam_pros(CreateSamProsCoreObj  samobj){
		CreateSamProsCoreObj  retryobj = new CreateSamProsCoreObj(samobj.callback);
		retryobj.init(samobj.token,samobj.sam_pros);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_CREATE_SAM_PROS, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************findpwd-code-request*******************************************************/
	public void findpwd_code_request(String countrycode, String cellphone, String deviceid,SMCallBack callback){
		VerifyCodeCoreObj samobj = new VerifyCodeCoreObj(callback);
		samobj.init_findpwd_code_request( countrycode,  cellphone,  deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_CODE_REQUEST, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_findpwd_code_request(SamCoreObj samobj){
		VerifyCodeCoreObj vcobj = (VerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.findpwd_code_request(vcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_findpwd_code_request(vcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_findpwd_code_request(VerifyCodeCoreObj samobj){
		VerifyCodeCoreObj  retryobj = new VerifyCodeCoreObj(samobj.callback);
		
		retryobj.init_findpwd_code_request( samobj.countrycode, samobj.cellphone, samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_CODE_REQUEST, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************findpwd-code-verify*******************************************************/
	public void findpwd_code_verify(String countrycode, String cellphone, String verifycode, String deviceid,SMCallBack callback){
		VerifyCodeCoreObj samobj = new VerifyCodeCoreObj(callback);
		samobj.init_findpwd_code_verify( countrycode,  cellphone,  verifycode,  deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_CODE_VERIFY, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_findpwd_code_verify(SamCoreObj samobj){
		VerifyCodeCoreObj vcobj = (VerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.findpwd_code_verify(vcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_findpwd_code_verify(vcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_findpwd_code_verify(VerifyCodeCoreObj samobj){
		VerifyCodeCoreObj  retryobj = new VerifyCodeCoreObj(samobj.callback);
		
		retryobj.init_findpwd_code_verify(samobj.countrycode, samobj.cellphone, samobj.verifycode, samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_CODE_VERIFY, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************findpwd-update*******************************************************/
	public void findpwd_update(String countrycode, String cellphone, String verifycode, String new_password, String deviceid,SMCallBack callback){
		VerifyCodeCoreObj samobj = new VerifyCodeCoreObj(callback);
		samobj.init_findpwd_update( countrycode,  cellphone,  verifycode,  new_password,  deviceid);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_UPDATE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_findpwd_update(SamCoreObj samobj){
		VerifyCodeCoreObj vcobj = (VerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.findpwd_update(vcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_findpwd_update(vcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_findpwd_update(VerifyCodeCoreObj samobj){
		VerifyCodeCoreObj  retryobj = new VerifyCodeCoreObj(samobj.callback);
		
		retryobj.init_findpwd_update(samobj.countrycode, samobj.cellphone, samobj.verifycode, samobj.new_password, samobj.deviceid);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_FINDPWD_UPDATE, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************update password*******************************************************/
	public void update_password(String old_pwd,String new_pwd,SMCallBack callback){
		UpdatePwdCoreObj samobj = new UpdatePwdCoreObj(callback);
		samobj.init( get_current_token(), old_pwd,  new_pwd);
		Message msg = mSamServiceHandler.obtainMessage(MSG_UPDATE_PASSWORD, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_update_password(SamCoreObj samobj){
		UpdatePwdCoreObj upobj = (UpdatePwdCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.update_password(upobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_update_password(upobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_update_password(UpdatePwdCoreObj samobj){
		UpdatePwdCoreObj  retryobj = new UpdatePwdCoreObj(samobj.callback);
		
		retryobj.init( samobj.token, samobj.old_pwd, samobj.new_pwd);

		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_UPDATE_PASSWORD, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************send question*******************************************************/
	public void send_question(String question, double latitude, double longitude, String place_id, String address, SCell cell, SMCallBack callback){
		SendqCoreObj samobj = new SendqCoreObj(callback);
		samobj.init(get_current_token(),  question,  latitude,  longitude,  place_id,  address, cell);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SEND_QUESTION, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_send_question(SamCoreObj samobj){
		SendqCoreObj sqobj = (SendqCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.send_question(sqobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				SendQuestion sq = new SendQuestion(hcc.qinfo.question_id, hcc.qinfo.question, hcc.qinfo.datetime,sqobj.address);
				if(dao.update_SendQuestion_db(sq) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_send_question(sqobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_send_question(SendqCoreObj samobj){
		SendqCoreObj  retryobj = new SendqCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.question, samobj.latitude, samobj.longitude, samobj.place_id, samobj.address,samobj.cell);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SEND_QUESTION, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************GetPlacesInfo*******************************************************/
	public void get_places_info(String key, SMCallBack callback){
		GetPlacesInfoCoreObj samobj = new GetPlacesInfoCoreObj(callback);
		samobj.init(get_current_token(), key);
		Message msg = mSamServiceHandler.obtainMessage(MSG_GET_PLACES_INFO, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_get_places_info(SamCoreObj samobj){
		GetPlacesInfoCoreObj gpiobj = (GetPlacesInfoCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.get_places_info(gpiobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_get_places_info(gpiobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_get_places_info(GetPlacesInfoCoreObj samobj){
		GetPlacesInfoCoreObj  retryobj = new GetPlacesInfoCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.key);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_GET_PLACES_INFO, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}



/***************************************follow/unfollow*******************************************************/
	public void follow(boolean isFollow,ContactUser sp, SMCallBack callback){
		FollowCoreObj samobj = new FollowCoreObj(callback);
		samobj.init( isFollow, get_current_token(), sp);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_FOLLOW, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_follow(SamCoreObj samobj){
		FollowCoreObj fcobj = (FollowCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.follow(fcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				if(fcobj.isFollow){
					FollowedSamPros fsp = new FollowedSamPros(hcc.userinfo.getunique_id(),hcc.userinfo.getusername());
					fsp.setavatar(hcc.userinfo.getavatar());
					fsp.setservice_category(hcc.userinfo.getservice_category());
					FollowDataCache.getInstance().addFollowSP(fsp.getunique_id(), fsp);
					if(dao.update_FollowList_db(fsp) == -1){
						samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
					}else{
						samobj.callback.onSuccess(hcc,0);
					}

					SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
					dao.update_ContactUser_db(hcc.userinfo);

				}else{
					dao.delete_FollowList_db_by_unique_id(hcc.userinfo.getunique_id());
					dao.delete_RcvdAdvSessionBySession(hcc.userinfo.getunique_id());
					FollowDataCache.getInstance().removeFollowSP(hcc.userinfo.getunique_id());
					SamDBManager.getInstance().clearUserTable(hcc.userinfo.getunique_id());
					samobj.callback.onSuccess(hcc,0);
				}
				
				Intent intent = new Intent();
				intent.setAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);
				sendbroadcast(intent);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_follow(fcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_follow(FollowCoreObj samobj){
		FollowCoreObj  retryobj = new FollowCoreObj(samobj.callback);
		
		retryobj.init(samobj.isFollow, samobj.token, samobj.sp);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_FOLLOW, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************block/unblock*******************************************************/
	public void block(boolean isBlock, ContactUser sam_pros, SMCallBack callback){
		BlockCoreObj samobj = new BlockCoreObj(callback);
		samobj.init( isBlock, get_current_token(),  sam_pros);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_BLOCK, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_block(SamCoreObj samobj){
		BlockCoreObj bcobj = (BlockCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.block(bcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				int tag = bcobj.isBlock ? Constants.TAG : Constants.NO_TAG;
				FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(bcobj.sam_pros.getunique_id());
				if(fsp != null){
					fsp.setblock_tag(tag);
				}else{
					samobj.callback.onError(Constants.EXCEPTION_ERROR);
					return;
				}
				
				if(dao.update_FollowList_db_BlockTag(hcc.userinfo.getunique_id(), tag) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
				
				ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(hcc.userinfo.getunique_id());
				if(user == null || user.getlastupdate() != hcc.latest_lastupdate){
					SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(hcc.userinfo.getunique_id());
				}

				Intent intent = new Intent();
				intent.setAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);
				sendbroadcast(intent);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_block(bcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_block(BlockCoreObj samobj){
		BlockCoreObj  retryobj = new BlockCoreObj(samobj.callback);
		
		retryobj.init(samobj.isBlock, samobj.token, samobj.sam_pros);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_BLOCK, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************favourite/unfavourite*******************************************************/
	public void favourite(boolean isFavourite, ContactUser sam_pros, SMCallBack callback){
		FavouriteCoreObj samobj = new FavouriteCoreObj(callback);
		samobj.init(isFavourite, get_current_token(), sam_pros);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_FAVOURITE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_favourite(SamCoreObj samobj){
		FavouriteCoreObj fcobj = (FavouriteCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.favourite(fcobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				int tag = fcobj.isFavourite? Constants.TAG : Constants.NO_TAG;
				FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(fcobj.sam_pros.getunique_id());
				if(fsp != null){
					fsp.setfavourite_tag(tag);
				}else{
					samobj.callback.onError(Constants.EXCEPTION_ERROR);
					return;
				}
				
				if(dao.update_FollowList_db_FavouriteTag(hcc.userinfo.getunique_id(), tag) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}

				ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(hcc.userinfo.getunique_id());
				if(user == null || user.getlastupdate() != hcc.latest_lastupdate){
					SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(hcc.userinfo.getunique_id());
				}

				Intent intent = new Intent();
				intent.setAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);
				sendbroadcast(intent);
				
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_favourite(fcobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_favourite(FavouriteCoreObj samobj){
		FavouriteCoreObj  retryobj = new FavouriteCoreObj(samobj.callback);
		
		retryobj.init(samobj.isFavourite, samobj.token, samobj.sam_pros);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_PUBLIC_FAVOURITE, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************query user fuzzy*******************************************************/
	public void query_user_fuzzy(String search_key, SMCallBack callback){
		QueryUserFuzzyCoreObj samobj = new QueryUserFuzzyCoreObj(callback);
		samobj.init(get_current_token(),  search_key);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_FUZZY, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_user_fuzzy(SamCoreObj samobj){
		QueryUserFuzzyCoreObj qufobj = (QueryUserFuzzyCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_user_fuzzy(qufobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_query_user_fuzzy(qufobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_query_user_fuzzy(QueryUserFuzzyCoreObj samobj){
		QueryUserFuzzyCoreObj  retryobj = new QueryUserFuzzyCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.search_key);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_FUZZY, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************query user precise*******************************************************/
	public void query_user_precise(TypeEnum type, String cellphone, long unique_id, String username, boolean persist, SMCallBack callback){
		LogUtil.i(TAG,"query user precise:"+type+" "+cellphone+" "+unique_id+" "+username+" "+persist   );
		QueryUserPreciseCoreObj samobj = new QueryUserPreciseCoreObj(callback);
		samobj.init(get_current_token(),  type, cellphone,  unique_id,  username, persist);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_PRECISE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_user_precise(SamCoreObj samobj){
		QueryUserPreciseCoreObj qupobj = (QueryUserPreciseCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_user_precise(qupobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				if(qupobj.persist){
					boolean isDbError = syncUpdateUserInfo(hcc.users);
					samobj.callback.onSuccess(hcc,isDbError?Constants.DB_OPT_ERROR:0);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_query_user_precise(qupobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_query_user_precise(QueryUserPreciseCoreObj samobj){
		QueryUserPreciseCoreObj  retryobj = new QueryUserPreciseCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.type, samobj.cellphone, samobj.unique_id, samobj.username, samobj.persist);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_PRECISE, retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************read profile multiple*******************************************************/
	public void query_user_multiple(List<Long> unique_id_list, boolean persist, SMCallBack callback){
		QueryUserMultipleCoreObj samobj = new QueryUserMultipleCoreObj(callback);
		samobj.init(get_current_token(), unique_id_list,persist);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_MULT, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_user_multiple(SamCoreObj samobj){
		QueryUserMultipleCoreObj qumobj = (QueryUserMultipleCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_user_multiple(qumobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				if(qumobj.persist){
					boolean isDbError = syncUpdateUserInfo(hcc.users);
					samobj.callback.onSuccess(hcc,isDbError?Constants.DB_OPT_ERROR:0);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_query_user_multiple(qumobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_query_user_multiple(QueryUserMultipleCoreObj samobj){
		QueryUserMultipleCoreObj  retryobj = new QueryUserMultipleCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.unique_id_list,samobj.persist);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_MULT,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************query user without token*******************************************************/
	public void query_user_without_token(TypeEnum type, String countrycode, String cellphone, String username, SMCallBack callback){
		QueryUserWithoutTokenCoreObj samobj = new QueryUserWithoutTokenCoreObj(callback);
		samobj.init( type,  countrycode,  cellphone,  username);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_USER_WITHOUT_TOKEN, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_user_without_token(SamCoreObj samobj){
		QueryUserWithoutTokenCoreObj quwobj = (QueryUserWithoutTokenCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_user_without_token(quwobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

/***************************************send invite msg*******************************************************/
	public void send_invite_msg(List<PhoneNumber> phones, String invite_msg, SMCallBack callback){
		SendInviteMsgCoreObj samobj = new SendInviteMsgCoreObj(callback);
		samobj.init(get_current_token(),  phones, invite_msg);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SEND_INVITE_MSG, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_send_invite_msg(SamCoreObj samobj){
		SendInviteMsgCoreObj simobj = (SendInviteMsgCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.send_invite_msg(simobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }



/***************************************edit profile*******************************************************/
//this parm ContactUser user should be a copy of current user 
	public void edit_profile(ContactUser user, SMCallBack callback){
		EditProfileCoreObj samobj = new EditProfileCoreObj(callback);
		samobj.init(get_current_token(), user);
		Message msg = mSamServiceHandler.obtainMessage(MSG_EDIT_PROFILE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_edit_profile(SamCoreObj samobj){
		EditProfileCoreObj epobj = (EditProfileCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.edit_profile(epobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_edit_profile(epobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_edit_profile(EditProfileCoreObj samobj){
		EditProfileCoreObj  retryobj = new EditProfileCoreObj(samobj.callback);
		
		retryobj.init( samobj.token, samobj.user);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_EDIT_PROFILE,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************update avatar*******************************************************/
//this parm ContactUser user should be a copy of current user 
	public void update_avatar(ContactUser user , SMCallBack callback){
		UpdateAvatarCoreObj samobj = new UpdateAvatarCoreObj(callback);
		samobj.init(get_current_token(),  user);
		Message msg = mSamServiceHandler.obtainMessage(MSG_UPDATE_AVATAR, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_update_avatar(SamCoreObj samobj){
		UpdateAvatarCoreObj uaobj = (UpdateAvatarCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.update_avatar(uaobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_update_avatar(uaobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_update_avatar(UpdateAvatarCoreObj samobj){
		UpdateAvatarCoreObj  retryobj = new UpdateAvatarCoreObj(samobj.callback);
		
		retryobj.init(samobj.token,samobj.user);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_UPDATE_AVATAR,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}
	
/***************************************query public*******************************************************/
	public void query_public(int count, String key, double latitude, double longitude, String place_id, String address, SMCallBack callback){
		QueryPublicCoreObj samobj = new QueryPublicCoreObj(callback);
		samobj.init(get_current_token(), count, key,  latitude,  longitude,  place_id,  address);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_PUBLIC, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_public(SamCoreObj samobj){
		QueryPublicCoreObj qpobj = (QueryPublicCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_public(qpobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_query_public(qpobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_query_public(QueryPublicCoreObj samobj){
		QueryPublicCoreObj  retryobj = new QueryPublicCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.count,samobj.key, samobj.latitude,samobj.longitude, samobj.place_id, samobj.address);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_PUBLIC,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************add contact*******************************************************/
	public void add_contact(int type, ContactUser user, SMCallBack callback){
		ContactCoreObj samobj = new ContactCoreObj(callback);
		samobj.init(get_current_token(), Constants.CONTACT_OPT_ADD, type,user);
		Message msg = mSamServiceHandler.obtainMessage(MSG_ADD_CONTACT, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_add_contact(SamCoreObj samobj){
		ContactCoreObj opobj = (ContactCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.add_contact(opobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				String service_category = null;
				if(opobj.user.getusertype() == Constants.SAM_PROS){
					service_category = (opobj.user).getservice_category();
				}
				Contact user = new Contact(opobj.user.getunique_id(),opobj.user.getusername(),
																	opobj.user.getavatar(),service_category);
				
				if(opobj.type == Constants.ADD_INTO_CONTACT){
					dao.update_ContactList_db(user, false);
					ContactDataCache.getInstance().addContact(user.getunique_id(), user);
				}else{
					dao.update_ContactList_db(user, true);
					CustomerDataCache.getInstance().addCustomer(user.getunique_id(), user);
				}

				
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}

				Intent intent = new Intent();
				if(opobj.type == Constants.ADD_INTO_CONTACT){
					intent.setAction(Constants.BROADCAST_CONTACTLIST_UPDATE);
				}else{
					intent.setAction(Constants.BROADCAST_CUSTOMERLIST_UPDATE);
				}
				sendbroadcast(intent);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_add_contact(opobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_add_contact(ContactCoreObj samobj){
		ContactCoreObj  retryobj = new ContactCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.opt, samobj.type,  samobj.user);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_ADD_CONTACT,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************remove contact*******************************************************/
	public void remove_contact(int type, ContactUser user, SMCallBack callback){
		ContactCoreObj samobj = new ContactCoreObj(callback);
		samobj.init(get_current_token(), Constants.CONTACT_OPT_REMOVE, type, user);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REMOVE_CONTACT, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_remove_contact(SamCoreObj samobj){
		ContactCoreObj opobj = (ContactCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.remove_contact(opobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				if(opobj.type == Constants.REMOVE_OUT_CONTACT){
					dao.delete_ContactList_db_by_unique_id(opobj.user.getunique_id(), false);
					ContactDataCache.getInstance().removeContact(opobj.user.getunique_id());
				}else{
					dao.delete_ContactList_db_by_unique_id(opobj.user.getunique_id(), true);
					CustomerDataCache.getInstance().removeCustomer(opobj.user.getunique_id());
				}
				samobj.callback.onSuccess(hcc,0);

				Intent intent = new Intent();
				if(opobj.type == Constants.REMOVE_OUT_CONTACT){
					intent.setAction(Constants.BROADCAST_CONTACTLIST_UPDATE);
				}else{
					intent.setAction(Constants.BROADCAST_CUSTOMERLIST_UPDATE);
				}
				sendbroadcast(intent);

				
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_remove_contact(opobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_remove_contact(ContactCoreObj samobj){
		ContactCoreObj  retryobj = new ContactCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.opt, samobj.type,  samobj.user);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_REMOVE_CONTACT,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************sync contact list*******************************************************/
	public void sync_contact_list(boolean isCustomer, SMCallBack callback){
		if(isCustomer){
			syncStatus[SYNC_CUSTOMER_LIST] = SYNCING;
		}else{
			syncStatus[SYNC_CONTACT_LIST] = SYNCING;
		}
		
		SyncContactListCoreObj samobj = new SyncContactListCoreObj(callback);
		samobj.init(isCustomer,get_current_token());
		Message msg = mSamServiceHandler.obtainMessage(MSG_SYNC_CONTACT_LIST, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private boolean syncUpdateContactList(boolean isCustomer,MultipleContact contacts){
		boolean isDbError = false;
		dao.delete_ContactList_db_all(isCustomer);
		if(isCustomer){
			CustomerDataCache.getInstance().clearCache();
		}else{
			ContactDataCache.getInstance().clearCache();
		}
		
		for(Contact contact:contacts.getcontacts()){
			if(isCustomer){
				CustomerDataCache.getInstance().addCustomer(contact.getunique_id(), contact);
			}else{
				ContactDataCache.getInstance().addContact(contact.getunique_id(), contact);
			}
			
			if(dao.add_ContactList_db(contact, isCustomer) == -1){
				isDbError = true;
			}
		}
		return isDbError = true;
	}

	private void do_sync_contact_list(SamCoreObj samobj){
		SyncContactListCoreObj scobj = (SyncContactListCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.sync_contact_list(scobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				
				boolean isDbError = syncUpdateContactList(scobj.isCustomer,hcc.contacts);
				samobj.callback.onSuccess(hcc,isDbError?Constants.DB_OPT_ERROR:0);
				Intent intent = new Intent();
				if(!scobj.isCustomer){
					intent.setAction(Constants.BROADCAST_CONTACTLIST_UPDATE);
				}else{
					intent.setAction(Constants.BROADCAST_CUSTOMERLIST_UPDATE);
				}
				sendbroadcast(intent);
				
				for(Contact contact:hcc.contacts.getcontacts()){
					ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(contact.getunique_id());
					if(user == null || user.getlastupdate() != contact.getlastupdate()){
						SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(contact.getunique_id());
					}
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_sync_contact_list(SyncContactListCoreObj samobj){
		SyncContactListCoreObj  retryobj = new SyncContactListCoreObj(samobj.callback);
		
		retryobj.init(samobj.isCustomer,samobj.token);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SYNC_CONTACT_LIST,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}


/***************************************sync follow list*******************************************************/
	public void sync_follow_list(SMCallBack callback){
		syncStatus[SYNC_FOLLOW_LIST] = SYNCING;
		SyncFollowListCoreObj samobj = new SyncFollowListCoreObj(callback);
		samobj.init(get_current_token());
		Message msg = mSamServiceHandler.obtainMessage(MSG_SYNC_FOLLOW_LIST, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_sync_follow_list(SamCoreObj samobj){
		SyncFollowListCoreObj sflobj = (SyncFollowListCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.sync_follow_list(sflobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				boolean isDbError = syncUpdateFollowList(hcc.followusers);
				samobj.callback.onSuccess(hcc,isDbError?Constants.DB_OPT_ERROR:0);
				for(FollowedSamPros sp:hcc.followusers.getsps()){
					ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(sp.getunique_id());
					if(user == null || user.getlastupdate() != sp.getlastupdate()){
						SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(sp.getunique_id());
					}
				}

				Intent intent = new Intent();
				intent.setAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);
				sendbroadcast(intent);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_sync_follow_list(SyncFollowListCoreObj samobj){
		SyncFollowListCoreObj  retryobj = new SyncFollowListCoreObj(samobj.callback);
		
		retryobj.init( samobj.token);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SYNC_FOLLOW_LIST,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}

/***************************************write advertisement*******************************************************/
	public void write_advertisement(int type, String content, String content_thumb, long sender_unique_id, String message_id, SMCallBack callback){
		AdvCoreObj samobj = new AdvCoreObj(callback);
		samobj.init(get_current_token(),type,content, content_thumb, sender_unique_id,message_id);
		Message msg = mSamServiceHandler.obtainMessage(MSG_WRITE_ADV, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_write_advertisement(SamCoreObj samobj){
		AdvCoreObj advobj = (AdvCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.write_advertisement(advobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				if(dao.update_SamProsAdv_db(hcc.adv) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
			if(0<samobj.retry_count--){
				retry_write_advertisement(advobj);
			}else{
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
			}
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

	private void retry_write_advertisement(AdvCoreObj samobj){
		AdvCoreObj  retryobj = new AdvCoreObj(samobj.callback);
		
		retryobj.init(samobj.token, samobj.type,samobj.content,samobj.content_thumb, samobj.sender_unique_id,samobj.message_id);
		
		retryobj.setRetryCount(samobj.retry_count);
		Message msg = mSamServiceHandler.obtainMessage(MSG_WRITE_ADV,retryobj);
		mSamServiceHandler.sendMessageDelayed(msg, SAMSERVICE_RETRY_WAIT);
		startTimeOutRetry(retryobj);
	}	

/***************************************delete advertisement*******************************************************/
	public void delete_advertisement(List<Long> advs, SMCallBack callback){
		DeleteAdvCoreObj samobj = new DeleteAdvCoreObj(callback);
		samobj.init(get_current_token(),advs);
		Message msg = mSamServiceHandler.obtainMessage(MSG_DELETE_ADV, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_delete_advertisement(SamCoreObj samobj){
		DeleteAdvCoreObj advobj = (DeleteAdvCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		for(long adv_id:advobj.advs){
			dao.delete_SamProsAdv_db_by_adv_id(adv_id);
		}

		samobj.callback.onSuccess(null,0);

		boolean http_ret = hcc.delete_advertisement(advobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

/***************************************downalod advertisement thum*******************************************************/
	public void download(String url, String path, SMCallBack callback){
		DownloadCoreObj samobj = new DownloadCoreObj(callback);
		samobj.init(url,path);
		Message msg = mSamServiceHandler.obtainMessage(MSG_DOWNLOAD, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_download(SamCoreObj samobj){
		DownloadCoreObj dobj = (DownloadCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.download(dobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			samobj.callback.onSuccess(hcc,0);
		}else{
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}
    }

/********************************************** Bind Getu Alias  ********************************************************/
	public void bind_alias(SMCallBack callback){
		syncStatus[SYNC_BIND]=SYNCING;
		BindCoreObj samobj = new BindCoreObj(callback);
		samobj.init(get_current_token());
		Message msg = mSamServiceHandler.obtainMessage(MSG_BIND_ALIAS,samobj);
		mSamServiceHandler.sendMessage(msg);
	}

	private void do_bind_alias(SamCoreObj samobj){
		String account = Preferences.getUserAccount();
		String alias = Preferences.getUserAlias();
		boolean bindSucceed = false;
		
		if(alias == null || !alias.equals(StringUtil.makeMd5(account))){
			bindSucceed = PushManager.getInstance().bindAlias(DemoCache.getContext(),StringUtil.makeMd5(account));
		}else{
			SamLog.e(TAG, "do not bind alias this time");
			samobj.callback.onSuccess(null,0);
			return;
		}

		if(bindSucceed){
			Preferences.saveUserAlias(StringUtil.makeMd5(account));
			SamLog.e(TAG, "bind alias succeed");
			samobj.callback.onSuccess(null,0);
		}else{
			samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}
    }


/********************************************** Send Client ID  ********************************************************/
	public void send_clientid(SMCallBack callback){
		syncStatus[SYNC_SEND_CLIENT_ID]=SYNCING;
		SendClientIDCoreObj samobj = new SendClientIDCoreObj(callback);
		samobj.init(get_current_token(),clientID);
		Message msg = mSamServiceHandler.obtainMessage(MSG_SEND_CLIENTID, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_send_clientid(SamCoreObj samobj){
		SendClientIDCoreObj bdobj = (SendClientIDCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.send_clientid(bdobj);

		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
    }

/********************************************** Query State  ********************************************************/
	public void query_state(SMCallBack callback){
		syncStatus[SYNC_QUERY_STATE] = SYNCING;
		QueryStateCoreObj samobj = new QueryStateCoreObj(callback);
		samobj.init(get_current_token());
		Message msg = mSamServiceHandler.obtainMessage(MSG_QUERY_STATE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_query_state(SamCoreObj samobj){
		QueryStateCoreObj qsobj = (QueryStateCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.query_state(qsobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
		
    }

/********************************************** Edit Cellphone code request  ********************************************************/
	public void edit_cellphone_code_request(String countrycode,String cellphone,SMCallBack callback){
		EditCellphoneVerifyCodeCoreObj samobj = new EditCellphoneVerifyCodeCoreObj(callback);
		samobj.init(get_current_token(),countrycode,cellphone);
		Message msg = mSamServiceHandler.obtainMessage(MSG_EDITCELLPHONE_CODE_REQUEST, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_edit_cellphone_code_request(SamCoreObj samobj){
		EditCellphoneVerifyCodeCoreObj vcobj = (EditCellphoneVerifyCodeCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.edit_cellphone_code_request(vcobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				samobj.callback.onSuccess(hcc,0);
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
		
    }

/********************************************** Edit Cellphone update  ********************************************************/
	public void edit_cellphone(ContactUser user,String verifycode,SMCallBack callback){
		EditCellphoneCoreObj samobj = new EditCellphoneCoreObj(callback);
		samobj.init(get_current_token(),user,verifycode);
		Message msg = mSamServiceHandler.obtainMessage(MSG_EDITCELLPHONE_UPDATE, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_edit_cellphone(SamCoreObj samobj){
		EditCellphoneCoreObj ecobj = (EditCellphoneCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.edit_cellphone(ecobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
		
    }

/********************************************** update question notify only for sp  ********************************************************/
	public void update_question_notify(ContactUser user,SMCallBack callback){
		UpdateQuestionNotifyCoreObj samobj = new UpdateQuestionNotifyCoreObj(callback);
		samobj.init(get_current_token(),user);
		Message msg = mSamServiceHandler.obtainMessage(MSG_UPDATE_QUESTION_NOTIFY, samobj);
		mSamServiceHandler.sendMessage(msg);
		startTimeOut(samobj);
	}

	private void do_update_question_notify(SamCoreObj samobj){
		UpdateQuestionNotifyCoreObj ecobj = (UpdateQuestionNotifyCoreObj)samobj;
		HttpCommClient hcc = new HttpCommClient();

		boolean http_ret = hcc.update_question_notify(ecobj);
		if(isTimeOut(samobj)){
			return;
		}else if(http_ret){
			if(hcc.ret == 0){
				set_current_user(hcc.userinfo);
				SamchatUserInfoCache.getInstance().addUser(hcc.userinfo.getunique_id(), hcc.userinfo);
				if(dao.update_ContactUser_db(hcc.userinfo) == -1){
					samobj.callback.onSuccess(hcc,Constants.DB_OPT_ERROR);
				}else{
					samobj.callback.onSuccess(hcc,0);
				}
			}else{
				samobj.callback.onFailed(hcc.ret);
			}
		}else if(!hcc.exception){
				samobj.callback.onError(Constants.CONNECTION_HTTP_ERROR);
		}else{
			samobj.callback.onError(Constants.EXCEPTION_ERROR);
		}
		
    }

/********************************************** HTTP PUSH  ********************************************************/
	public void handlePushCmd(Context context, String jsonString){
		HttpCommClient hcc = new HttpCommClient();
		int category = hcc.parsePushJson(jsonString);
		boolean isDBError = false;

		switch(category){
			case Constants.PUSH_CATEGORY_QUESTION:
				SamDBManager.getInstance().handleReceivedQuestion(context,hcc);
			break;

			case Constants.PUSH_CATEGORY_ADV:
				SamDBManager.getInstance().handleReceivedAdvertisement(hcc);
			break;

			default:

			return;
		}

	} 

/********************************************** SYNC OPT ********************************************************/
	public void client_id_ready(String clid){
		LogUtil.i(TAG,"clientid:"+clid);
		clientID = clid;
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_CLIENTID_READY);
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	public void network_available(){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_NETWORK_AVAILABLE);
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void sync_status_check(){
		mHandlerTimeOutHandler.removeMessages(MSG_SYNC_CHECK);
		if(isSyncComplete() || !NetworkMonitor.isNetworkAvailable()){
			return;
		}
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_CHECK);
		mHandlerTimeOutHandler.sendMessageDelayed(msg,SAMSERVICE_STATUS_CHECK_TIMOUT);
	}

	private void set_query_state(StateDateInfo sdinfo,boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_QUERY_STATE_UPDATE, new SyncCoreObj(succeed,sdinfo));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void set_sync_follow_list(boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_FOLLOW_LIST_UPDATE, new SyncCoreObj(succeed));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void set_sync_contact_list(boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_CONTACT_LIST_UPDATE,new SyncCoreObj(succeed));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void set_sync_customer_list(boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_CUSTOMER_LIST_UPDATE,new SyncCoreObj(succeed));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void set_sync_bind_alias(boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_BIND_ALIAS_UPDATE,new SyncCoreObj(succeed));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	private void set_sync_send_clientID(boolean succeed){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_SEND_CLIENT_ID_UPDATE,new SyncCoreObj(succeed));
		mHandlerTimeOutHandler.sendMessage(msg);		
	}

	public void startSync(){
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_SYNC_START);
		mHandlerTimeOutHandler.sendMessage(msg);
	}

	private boolean isSyncComplete(){
		for(int status: syncStatus){
			if(status != SYNCED){
				return false;
			}
		}
		return true;
	}

	private boolean needSyncFollowList(StateDateInfo sdinfo){
		if(sdinfo.getfollow_list_date() == 0){
			return false;
		}
		
		String date = Preferences.getFldate();
		if(date == null || !date.equals(""+sdinfo.getfollow_list_date())){
			return true;
		}

		return false;
	}

	private boolean needSyncContactList(StateDateInfo sdinfo){
		if(sdinfo.getcontact_list_date() == 0){
			return false;
		}
		
		String date = Preferences.getCcdate();
		if(date == null || !date.equals(""+sdinfo.getcontact_list_date())){
			return true;
		}

		return false;
	}

	private boolean needSyncCustomerList(StateDateInfo sdinfo){
		if(sdinfo.getcustomer_list_date() == 0 || get_current_user().getusertype() == Constants.USER){
			return false;
		}
		
		String date = Preferences.getCudate();
		if(date == null || !date.equals(""+sdinfo.getcustomer_list_date())){
			return true;
		}

		return false;
	}

	SMCallBack syncQueryStateCallback = new SMCallBack(){
		@Override
		public void onSuccess( Object obj,  int WarningCode) {
			HttpCommClient hcc = (HttpCommClient)obj;
			set_query_state(hcc.sdinfo,true);
			LogUtil.i(TAG,"sync Query state succeed");
		}
		@Override
		public void onFailed(int code) {
			set_query_state(null,false);
			LogUtil.i(TAG,"sync Query state failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_query_state(null,false);
			LogUtil.i(TAG,"sync Query state error:"+code);
		}
	};

	SMCallBack syncFollowListCallback = new SMCallBack(){
		@Override
		public void onSuccess(Object obj, int WarningCode) {
			set_sync_follow_list(true);
			LogUtil.i(TAG,"sync follow list succeed");
		}
		@Override
		public void onFailed(int code) {
			set_sync_follow_list(false);
			LogUtil.i(TAG,"sync follow list failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_sync_follow_list(false);
			LogUtil.i(TAG,"sync follow list error:"+code);
		}
	};

	SMCallBack syncContactListCallback = new SMCallBack(){
		@Override
		public void onSuccess(final Object obj, final int WarningCode) {
			set_sync_contact_list(true);
			LogUtil.i(TAG,"sync contact list succeed");
		}
		@Override
		public void onFailed(int code) {
			set_sync_contact_list(false);
			LogUtil.i(TAG,"sync contact list failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_sync_contact_list(false);
			LogUtil.i(TAG,"sync contact list error:"+code);
		}
	};

	
	SMCallBack syncCustomerListCallback = new SMCallBack(){
		@Override
		public void onSuccess(final Object obj, final int WarningCode) {
			set_sync_customer_list(true);
			LogUtil.i(TAG,"sync customer list succeed");
		}
		@Override
		public void onFailed(int code) {
			set_sync_customer_list(false);
			LogUtil.i(TAG,"sync customer list failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_sync_customer_list(false);
			LogUtil.i(TAG,"sync customer list error:"+code);
		}
	};

	SMCallBack syncBindAliasCallback = new SMCallBack(){
		@Override
		public void onSuccess(final Object obj, final int WarningCode) {
			set_sync_bind_alias(true);
			LogUtil.i(TAG,"sync bind alias succeed");
		}
		@Override
		public void onFailed(int code) {
			set_sync_bind_alias(false);
			LogUtil.i(TAG,"sync bind alias failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_sync_bind_alias(false);
			LogUtil.i(TAG,"sync bind alias error:"+code);
		}
	};

	SMCallBack syncSendClientIDCallback = new SMCallBack(){
		@Override
		public void onSuccess(final Object obj, final int WarningCode) {
			set_sync_send_clientID(true);
			LogUtil.i(TAG,"sync send client id succeed");
		}
		@Override
		public void onFailed(int code) {
			set_sync_send_clientID(false);
			LogUtil.i(TAG,"sync send client id failed:"+code);
		}
		@Override
		public void onError(int code) {
			set_sync_send_clientID(false);
			LogUtil.i(TAG,"sync send client id error:"+code);
		}
	};

/***************************************clear data cache in sdcard*******************************************************/
	public void clear_cache(SMCallBack callback){
		ClearCacheCoreObj samobj = new ClearCacheCoreObj(callback);
		samobj.init();
		Message msg = mSamServiceHandler.obtainMessage(MSG_CLEAR_CACHE, samobj);
		mSamServiceHandler.sendMessage(msg);
	}

	private void do_clear_cache(SamCoreObj samobj){
		//clear image cache
		File cacheDir = StorageUtils.getOwnCacheDirectory(DemoCache.getContext(), DemoCache.getContext().getPackageName() + "/cache/image/");
		String imgCachePath = cacheDir.getAbsolutePath();
		AttachmentStore.deleteFileUnderDir(imgCachePath);
		
		samobj.callback.onSuccess(null,0);
    }
	

/********************************************** HTTP API END ********************************************************/

/********************************************** DB API  ********************************************************/
		



/********************************************** DB END  ********************************************************/
	
	public void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	public static synchronized SamService getInstance(){
		if(mSamService == null){
			mContext = DemoCache.getContext();
			mSamService	= new SamService();
		}
		return mSamService;
	}


	synchronized public void set_current_user(ContactUser user){
		current_user = user;
	}

	public ContactUser get_current_user(){
		return current_user;
	}

	synchronized public void store_current_token(String token){
			current_token = token;
	}

	synchronized public String get_current_token(){
		return current_token;
	}
	
	static public boolean isNumeric(String str){    
		Pattern pattern = Pattern.compile("[0-9]*");    
		Matcher isNum = pattern.matcher(str);
  
		if( !isNum.matches() ){   
			return false;    
		}else{
			return true;  
		}

	}
    
	private void initSamService(){
		current_token = null;
		current_user = null;
		sam_cache_path = Environment.getExternalStorageDirectory() + "/" + mContext.getPackageName() + "/nim/samchat/cache";
		sam_download_path = Environment.getExternalStorageDirectory() + "/" + mContext.getPackageName() + "/nim/samchat/download";
    	
		SamLog.e(TAG,"sam_cache_path:"+sam_cache_path);
		SamLog.e(TAG,"sam_download_path:"+sam_download_path);

		File file1 = new File(sam_cache_path);
		if(!file1.exists()){
			file1.mkdirs();
		}

		File file2 = new File(sam_download_path);
		if(!file2.exists()){
			file2.mkdirs();
		}

		clientID = null;
		
	}

	
	public static synchronized boolean isSamServiceExisted(){
		return mSamService != null ? true:false;
	}

	public synchronized SamDBDao getDao(){
		return dao;
	}

	public synchronized void initDao(String dbFolder){
	   if(dao == null){
		 	this.dbFolder = dbFolder;
			dao = new SamDBDao(mContext,dbFolder);
			//TestCase.testInitDB();
		}
	}

	private SamService(){
		initSamService();
		InitHandlerThread();
	}
    
	private void InitHandlerThread(){
		mHandlerThread = new HandlerThread("SamService");
		mHandlerThread.start();
		mSamServiceHandler = new SamServiceHandler(mHandlerThread.getLooper());

		mHandlerTimeOutThread = new HandlerThread("SamServiceTimeOut");
		mHandlerTimeOutThread.start();
		mHandlerTimeOutHandler = new SamServiceTimeOutHandler(mHandlerTimeOutThread.getLooper());
		
		mFixedHttpThreadPool = Executors.newFixedThreadPool(5);

		
	}
	
	public void stopSamService(){
		/*remove all msg in mSamServiceHandler*/		
		mSamServiceHandler.removeCallbacksAndMessages(null);
		mHandlerThread.getLooper().quit();

		mHandlerTimeOutHandler.removeCallbacksAndMessages(null);
		mHandlerTimeOutThread.getLooper().quit();

		if(mFixedHttpThreadPool!=null){
			if(!mFixedHttpThreadPool.isShutdown()){
				mFixedHttpThreadPool.shutdown();
			}
			mFixedHttpThreadPool = null;
		}

		if(dao!=null){ 
			dao.close();
			dao = null;
		}

		mSamService = null;
		current_token = null;
		current_user = null;
		clientID = null;
	}

	public void setClientID(String clientID){
		this.clientID = clientID;
	}

	private void asyncUpdateUserInfo(ContactUser now){
		query_user_precise(TypeEnum.UNIQUE_ID, null, now.getunique_id(), null, true, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {}

				@Override
				public void onFailed(int code) {}

				@Override
				public void onError(int code) {}

			});
	}

	private boolean syncUpdateUserInfoIfExisted(MultipleUserProfile users){
		boolean isDbError = false;
		
		if(users.getcount()>0){
			for(ContactUser user: users.getusers()){
				SamchatUserInfoCache.getInstance().addUser(user.getunique_id(), user);
				if(dao.update_ContactUser_db_if_existed(user) == -1){
					isDbError = true;
				}
			}
		}

		return isDbError;
	}

	private boolean syncUpdateUserInfo(MultipleUserProfile users){
		boolean isDbError = false;
		
		if(users.getcount()>0){
			for(ContactUser user: users.getusers()){
				SamchatUserInfoCache.getInstance().addUser(user.getunique_id(), user);
				if(dao.update_ContactUser_db(user) == -1){
					isDbError = true;
				}
			}
		}

		return isDbError;
	}

	private boolean syncUpdateUserInfo(ContactUser user){
		boolean isDbError = false;
		SamchatUserInfoCache.getInstance().addUser(user.getunique_id(), user);
		 if(dao.update_ContactUser_db(user) == -1){
			isDbError = true;
		}
		 
		return isDbError;
	}

	private boolean syncUpdateFollowList(MultipleFollowUser followusers){
		boolean isDbError = false;
		dao.delete_FollowList_db_all();
		FollowDataCache.getInstance().clearCache();
		for(FollowedSamPros sp:followusers.getsps()){
			FollowDataCache.getInstance().addFollowSP(sp.getunique_id(), sp);
			if(dao.add_FollowList_db(sp) == -1){
				isDbError = true;
			}
		}
		return isDbError;
	}

	private void cancelTimeOut(SamCoreObj samobj) {
		mHandlerTimeOutHandler.removeMessages(MSG_HANDLE_TIMEOUT,samobj);
	}

	private void startTimeOut(SamCoreObj samobj) {
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_HANDLE_TIMEOUT,samobj);
		mHandlerTimeOutHandler.sendMessageDelayed(msg, SAMSERVICE_HANDLE_TIMEOUT);
	}

	private void startTimeOutRetry(SamCoreObj samobj) {
		Message msg = mHandlerTimeOutHandler.obtainMessage(MSG_HANDLE_TIMEOUT,samobj);
		mHandlerTimeOutHandler.sendMessageDelayed(msg, SAMSERVICE_HANDLE_TIMEOUT+SAMSERVICE_RETRY_WAIT);
	}

	

	private final class SamServiceTimeOutHandler extends Handler{
		public SamServiceTimeOutHandler(Looper looper)
		{
		   super(looper);
		}

		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_HANDLE_TIMEOUT:
					SamCoreObj samobj = (SamCoreObj)msg.obj;
					boolean continue_run = true;
					synchronized(samobj){
						if(samobj.request_status == SamCoreObj.STATUS_INIT){
							samobj.request_status = SamCoreObj.STATUS_TIMEOUT;
						}else if(samobj.request_status == SamCoreObj.STATUS_DONE){
							continue_run = false;
						}
					}

					if(continue_run){
						samobj.callback.onError(Constants.CONNECTION_TIMEOUT_ERROR);
					}
					
				break;

				case MSG_SYNC_START:
					startSync = true;
					sync_status_check();
					query_state(syncQueryStateCallback);
				break;

				case MSG_CLIENTID_READY:
					clientidReady = true;
					
					if(syncStatus[SYNC_CUSTOMER_LIST] == SYNCED
						&& syncStatus[SYNC_CONTACT_LIST] == SYNCED
						&& syncStatus[SYNC_FOLLOW_LIST] == SYNCED
						&& syncStatus[SYNC_BIND] == SYNCED
						&& syncStatus[SYNC_SEND_CLIENT_ID] == INITED){
						send_clientid(syncSendClientIDCallback);
					}
				break;

				case MSG_NETWORK_AVAILABLE:
				case MSG_SYNC_CHECK:
					if(!startSync || isSyncComplete() ||!NetworkMonitor.isNetworkAvailable())
						return;

					if(syncStatus[SYNC_QUERY_STATE] == INITED){
						sync_status_check();
						query_state(syncQueryStateCallback);
						return;
					}else if(syncStatus[SYNC_QUERY_STATE] == SYNCING){
						return;
					}

					if(syncStatus[SYNC_FOLLOW_LIST] == INITED){
						sync_status_check();
						sync_follow_list(syncFollowListCallback);
					}

					if(syncStatus[SYNC_CONTACT_LIST] == INITED){
						sync_status_check();
						sync_contact_list(false,syncContactListCallback);
					}

					if(syncStatus[SYNC_CUSTOMER_LIST] == INITED){
						sync_status_check();
						sync_contact_list(true,syncCustomerListCallback);
					}

					if(syncStatus[SYNC_CUSTOMER_LIST] == SYNCED
						&& syncStatus[SYNC_CONTACT_LIST] == SYNCED
						&& syncStatus[SYNC_FOLLOW_LIST] == SYNCED
						&& syncStatus[SYNC_BIND] == INITED){
						sync_status_check();
						bind_alias(syncBindAliasCallback);
					}

					if(syncStatus[SYNC_CUSTOMER_LIST] == SYNCED
						&& syncStatus[SYNC_CONTACT_LIST] == SYNCED
						&& syncStatus[SYNC_FOLLOW_LIST] == SYNCED
						&& syncStatus[SYNC_BIND] == SYNCED
						&& syncStatus[SYNC_SEND_CLIENT_ID] == INITED
						&& clientidReady){
						sync_status_check();
						send_clientid(syncSendClientIDCallback);
					}

				break;

				case MSG_SYNC_QUERY_STATE_UPDATE:
					SyncCoreObj stateObj = (SyncCoreObj)msg.obj;
					if(stateObj.succeed){
						syncStatus[SYNC_QUERY_STATE]=SYNCED;
						sdinfo = stateObj.sdinfo;
						if(needSyncFollowList(sdinfo)){
							sync_status_check();
							sync_follow_list(syncFollowListCallback);
						}else{
							syncStatus[SYNC_FOLLOW_LIST]=SYNCED;
							Preferences.saveFldate(""+sdinfo.getfollow_list_date());
						}

						if(needSyncContactList(sdinfo)){
							sync_status_check();
							sync_contact_list(false, syncContactListCallback);
						}else{
							syncStatus[SYNC_CONTACT_LIST]=SYNCED;
							Preferences.saveCcdate(""+sdinfo.getcontact_list_date());
						}

						if(needSyncCustomerList(sdinfo)){
							sync_status_check();
							sync_contact_list(true, syncCustomerListCallback);
						}else{
							syncStatus[SYNC_CUSTOMER_LIST]=SYNCED;
							Preferences.saveCudate(""+sdinfo.getcustomer_list_date());
						}
						
						if(syncStatus[SYNC_FOLLOW_LIST]==SYNCED && syncStatus[SYNC_CONTACT_LIST]==SYNCED 
							&& syncStatus[SYNC_CUSTOMER_LIST]==SYNCED && syncStatus[SYNC_BIND] == INITED){
							sync_status_check();
							bind_alias(syncBindAliasCallback);
						}
					}else{
						syncStatus[SYNC_QUERY_STATE]=INITED;
						sync_status_check();
					}
				break;

				case MSG_SYNC_FOLLOW_LIST_UPDATE:
					SyncCoreObj followObj = (SyncCoreObj)msg.obj;
					if(followObj.succeed){
						syncStatus[SYNC_FOLLOW_LIST]=SYNCED;
						Preferences.saveFldate(""+sdinfo.getfollow_list_date());
						if(syncStatus[SYNC_FOLLOW_LIST]==SYNCED && syncStatus[SYNC_CONTACT_LIST]==SYNCED 
							&& syncStatus[SYNC_CUSTOMER_LIST]==SYNCED && syncStatus[SYNC_BIND] == INITED){
							sync_status_check();
							bind_alias(syncBindAliasCallback);
						}
					}else{
						syncStatus[SYNC_FOLLOW_LIST]=INITED;
						sync_status_check();
					}
				break;

				case MSG_SYNC_CONTACT_LIST_UPDATE:
					SyncCoreObj contactObj = (SyncCoreObj)msg.obj;
					if(contactObj.succeed){
						syncStatus[SYNC_CONTACT_LIST]=SYNCED;
						Preferences.saveCcdate(""+sdinfo.getcontact_list_date());
						if(syncStatus[SYNC_FOLLOW_LIST]==SYNCED && syncStatus[SYNC_CONTACT_LIST]==SYNCED 
							&& syncStatus[SYNC_CUSTOMER_LIST]==SYNCED && syncStatus[SYNC_BIND] == INITED){
							sync_status_check();
							bind_alias(syncBindAliasCallback);
						}
					}else{
						syncStatus[SYNC_CONTACT_LIST]=INITED;
						sync_status_check();
					}
				break;
				
				case MSG_SYNC_CUSTOMER_LIST_UPDATE:
					SyncCoreObj customerObj = (SyncCoreObj)msg.obj;
					if(customerObj.succeed){
						syncStatus[SYNC_CUSTOMER_LIST]=SYNCED;
						Preferences.saveCudate(""+sdinfo.getcustomer_list_date());
						if(syncStatus[SYNC_FOLLOW_LIST]==SYNCED && syncStatus[SYNC_CONTACT_LIST]==SYNCED 
							&& syncStatus[SYNC_CUSTOMER_LIST]==SYNCED && syncStatus[SYNC_BIND] == INITED){
							sync_status_check();
							bind_alias(syncBindAliasCallback);
						}
					}else{
						syncStatus[SYNC_CUSTOMER_LIST]=INITED;
						sync_status_check();
					}
				break;
				
				case MSG_SYNC_BIND_ALIAS_UPDATE:
					SyncCoreObj bindObj = (SyncCoreObj)msg.obj;
					if(bindObj.succeed){
						syncStatus[SYNC_BIND]=SYNCED;
						if(syncStatus[SYNC_SEND_CLIENT_ID]==INITED && clientidReady){
							sync_status_check();
							send_clientid(syncSendClientIDCallback);
						}
					}else{
						syncStatus[SYNC_BIND]=INITED;
						sync_status_check();
					}

				break;

				case MSG_SYNC_SEND_CLIENT_ID_UPDATE:
					SyncCoreObj sendObj = (SyncCoreObj)msg.obj;
					if(!sendObj.succeed){
						syncStatus[SYNC_SEND_CLIENT_ID]=INITED;
						sync_status_check();
					}else{
						syncStatus[SYNC_SEND_CLIENT_ID]=SYNCED;
					}
				break;
			}
		}
	}
	
	private final class SamServiceHandler extends Handler{
		public SamServiceHandler(Looper looper)
		{
			super(looper);
		}
    	
		@Override
		public void handleMessage(Message msg){
			final SamCoreObj msgObj = (SamCoreObj)msg.obj;

			switch(msg.what){
				case MSG_REGISTER_CODE_REQEUST:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_register_code_request(msgObj);
						}
					});
					break;
					
				case MSG_REGISTER_CODE_VERIFY:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_register_code_verify(msgObj);
						}
					});
					break;

				case MSG_SIGN_UP:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_sign_up(msgObj);
						}
					});
					break;

				case MSG_SIGN_IN:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_sign_in(msgObj);
						}
					});
					break;

				case MSG_GET_PUSH_APP_KEY:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_get_app_key(msgObj);
						}
					});
					break;

				case MSG_SIGN_OUT:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_sign_out(msgObj);
						}
					});
					break;

				case MSG_CREATE_SAM_PROS:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_create_sam_pros(msgObj);
						}
					});
					break;
	
				case MSG_FINDPWD_CODE_REQUEST:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_findpwd_code_request(msgObj);
						}
					});
					break;

				case MSG_FINDPWD_CODE_VERIFY:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_findpwd_code_verify(msgObj);
						}
					});
					break;

				case MSG_FINDPWD_UPDATE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_findpwd_update(msgObj);
						}
					});
					break;

				case MSG_UPDATE_PASSWORD:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_update_password(msgObj);
						}
					});
					break;

				case MSG_SEND_QUESTION:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_send_question(msgObj);
						}
					});
					break;

				case MSG_GET_PLACES_INFO:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_get_places_info(msgObj);
						}
					});
					break;

				case MSG_PUBLIC_FOLLOW:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_follow(msgObj);
						}
					});
					break;

				case MSG_PUBLIC_BLOCK:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_block(msgObj);
						}
					});
					break;

				case MSG_PUBLIC_FAVOURITE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_favourite(msgObj);
						}
					});
					break;

				case MSG_QUERY_USER_FUZZY:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_user_fuzzy(msgObj);
						}
					});
					break;

				case MSG_QUERY_USER_PRECISE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_user_precise(msgObj);
						}
					});
					break;

				case MSG_QUERY_USER_MULT:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_user_multiple(msgObj);
						}
					});
					break;

				case MSG_QUERY_USER_WITHOUT_TOKEN:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_user_without_token(msgObj);
						}
					});
					break;

				case MSG_SEND_INVITE_MSG:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_send_invite_msg(msgObj);
						}
					});
					break;

				case MSG_EDIT_PROFILE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_edit_profile(msgObj);
						}
					});
					break;
				case MSG_UPDATE_AVATAR:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_update_avatar(msgObj);
						}
					});
					break;

				case MSG_QUERY_PUBLIC:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_public(msgObj);
						}
					});
					break;
					
				case MSG_ADD_CONTACT:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_add_contact(msgObj);
						}
					});
					break;

				case MSG_REMOVE_CONTACT:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_remove_contact(msgObj);
						}
					});
					break;

				case MSG_SYNC_CONTACT_LIST:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_sync_contact_list(msgObj);
						}
					});
					break;

				case MSG_SYNC_FOLLOW_LIST:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_sync_follow_list(msgObj);
						}
					});
					break;

				case MSG_WRITE_ADV:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_write_advertisement(msgObj);
						}
					});
					break;

				case MSG_DELETE_ADV:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_delete_advertisement(msgObj);
						}
					});
					break;

				case MSG_DOWNLOAD:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_download(msgObj);
						}
					});
					break;

				case MSG_QUERY_STATE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_query_state(msgObj);
						}
					});
					break;

				case MSG_BIND_ALIAS:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_bind_alias(msgObj);
						}
					});
					break;
					
				case MSG_SEND_CLIENTID:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_send_clientid(msgObj);
						}
					});
					break;

				case MSG_CLEAR_CACHE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_clear_cache(msgObj);
						}
					});
					break;

				case MSG_EDITCELLPHONE_CODE_REQUEST:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_edit_cellphone_code_request(msgObj);
						}
					});
					break;

				case MSG_EDITCELLPHONE_UPDATE:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_edit_cellphone(msgObj);
						}
					});
					break;

				case MSG_UPDATE_QUESTION_NOTIFY:
					mFixedHttpThreadPool.execute(new Runnable(){
						@Override
						public void run(){
							do_update_question_notify(msgObj);
						}
					});
					break;
					
			}
		}
	}
}
