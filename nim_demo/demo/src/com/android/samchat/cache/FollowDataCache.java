package com.android.samchat.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.android.samservice.info.Contact;
import com.android.samservice.SamService;
import com.android.samservice.info.FollowedSamPros;

import java.util.Collection;

public class FollowDataCache {
	public static FollowDataCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,FollowedSamPros> followMap = new ConcurrentHashMap<>();

	public void buildCache(){
		List<FollowedSamPros> followers = SamService.getInstance().getDao().query_FollowList_db_All();
		for(FollowedSamPros user : followers){
			followMap.put(user.getunique_id(),user);
		}
	}

	public void clearCache(){
		followMap.clear();
	}

	public Collection<FollowedSamPros> getMyFollowSPs(){
		return followMap.values();
	}

	public List<FollowedSamPros> getMyFollowSPsList(){
        FollowedSamPros [] array = followMap.values().toArray(new FollowedSamPros [] {});
        List<FollowedSamPros> list = new ArrayList<>();
        for(FollowedSamPros sp: array){
            list.add(sp);
        }
        return list;
	}

	public int getMyFollowSPCount(){
		return followMap.size();
	}

	public FollowedSamPros getFollowSPByUniqueID(long unique_id){
		return followMap.get(unique_id);
	}

	public void addFollowSP(long unique_id,FollowedSamPros user){
        followMap.put(unique_id,user);
	}
	
	static class InstanceHolder {
		final static FollowDataCache instance = new FollowDataCache();
	}
}