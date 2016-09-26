package com.android.samservice.info;

import java.util.ArrayList;
import java.util.List;

public class StateDateInfo{
	private long follow_list_date;
	private long contact_list_date;
	private long customer_list_date;

	public StateDateInfo(long follow_list_date,long contact_list_date,long customer_list_date){
		this.follow_list_date = follow_list_date;
		this.contact_list_date = contact_list_date;
		this.customer_list_date = customer_list_date;
	}

	public long getfollow_list_date(){
		return follow_list_date;
	}

	public long getcontact_list_date(){
		return contact_list_date;
	}

	public long getcustomer_list_date(){
		return customer_list_date;
	}
}


