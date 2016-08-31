package com.android.samchat.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.android.samservice.info.Contact;
import com.android.samservice.SamService;
import java.util.Collection;

public class ContactDataCache {
	public static ContactDataCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,Contact> contactMap = new ConcurrentHashMap<>();

	public void buildCache(){
		List<Contact> contacts = SamService.getInstance().getDao().query_ContactList_db_All(false);
		for(Contact user : contacts){
			contactMap.put(user.getunique_id(),user);
		}
	}

	public void clearCache(){
		contactMap.clear();
	}

	public Collection<Contact> getMyContacts(){
		return contactMap.values();
	}

	public List<Contact> getMyContactsList(){
        Contact [] array = contactMap.values().toArray(new Contact [] {});
        List<Contact>  list = new ArrayList<>();
        for(Contact c : array){
            list.add(c);
        }
        return list;
	}

	public int getMyContactCount(){
		return contactMap.size();
	}

	public Contact getContactByUniqueID(Long unique_id){
		return contactMap.get(unique_id);
	}

	public void addContact(Long unique_id,Contact user){
        contactMap.put(unique_id,user);
	}
	
	static class InstanceHolder {
		final static ContactDataCache instance = new ContactDataCache();
	}
}