package com.android.samchat.common;

import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.util.string.ConvertHelper;

public class BasicUserInfoHelper {
    static public String getUserName(String account) {
        	ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ConvertHelper.stringTolong(account));
			if(user != null){
				return user.getusername();
			}

			Contact contact = ContactDataCache.getInstance().getContactByUniqueID(ConvertHelper.stringTolong(account));
			if(contact != null){
				return contact.getusername();
			}

			contact = CustomerDataCache.getInstance().getCustomerByUniqueID(ConvertHelper.stringTolong(account));
			if(contact != null){
				return contact.getusername();
			}	
						
			FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(ConvertHelper.stringTolong(account));
			if(fsp !=null){
				return fsp.getusername();
			}
			
			return "";
    }

	static public String getUserName(long unique_id) {
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(unique_id);
		if(user != null){
			return user.getusername();
		}

		Contact contact = ContactDataCache.getInstance().getContactByUniqueID(unique_id);
		if(contact != null){
			return contact.getusername();
		}

		contact = CustomerDataCache.getInstance().getCustomerByUniqueID(unique_id);
		if(contact != null){
			return contact.getusername();
		}	
						
		FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(unique_id);
		if(fsp !=null){
			return fsp.getusername();
		}
			
		return "";
	}
}