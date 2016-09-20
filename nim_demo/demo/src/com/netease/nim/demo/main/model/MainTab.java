package com.netease.nim.demo.main.model;

import com.android.samchat.R;
import com.netease.nim.demo.main.fragment.ContactListFragment;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.netease.nim.demo.main.fragment.SessionListFragment;
import com.netease.nim.demo.main.reminder.ReminderId;
/*SAMC_BEGIN(Change the main tab from 3 to 5)*/
import com.android.samchat.fragment.SamchatContactListFragment;
import com.android.samchat.fragment.SamchatRequestListFragment;
import com.android.samchat.fragment.SamchatPublicListFragment;
import com.android.samchat.fragment.SamchatChatListFragment;
import com.android.samchat.fragment.SamchatSettingListFragment;
import com.android.samchat.SamchatGlobal;
import android.content.Context;
import com.android.samchat.type.ModeEnum;
/*SAMC_END(Change the main tab from 3 to 5)*/

public enum MainTab {
    /*SAMC_BEGIN(Change the main tab from 3 to 5)*/
    SAMCHAT_REQUEST(0, ReminderId.SAMCHAT_TAB_REQUEST, SamchatRequestListFragment.class, R.string.main_tab_session, R.layout.samchat_request_list),
    SAMCHAT_PUBLIC(1, ReminderId.SAMCHAT_TAB_PUBLIC, SamchatPublicListFragment.class, R.string.main_tab_session, R.layout.samchat_public_list),
    SAMCHAT_CHAT(2, ReminderId.SAMCHAT_TAB_CHAT, SamchatChatListFragment.class, R.string.main_tab_session, R.layout.samchat_chat_list),
	 SAMCHAT_CONTACT(3, ReminderId.SAMCHAT_TAB_CONTACT, SamchatContactListFragment.class, R.string.main_tab_contact, R.layout.samchat_contacts_list),
    SAMCHAT_SETTING(4, ReminderId.SAMCHAT_TAB_SETTING, SamchatSettingListFragment.class, R.string.main_tab_contact, R.layout.samchat_setting_list);

	 //RECENT_CONTACTS(5, ReminderId.SESSION, SessionListFragment.class, R.string.main_tab_session, R.layout.session_list),
    //CONTACT(6, ReminderId.CONTACT, ContactListFragment.class, R.string.main_tab_contact, R.layout.contacts_list),
    //CHAT_ROOM(7, ReminderId.INVALID, ChatRoomListFragment.class, R.string.chat_room, R.layout.chat_room_tab),
    /*SAMC_END(Change the main tab from 3 to 5)*/

    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends MainTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    MainTab(int index, int reminderId, Class<? extends MainTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final MainTab fromReminderId(int reminderId) {
        for (MainTab value : MainTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final MainTab fromTabIndex(int tabIndex) {
        for (MainTab value : MainTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }

    public static final int getTabTitle(int index){
		int title_string_id;
        MainTab tab = fromTabIndex(index);

		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			switch (tab){
                case SAMCHAT_REQUEST:
					title_string_id = R.string.customer_request_service;
					break;
				case SAMCHAT_PUBLIC:
					title_string_id = R.string.customer_public;
					break;
				case SAMCHAT_CHAT:
					title_string_id = R.string.customer_chat;
					break;
				case SAMCHAT_CONTACT:
					title_string_id = R.string.customer_service_provider;
					break;
				case SAMCHAT_SETTING:
					title_string_id = R.string.customer_setting;
					break;
				default:
					title_string_id = R.string.customer_request_service;
					break;
			}
		}else{
			switch (tab){
				case SAMCHAT_REQUEST:
					title_string_id = R.string.sp_service_request;
					break;
				case SAMCHAT_PUBLIC:
					title_string_id = R.string.sp_public;
					break;
				case SAMCHAT_CHAT:
					title_string_id = R.string.sp_chat;
					break;
				case SAMCHAT_CONTACT:
					title_string_id = R.string.sp_client;
					break;
				case SAMCHAT_SETTING:
					title_string_id = R.string.sp_setting;
					break;
				default:
					title_string_id = R.string.sp_service_request;
					break;
			}
		}

		return title_string_id;
	}

	public static final int getTabIcon(int index){
		int icon_id;
        MainTab tab = fromTabIndex(index);

		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			switch (tab){
             case SAMCHAT_REQUEST:
				case SAMCHAT_PUBLIC:
				case SAMCHAT_CHAT:
				case SAMCHAT_CONTACT:
				case SAMCHAT_SETTING:
					icon_id = R.drawable.samchat_switch_to_sp_icon;
				default:
					icon_id = R.drawable.samchat_switch_to_sp_icon;
					break;
			}
		}else{
			switch (tab){
             case SAMCHAT_REQUEST:
				case SAMCHAT_PUBLIC:
				case SAMCHAT_CHAT:
				case SAMCHAT_CONTACT:
				case SAMCHAT_SETTING:
					icon_id = R.drawable.samchat_switch_to_customer_icon;
				default:
					icon_id = R.drawable.samchat_switch_to_customer_icon;
					break;
			}
		}

		return icon_id;
	}

		
}
