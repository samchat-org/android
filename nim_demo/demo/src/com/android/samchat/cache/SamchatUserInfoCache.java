package com.android.samchat.cache;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.android.samservice.HttpCommClient;
import com.android.samservice.info.ContactUser;
import com.android.samservice.SamService;
import java.util.Collection;
import com.android.samservice.Constants;
import com.android.samservice.TypeEnum;
import com.android.samservice.SMCallBack;
import com.android.samservice.info.MultipleUserProfile;


public class SamchatUserInfoCache {
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

	public void getUserByUniqueIDFromRemote(final Long unique_id){
		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID,null, unique_id, null, true,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					MultipleUserProfile users = ((HttpCommClient)obj).users;
					if(users.getcount() > 0){
						ContactUser user = users.getusers().get(0);
						addUser( unique_id,  user);
						Intent intent = new Intent();
						intent.setAction(Constants.BROADCAST_USER_INFO_UPDATE);
						Bundle bundle = new Bundle();
						bundle.putSerializable("user", user);
						intent.putExtras(bundle);
						SamService.getInstance().sendbroadcast(intent);
					}

				}

				@Override
				public void onFailed(int code) {}

				@Override
				public void onError(int code) {}

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