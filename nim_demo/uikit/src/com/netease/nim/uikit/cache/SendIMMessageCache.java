package com.netease.nim.uikit.cache;

import java.util.HashSet;
import java.util.Set;

public class SendIMMessageCache {
	public static SendIMMessageCache getInstance() {
		return InstanceHolder.instance;
	}

	private Set<String> setOfUUID = new HashSet<>();

	synchronized public void buildCache(){
		
	}

	synchronized public void clearCache(){
		setOfUUID.clear();
	}

	synchronized public void add(String uuid){
        setOfUUID.add(uuid);
	}

	synchronized public void remove(String uuid){
		setOfUUID.remove(uuid);
	}

	synchronized public boolean contains(String uuid){
		return setOfUUID.contains(uuid);
	}
	
	static class InstanceHolder {
		final static SendIMMessageCache instance = new SendIMMessageCache();
	}
}