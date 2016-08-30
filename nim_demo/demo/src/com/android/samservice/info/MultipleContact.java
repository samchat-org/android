package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class MultipleContact{
	private int count;
	private List<Contact> contacts;

	public MultipleContact(){
		count = 0;
		contacts = new ArrayList<Contact>();
	}

	public int getcount(){
		return count;
	}
	public void setcount(int count){
		this.count = count;
	}

	public List<Contact> getcontacts(){
		return contacts;
	}
	public void setcontacts(List<Contact> contacts){
		this.contacts = contacts;
	}

	public void addcontact(Contact contact){
		this.contacts.add(contact);
	}
	
}