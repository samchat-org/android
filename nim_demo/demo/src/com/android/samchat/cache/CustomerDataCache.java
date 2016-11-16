package com.android.samchat.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.android.samservice.info.Contact;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.ConvertHelper;

import java.util.Collection;

public class CustomerDataCache {
	public static CustomerDataCache getInstance() {
		return InstanceHolder.instance;
	}

	private Map<Long,Contact> customerMap = new ConcurrentHashMap<>();

	public void clear(){
		customerMap.clear();
	}

	public void buildCache(){
		List<Contact> contacts = SamService.getInstance().getDao().query_ContactList_db_All(true);
		for(Contact user : contacts){
			customerMap.put(user.getunique_id(),user);
		}
	}

	public void clearCache(){
		customerMap.clear();
	}

	public Collection<Contact> getMyCustomers(){
		return customerMap.values();
	}

	public int getMyCustomerCount(){
		return customerMap.size();
	}
	
	public Contact getCustomerByAccount(String account){
		long unique_id = ConvertHelper.stringTolong(account);
		return getCustomerByUniqueID(unique_id);
	}

	public Contact getCustomerByUniqueID(Long unique_id){
		return customerMap.get(unique_id);
	}

	public void addCustomer(Long unique_id,Contact user){
        customerMap.put(unique_id,user);
	}

	public void removeCustomer(long unique_id){
		customerMap.remove(unique_id);
	}
	
	static class InstanceHolder {
		final static CustomerDataCache instance = new CustomerDataCache();
	}
}