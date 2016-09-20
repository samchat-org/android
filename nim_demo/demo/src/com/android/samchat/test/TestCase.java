package com.android.samchat.test;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.android.samchat.R;
import com.android.samchat.callback.SendQuestionCallback;
import android.widget.LinearLayout;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.adapter.ReceivedQuestionAdapter;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.adapter.SendQuestionAdapter;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.Constants;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;

/**
 * Main Fragment in SamchatRequestListFragment
 */
public class TestCase{
	static public void testInitDB(){
	//SendQuestion DB
	
	}

	public static void createFollowList(){
		SamService.getInstance().getDao().delete_FollowList_db_all();
		SamService.getInstance().getDao().add_FollowList_db(new FollowedSamPros(100L,"SP100"));
		SamService.getInstance().getDao().add_FollowList_db(new FollowedSamPros(101L,"SP101"));
		SamService.getInstance().getDao().add_FollowList_db(new FollowedSamPros(102L,"SP102"));
		ContactUser SP100 = new ContactUser();
		SP100.setunique_id(100L);
		SP100.setusername("SP100");
		SP100.setavatar(null);
		SP100.setavatar_original(null);
		SP100.setavatar_original("86");
		SP100.setcellphone("13911791236");
		SP100.setemail("dkw1225@sina.com");
		SP100.setaddress("Guangshun Street No 160");
		SP100.setcompany_name("Samchat");
		SP100.setservice_category("IT");
		SP100.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(SP100);

		ContactUser SP101 = new ContactUser();
		SP101.setunique_id(101L);
		SP101.setusername("SP101");
		SP101.setavatar(null);
		SP101.setavatar_original(null);
		SP101.setavatar_original("86");
		SP101.setcellphone("13911791236");
		SP101.setemail("dkw1225@sina.com");
		SP101.setaddress("Guangshun Street No 160");
		SP101.setcompany_name("Samchat");
		SP101.setservice_category("IT");
		SP101.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(SP101);

		ContactUser SP102 = new ContactUser();
		SP102.setunique_id(102L);
		SP102.setusername("SP102");
		SP102.setavatar(null);
		SP102.setavatar_original(null);
		SP102.setavatar_original("86");
		SP102.setcellphone("13911791236");
		SP102.setemail("dkw1225@sina.com");
		SP102.setaddress("Guangshun Street No 160");
		SP102.setcompany_name("Samchat");
		SP102.setservice_category("IT");
		SP102.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(SP102);

	}

