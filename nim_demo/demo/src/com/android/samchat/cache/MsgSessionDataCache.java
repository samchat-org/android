package com.android.samchat.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.android.samservice.info.Contact;
import com.android.samservice.SamService;
import com.android.samservice.info.MsgSession;

import java.util.Collection;

public class MsgSessionDataCache {
	public static MsgSessionDataCache getInstance() {
		return InstanceHolder.instance;
	}

	//key: "unique_id"_"mode"
	private Map<String,MsgSession> sessionMap = new ConcurrentHashMap<>();

	public void buildCache(){
		List<MsgSession> sessions = SamService.getInstance().getDao().query_MsgSession_db_All();
		for(MsgSession s : sessions){
			sessionMap.put(MsgSession.makeKey(s.getsession_id(),s.getmode()),s);
		}
	}

	public void clearCache(){
		sessionMap.clear();
	}

	public Collection<MsgSession> getMsgSessions(){
		return sessionMap.values();
	}

	public List<MsgSession> getMsgSessionsList(){
        MsgSession [] array = sessionMap.values().toArray(new MsgSession [] {});
        List<MsgSession>  list = new ArrayList<>();
        for(MsgSession c : array){
            list.add(c);
        }
        return list;
	}

	public MsgSession getMsgSession(String session_id, int mode){
		return sessionMap.get(MsgSession.makeKey(session_id,  mode));
	}

	public void addMsgSession(String session_id, int mode, MsgSession s){
        sessionMap.put(MsgSession.makeKey(session_id,  mode),s);
	}

	public void removeMsgSession(String session_id, int mode){
		sessionMap.remove(MsgSession.makeKey(session_id,  mode));
	}
	
	static class InstanceHolder {
		final static MsgSessionDataCache instance = new MsgSessionDataCache();
	}
}