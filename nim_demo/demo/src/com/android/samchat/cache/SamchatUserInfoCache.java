package com.android.samchat.cache;

import android.content.Intent;
import android.os.Bundle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.android.samservice.HttpCommClient;
import com.android.samservice.info.ContactUser;
import com.android.samservice.SamService;
import java.util.Collection;
import com.android.samservice.Constants;
import com.android.samservice.type.TypeEnum;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.MultipleUserProfile;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.util.log.LogUtil;


public class SamchatUserInfoCache {
	public static final String TAG = "SamchatUserInfoCache";
	
	public static SamchatUserInfoCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,ContactUser> userInfoMap = new ConcurrentHashMap<>();

	private ContactUser findSP(List<ContactUser> users, long unique_id){
		for(ContactUser user:users){
			if(user.getunique_id() == unique_id){
				return user;
			}
		}
		return null;
	}

	public void buildCache(){
		List<ContactUser> users = SamService.getInstance().getDao().query_ContactUser_db_All();

		for(ContactUser user : users){
			userInfoMap.put(user.getunique_id(),user);
		}
	}

	public void clearCache(){
		userInfoMap.clear();
	}

	public Collection<ContactUser> getUsers(){
		return userInfoMap.values();
	}

	public int getUserCount(){
		return userInfoMap.size();
	}

	public ContactUser getUserByUniqueID(Long unique_id){
		ContactUser user =  userInfoMap.get(unique_id);
		if(user == null){

		}
        return user;
	}

	private long stringTolong(String s){
	 	long ret = -1;
		String account = s;
		if (s.startsWith(NimConstants.PUBLIC_ACCOUNT_PREFIX)) {
            account = s.substring(s.indexOf(NimConstants.PUBLIC_ACCOUNT_PREFIX) + NimConstants.PUBLIC_ACCOUNT_PREFIX.length());
		}
		try{
			ret = Long.valueOf(account);
		}catch(Exception e){
			e.printStackTrace();
			return ret;
		}
		return ret;
	 }
	public ContactUser getUserByAccount(String account){
		long unique_id = stringTolong(account);
		ContactUser user =  userInfoMap.get(unique_id);
		if(user == null){

		}
		return user;
	}

	public void getUserByUniqueIDFromRemote(final Long unique_id){
		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID,null, unique_id, null, true,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {

				}

				@Override
				public void onFailed(int code) {
					LogUtil.e(TAG,"query user failed:"+unique_id+" code:"+code);
				}

				@Override
				public void onError(int code) {
					LogUtil.e(TAG,"query user error:"+unique_id+" code:"+code);
				}

			});
	}

	public void addUser(Long unique_id, ContactUser user){
		 userInfoMap.put(unique_id, user);
	}

	public void addUsers(List<ContactUser> users){
		for(ContactUser user : users){
			userInfoMap.put(user.getunique_id(), user);
		}
	}
	
	static class InstanceHolder {
		final static SamchatUserInfoCache instance = new SamchatUserInfoCache();
	}
}