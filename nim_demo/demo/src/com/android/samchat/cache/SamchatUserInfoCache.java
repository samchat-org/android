package com.android.samchat.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.android.samservice.info.ContactUser;
import com.android.samservice.SamService;
import java.util.Collection;

public class SamchatUserInfoCache {
	public static SamchatUserInfoCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,ContactUser> userInfoMap = new ConcurrentHashMap<>();

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

	public void getUserByUniqueIDFromRemote(Long unique_id){
		
	}

	public void addUser(Long unique_id, ContactUser user){
		 userInfoMap.put(unique_id, user);
	}
	
	static class InstanceHolder {
		final static SamchatUserInfoCache instance = new SamchatUserInfoCache();
	}
}