	public static void createContactList(){
		SamService.getInstance().getDao().delete_ContactList_db_all(false);
		Contact user = new Contact(2000L,"aser2000",null,"leagal cosultant");
		SamService.getInstance().getDao().update_ContactList_db(user, false);
		ContactUser user2000 = new ContactUser();
		user2000.setunique_id(2000L);
		user2000.setusername("aser2000");
		user2000.setavatar(null);
		user2000.setavatar_original(null);
		user2000.setcountrycode("86");
		user2000.setcellphone("13911791236");
		user2000.setemail("dkw1225@sina.com");
		user2000.setaddress("Guangshun Street No 160");
		user2000.setcompany_name("Samchat");
		user2000.setservice_category("leagal cosultant");
		user2000.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(user2000);
		
		user = new Contact(2001L,"bser2001",null,"doctor");
		SamService.getInstance().getDao().update_ContactList_db(user, false);
		ContactUser user2001 = new ContactUser();
		user2001.setunique_id(2001L);
		user2001.setusername("bser2001");
		user2001.setavatar(null);
		user2001.setavatar_original(null);
		user2001.setcountrycode("86");
		user2001.setcellphone("13911791236");
		user2001.setemail("dkw1225@sina.com");
		user2001.setaddress("Guangshun Street No 160");
		user2001.setcompany_name("Samchat");
		user2001.setservice_category("doctor");
		user2001.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(user2001);
		
		user = new Contact(2002L,"eser2002",null,"chinese teacher");
		SamService.getInstance().getDao().update_ContactList_db(user, false);
		ContactUser user2002 = new ContactUser();
		user2002.setunique_id(2002L);
		user2002.setusername("eser2002");
		user2002.setavatar(null);
		user2002.setavatar_original(null);
		user2002.setcountrycode("86");
		user2002.setcellphone("13911791236");
		user2002.setemail("dkw1225@sina.com");
		user2002.setaddress("Guangshun Street No 160");
		user2002.setcompany_name("Samchat");
		user2002.setservice_category("chinese teacher");
		user2002.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(user2002);
		
		user = new Contact(2003L,"dser2003",null,"english teacher");
		SamService.getInstance().getDao().update_ContactList_db(user, false);
		ContactUser user2003 = new ContactUser();
		user2003.setunique_id(2003L);
		user2003.setusername("dser2003");
		user2003.setavatar(null);
		user2003.setavatar_original(null);
		user2003.setcountrycode("86");
		user2003.setcellphone("13911791236");
		user2003.setemail("dkw1225@sina.com");
		user2003.setaddress("Guangshun Street No 160");
		user2003.setcompany_name("Samchat");
		user2003.setservice_category("english teacher");
		user2003.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(user2003);
		
		user = new Contact(2004L,"cser2004",null,"piano teacher");
		SamService.getInstance().getDao().update_ContactList_db(user, false);
		ContactUser user2004 = new ContactUser();
		user2004.setunique_id(2004L);
		user2004.setusername("cser2004");
		user2004.setavatar(null);
		user2004.setavatar_original(null);
		user2004.setcountrycode("86");
		user2004.setcellphone("13911791236");
		user2004.setemail("dkw1225@sina.com");
		user2004.setaddress("Guangshun Street No 160");
		user2004.setcompany_name("Samchat");
		user2004.setservice_category("piano teacher");
		user2004.setservice_description("Bridge for USA and CHINA");
		SamService.getInstance().getDao().update_ContactUser_db(user2004);

		for(int i=0;i<10;i++){
			user = new Contact(3000L+i,"iser300"+i,null,"piano teacher");
			SamService.getInstance().getDao().update_ContactList_db(user, false);
			ContactUser suser = new ContactUser();
			suser.setunique_id(3000L+i);
			suser.setusername("iser300"+i);
			suser.setavatar(null);
			suser.setavatar_original(null);
			suser.setcountrycode("86");
			suser.setcellphone("13911791236");
			suser.setemail("dkw1225@sina.com");
			suser.setaddress("Guangshun Street No 160");
			suser.setcompany_name("Samchat");
			suser.setservice_category("piano teacher");
			suser.setservice_description("Bridge for USA and CHINA");
			SamService.getInstance().getDao().update_ContactUser_db(suser);
		}
		for(int i=0;i<10;i++){
			user = new Contact(4000L+i,"fser400"+i,null,"piano teacher");
			SamService.getInstance().getDao().update_ContactList_db(user, false);
			ContactUser suser = new ContactUser();
			suser.setunique_id(4000L+i);
			suser.setusername("fser400"+i);
			suser.setavatar(null);
			suser.setavatar_original(null);
			suser.setcountrycode("86");
			suser.setcellphone("13911791236");
			suser.setemail("dkw1225@sina.com");
			suser.setaddress("Guangshun Street No 160");
			suser.setcompany_name("Samchat");
			suser.setservice_category("piano teacher");
			suser.setservice_description("Bridge for USA and CHINA");
			SamService.getInstance().getDao().update_ContactUser_db(suser);
		}

		for(int i=0;i<10;i++){
			user = new Contact(5000L+i,"董500"+i,null,"piano teacher");
			SamService.getInstance().getDao().update_ContactList_db(user, false);
			ContactUser suser = new ContactUser();
			suser.setunique_id(5000L+i);
			suser.setusername("董500"+i);
			suser.setavatar(null);
			suser.setavatar_original(null);
			suser.setcountrycode("86");
			suser.setcellphone("13911791236");
			suser.setemail("dkw1225@sina.com");
			suser.setaddress("Guangshun Street No 160");
			suser.setcompany_name("Samchat");
			suser.setservice_category("piano teacher");
			suser.setservice_description("Bridge for USA and CHINA");
			SamService.getInstance().getDao().update_ContactUser_db(suser);
		}

		for(int i=0;i<10;i++){
			user = new Contact(6000L+i,"陈600"+i,null,"piano teacher");
			SamService.getInstance().getDao().update_ContactList_db(user, false);
			ContactUser suser = new ContactUser();
			suser.setunique_id(6000L+i);
			suser.setusername("陈600"+i);
			suser.setavatar(null);
			suser.setavatar_original(null);
			suser.setcountrycode("86");
			suser.setcellphone("13911791236");
			suser.setemail("dkw1225@sina.com");
			suser.setaddress("Guangshun Street No 160");
			suser.setcompany_name("Samchat");
			suser.setservice_category("piano teacher");
			suser.setservice_description("Bridge for USA and CHINA");
			SamService.getInstance().getDao().update_ContactUser_db(suser);
		}

	}
}




