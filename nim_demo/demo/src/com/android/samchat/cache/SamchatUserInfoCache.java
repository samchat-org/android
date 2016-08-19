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
import com.android.samservice.info.SamProsUser;
import com.android.samservice.Constants;

public class SamchatUserInfoCache {
	public static SamchatUserInfoCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,ContactUser> userInfoMap = new ConcurrentHashMap<>();

	private SamProsUser findSP(List<SamProsUser> users, long unique_id){
		for(SamProsUser user:users){
			if(user.getunique_id() == unique_id){
				return user;
			}
		}
		return null;
	}

	public void buildCache(){
		List<ContactUser> users = SamService.getInstance().getDao().query_ContactUser_db_All();
		List<SamProsUser> spusers = SamService.getInstance().getDao().query_SamProsUser_db_All();
		for(ContactUser user : users){
			if(user.getusertype() == Constants.USER){
				userInfoMap.put(user.getunique_id(),user);
			}else{
				SamProsUser sp = findSP(spusers, user.getunique_id());
				if(sp!=null){
					SamProsUser usersp = new SamProsUser(user);
					usersp.setid_sampros(sp.getid_sampros());
					usersp.setcompany_name(sp.getcompany_name());
					usersp.setservice_category(sp.getservice_category());
					usersp.setservice_description(sp.getservice_description());
					usersp.setcountrycode_sampros(sp.getcountrycode_sampros());
					usersp.setphone_sampros(sp.getphone_sampros());
					usersp.setemail_sampros(sp.getemail_sampros());
					usersp.setaddress_sampros(sp.getaddress_sampros());
					userInfoMap.put(user.getunique_id(),usersp);
				}
			}
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