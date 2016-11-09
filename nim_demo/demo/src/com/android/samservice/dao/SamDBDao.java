package com.android.samservice.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;

import com.android.samservice.info.*;
import com.android.samservice.provider.DBManager;

public class SamDBDao{
	public static final String TAG="SamDBDao";
	private Object dbLock_userinfo;
	private Object dbLock_question;
	private Object dbLock_rcvdadv;
	private Object dbLock_writeadv;
	private Object dbLock_msg;
	private DBManager dbHandle;

	public void close(){
        if(dbHandle!=null)
            dbHandle.closeDB();
	}

	public SamDBDao(){
		dbLock_userinfo = null;
		dbLock_question = null;
		dbLock_rcvdadv = null;
		dbLock_writeadv = null;
		dbLock_msg = null;
		dbHandle = null;
	}

	public SamDBDao(Context context,String dbFolder){
		dbLock_userinfo = new Object();
		dbLock_question = new Object();
		dbLock_rcvdadv = new Object();
		dbLock_writeadv = new Object();
		dbLock_msg = new Object();
		dbHandle = new DBManager(context,dbFolder);
	}

	private boolean stringEquals(String s1, String s2){
		if(s1 == null && s2 == null){
			return true;
		}else if(s1 == null && s2 != null){
			return false;
		}else if(s1 != null && s2 == null){
			return false;
		}else{
			return s1.equals(s2);
		}
	}
/********************************************ContactUser DB******************************************************************/
	private boolean compareContactUser(ContactUser old, ContactUser now){
		if(old.getunique_id()!=now.getunique_id()
			||!stringEquals(old.getusername(),now.getusername())
			||old.getusertype()!=now.getusertype()
			||old.getquestion_notify()!=now.getquestion_notify()
			||old.getlastupdate()!=now.getlastupdate()
			||!stringEquals(old.getavatar(),now.getavatar())
			||!stringEquals(old.getavatar_original(),now.getavatar_original())
			||!stringEquals(old.getcountrycode(),now.getcountrycode())
			||!stringEquals(old.getcellphone(),now.getcellphone())
			||!stringEquals(old.getemail(),now.getemail())
			||!stringEquals(old.getaddress(),now.getaddress())
			||!stringEquals(old.getcompany_name(),now.getcompany_name())
			||!stringEquals(old.getservice_category(),now.getservice_category())
			||!stringEquals(old.getservice_description(),now.getservice_description())
			||!stringEquals(old.getcountrycode_sp(),now.getcountrycode_sp())
			||!stringEquals(old.getphone_sp(),now.getphone_sp())
			||!stringEquals(old.getemail_sp(),now.getemail_sp())
			||!stringEquals(old.getaddress_sp(),now.getaddress_sp())
		){
			return true;
		}else{
			return false;
		}
	}

	public long update_ContactUser_db_if_existed(ContactUser user){
		long ret;
		synchronized(dbLock_userinfo){
			ContactUser tuser = dbHandle.queryContactUserByUniqueID(user.getunique_id());
			if(tuser == null){
				return 0;
			}else if(compareContactUser(tuser, user)){
				if(dbHandle.updateContactUser(tuser.getid(), user) != 0){
					ret = tuser.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = tuser.getid();	
			}	
		}
		return ret;
	}
	
	public long update_ContactUser_db(ContactUser user){
		long ret;
		synchronized(dbLock_userinfo){
			ContactUser tuser = dbHandle.queryContactUserByUniqueID(user.getunique_id());
			if(tuser == null){
				ret = dbHandle.addContactUser(user);
			}else if(compareContactUser(tuser, user)){
				if(dbHandle.updateContactUser(tuser.getid(), user) != 0){
					ret = tuser.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = tuser.getid();	
			}	
		}
		return ret;
	}

	public long update_ContactUser_db_avatar(long unique_id, String avatar , String avatar_original){
		long ret;
		
		synchronized(dbLock_userinfo){
			ContactUser cu = dbHandle.queryContactUserByUniqueID(unique_id);
			if(cu == null){
				ret = -1;
			}else{
				if(dbHandle.updateContactUserAvtar( unique_id,  avatar,  avatar_original)!= 0){
					ret = cu.getid();
				}else{
					ret = -1;
				}
			}
		}

		return ret;
	}

	public  ContactUser query_ContactUser_db(long id){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactUser(id);
		}
	}

	public  List<ContactUser> query_ContactUser_db_All(){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactUserAll();
		}
	}

	public ContactUser query_ContactUser_db_by_unique_id(long unique_id){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactUserByUniqueID(unique_id);
		}
	}

	public ContactUser query_ContactUser_db_by_username(String username){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactUserByUsername(username);
		}
	}

/********************************************SendQuestion DB******************************************************************/
	private boolean compareSendQuestion(SendQuestion old, SendQuestion now){
		if(old.getquestion_id() != now.getquestion_id()
			||!stringEquals(old.getaddress(),now.getaddress())
			||old.getstatus() != now.getstatus()
			||old.getdatetime() != now.getdatetime()
			||old.getlatest_answer_time() != now.getlatest_answer_time()
			||!stringEquals(old.getquestion(),now.getquestion())
			||!stringEquals(old.getsp_ids(),now.getsp_ids())
			||old.getunread() != now.getunread()){
			return true;
		}else{
			return false;
		}
	}

	public long update_SendQuestion_db(SendQuestion question){
		long ret;

		synchronized(dbLock_question){
			SendQuestion sq = dbHandle.querySendQuestionByQuestionID(question.getquestion_id());
			if(sq == null){
				ret = dbHandle.addSendQuestion(question);
			}else if(compareSendQuestion(sq, question)){
				if(dbHandle.updateSendQuestion(sq.getid(),question)!= 0){
					ret = sq.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = sq.getid();
			}	
		}
		return ret;
	}

	public long update_SendQuestion_dbUnread(long question_id, int unread){
		long ret;

		synchronized(dbLock_question){
			ret = dbHandle.updateSendQuestionUnread( question_id,  unread);
		}
		return ret;
	}

	public long add_SendQuestion_db_sp_ids(long question_id,String sp_id){
		long ret;
		synchronized(dbLock_question){
			SendQuestion sq = dbHandle.querySendQuestionByQuestionID(question_id);
			if(sq == null){
				ret = -1;
			}else if(sq.getsp_ids() == null){
				ret = dbHandle.updateSendQuestionSPIDS( question_id, sp_id);
			}else{
				ret = dbHandle.updateSendQuestionSPIDS( question_id, sq.getsp_ids()+":"+sp_id);
			}
		}
		return ret;
	}

	public SendQuestion query_SendQuestion_db_by_question_id(long question_id){
		synchronized(dbLock_question){
			return dbHandle.querySendQuestionByQuestionID(question_id);
		}
	}

	public List<SendQuestion> query_SendQuestion_db_Newest(int count){
		synchronized(dbLock_question){
			return dbHandle.querySendQuestionNewest(count);
		}
	}

	public List<SendQuestion> query_SendQuestion_db_ALL(){
		synchronized(dbLock_question){
			return dbHandle.querySendQuestionAll();
		}
	}

	public void delete_SendQuestion_db_ALL(){
		synchronized(dbLock_question){
            dbHandle.deleteSendQuestionAll();
		}
	}

	public void delete_SendQuestion_db_by_question_id(long question_id){
		synchronized(dbLock_question){
            dbHandle.deleteSendQuestionByID(question_id);
		}
	}

/********************************************ReceivedQuestion DB******************************************************************/
	private boolean compareReceivedQuestion(ReceivedQuestion old, ReceivedQuestion now){
		if(old.getquestion_id() != now.getquestion_id()
			||old.getstatus() != now.getstatus()
			||old.getsender_unique_id() != now.getsender_unique_id()
			||!stringEquals(old.getsender_username(),now.getsender_username())
			||old.getdatetime() != now.getdatetime()
			||!stringEquals(old.getaddress(),now.getaddress())
			||!stringEquals(old.getquestion(),now.getquestion())
			||old.getunread() != now.getunread()){
			return true;
		}else{
			return false;
		}
	}

	public long update_ReceivedQuestion_db(ReceivedQuestion question){
		long ret;

		synchronized(dbLock_question){
			ReceivedQuestion sq = dbHandle.queryReceivedQuestionByQuestionID(question.getquestion_id());
			if(sq == null){
				ret = dbHandle.addReceivedQuestion(question);
			}else if(compareReceivedQuestion(sq, question)){
				if(dbHandle.updateReceivedQuestion(sq.getid(),question)!= 0){
					ret = sq.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = sq.getid();
			}	
		}
		return ret;
	}

	public ReceivedQuestion query_ReceivedQuestion_db_by_question_id(long question_id){
		synchronized(dbLock_question){
			return dbHandle.queryReceivedQuestionByQuestionID(question_id);
		}
	}

	public  List<ReceivedQuestion> query_ReceivedQuestion_db_by_sender_unique_id(long sender_unique_id){
		synchronized(dbLock_question){
			return dbHandle.queryReceivedQuestionBySenderUniqueID(sender_unique_id);
		}
	}

	public  List<ReceivedQuestion> query_ReceivedQuestion_db_by_timestamp(long timestamp,boolean after){
		synchronized(dbLock_question){
			return dbHandle.queryReceivedQuestionByTimestamp(timestamp,after);
		}
	}

	public void delete_ReceivedQuestion_db_All(){
		synchronized(dbLock_question){
			dbHandle.deleteReceivedQuestionAll();
		}
	}

	public void delete_ReceivedQuestion_db_by_question_id(long question_id){
		synchronized(dbLock_question){
			dbHandle.deleteReceivedQuestionByID(question_id);
		}
	}
	

/********************************************Contact List DB******************************************************************/
	private boolean compareContact(Contact old, Contact now){
		if(old.getunique_id() != now.getunique_id()
			||!stringEquals(old.getusername(),now.getusername())
			||!stringEquals(old.getavatar(),now.getavatar())
            ||!stringEquals(old.getservice_category(),now.getservice_category()))
		{
			return true;
		}else{
			return false;
		}
	}

	public long add_ContactList_db(Contact user,boolean isCustomer){
		long ret;
		synchronized(dbLock_userinfo){
			ret = dbHandle.addContact(user,isCustomer);
		}

		return ret;
	}

	public long update_ContactList_db(Contact user,boolean isCustomer){
		long ret;

		synchronized(dbLock_userinfo){
			Contact fsp = dbHandle.queryContactByUniqueID(user.getunique_id(),isCustomer);
			if(fsp == null){
				ret = dbHandle.addContact(user,isCustomer);
			}else if(compareContact(fsp, user)){
				if(dbHandle.updateContact(fsp.getid(),user,isCustomer)!= 0){
					ret = fsp.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = fsp.getid();
			}	
		}
		return ret;
	}

	public Contact query_ContactList_db_by_unique_id(long unique_id,boolean isCustomer){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactByUniqueID(unique_id,isCustomer);
		}
	}

	public List<Contact> query_ContactList_db_All(boolean isCustomer){
		synchronized(dbLock_userinfo){
			return dbHandle.queryContactAll(isCustomer);
		}
	}

	public void delete_ContactList_db_by_unique_id(long unique_id,boolean isCustomer){
		synchronized(dbLock_userinfo){
			dbHandle.deleteContactByUniqueID(unique_id, isCustomer);
		}
	}

	//just for test case
	public void delete_ContactList_db_all(boolean isCustomer){
		synchronized(dbLock_userinfo){
			dbHandle.deleteContactAll(isCustomer);
		}
	}

/********************************************Follow List DB******************************************************************/
	private boolean compareFollowedSamPros(FollowedSamPros old, FollowedSamPros now){
		if(old.getunique_id() != now.getunique_id()
			||!stringEquals(old.getusername(),now.getusername())
			||old.getfavourite_tag() != now.getfavourite_tag()
			||old.getblock_tag() != now.getblock_tag()
			||!stringEquals(old.getavatar(),now.getavatar())
			||!stringEquals(old.getservice_category(),now.getservice_category()))
		{
			return true;
		}else{
			return false;
		}
	}

	public long add_FollowList_db(FollowedSamPros follower){
		long ret;
		synchronized(dbLock_userinfo){
			ret = dbHandle.addFollowedSamPros(follower);
		}

		return ret;
	}

	public long update_FollowList_db(FollowedSamPros follower){
		long ret;

		synchronized(dbLock_userinfo){
			FollowedSamPros fsp = dbHandle.queryFollowedSamProsByUniqueID(follower.getunique_id());
			if(fsp == null){
				ret = dbHandle.addFollowedSamPros(follower);
			}else if(compareFollowedSamPros(fsp, follower)){
				if(dbHandle.updateFollowedSamPros(fsp.getid(),follower)!= 0){
					ret = fsp.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = fsp.getid();
			}	
		}
		return ret;
	}

	public long update_FollowList_db_FavouriteTag(long unique_id, int tag){
		long ret;
		
		synchronized(dbLock_userinfo){
			FollowedSamPros fsp = dbHandle.queryFollowedSamProsByUniqueID(unique_id);
			if(fsp == null){
				ret = -1;
			}else{
				if(dbHandle.updateFollowedSamProsFavouriteTag(unique_id,tag)!= 0){
					ret = fsp.getid();
				}else{
					ret = -1;
				}
			}
		}

		return ret;
	}

	public long update_FollowList_db_BlockTag(long unique_id, int tag){
		long ret;
		
		synchronized(dbLock_userinfo){
			FollowedSamPros fsp = dbHandle.queryFollowedSamProsByUniqueID(unique_id);
			if(fsp == null){
				ret = -1;
			}else{
				if(dbHandle.updateFollowedSamProsBlockTag(unique_id,tag)!= 0){
					ret = fsp.getid();
				}else{
					ret = -1;
				}
			}
		}

		return ret;
	}

	public FollowedSamPros query_FollowList_db_by_unique_id(long unique_id){
		synchronized(dbLock_userinfo){
			return dbHandle.queryFollowedSamProsByUniqueID(unique_id);
		}
	}

	public List<FollowedSamPros> query_FollowList_db_All(){
		synchronized(dbLock_userinfo){
			return dbHandle.queryFollowedSamProsAll();
		}
	}

	public void delete_FollowList_db_by_unique_id(long unique_id){
		synchronized(dbLock_userinfo){
			dbHandle.deleteFollowedSamProsByUniqueID(unique_id);
		}
	}

	public void delete_FollowList_db_all(){
		synchronized(dbLock_userinfo){
			dbHandle.deleteFollowedSamProsAll();
		}
	}

/********************************************SamPros Adv******************************************************************/
	private boolean compareSamProsAdvertisement(Advertisement old, Advertisement now){
		if(old.getadv_id() != now.getadv_id()
			||old.gettype() != now.gettype()
			|| old.getpublish_timestamp() != now.getpublish_timestamp()
			|| !stringEquals(old.getcontent(),now.getcontent())
			|| !stringEquals(old.getcontent_thumb(),now.getcontent_thumb())){

			return true;
		}else{
			return false;
		}
	}

	public long add_SamProsAdv_db(Advertisement adv){
		long ret;
		synchronized(dbLock_writeadv){
			ret = dbHandle.addSamProsAdv(adv);
		}
		return ret;
	}

	public long update_SamProsAdv_db(Advertisement adv){
		long ret;
		synchronized(dbLock_writeadv){	
			Advertisement tadv = dbHandle.querySamProsAdvByAdvID(adv.getadv_id());
			if(tadv == null){
				ret = dbHandle.addSamProsAdv(adv);
			}else if(compareSamProsAdvertisement(tadv, adv)){
				if(dbHandle.updateSamProsAdv(tadv.getid(), adv) != 0){
					ret = tadv.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = tadv.getid();	
			}	
		}
		return ret;
	}

	

	public Advertisement query_SamProsAdv_db_by_adv_id(long adv_id){
		synchronized(dbLock_writeadv){
			return dbHandle.querySamProsAdvByAdvID(adv_id);
		}
	}

	public void delete_SamProsAdv_db_by_adv_id(long adv_id){
		synchronized(dbLock_writeadv){
			dbHandle.deleteSamProsAdvByAdvID(adv_id);
		}
	}

	public void delete_SamProsAdv_db_All(){
		synchronized(dbLock_writeadv){
			dbHandle.deleteSamProsAdvAll();
		}
	}

/********************************************Received Adv Table******************************************************************/
	private boolean compareRcvdAdvSession(RcvdAdvSession old, RcvdAdvSession now){
		if( old.getsession() != now.getsession()
			|| !stringEquals(old.getname(),now.getname())
			|| old.getrecent_adv_id() != now.getrecent_adv_id()
			|| old.getrecent_adv_type() != now.getrecent_adv_type()
			|| !stringEquals(old.getrecent_adv_content(),now.getrecent_adv_content())
			|| !stringEquals(old.getrecent_adv_content_thumb(),now.getrecent_adv_content_thumb())
			|| old.getrecent_adv_publish_timestamp() != now.getrecent_adv_publish_timestamp()
			|| old.getunread() != now.getunread()){
			return true;
		}else{
			return false;
		}
	}

	public long add_RcvdAdvSession_db(RcvdAdvSession session){
		long ret;
		synchronized(dbLock_rcvdadv){
			ret = dbHandle.addRcvdAdvSession(session);
		}
		return ret;
	}

	public long update_RcvdAdvSession_db(RcvdAdvSession session){
		long ret;
		synchronized(dbLock_rcvdadv){	
			RcvdAdvSession ts = dbHandle.queryRcvdAdvSessionBySession(session.getsession());
			if(ts == null){
				ret = dbHandle.addRcvdAdvSession(session);
			}else if(compareRcvdAdvSession(ts, session)){
				if(dbHandle.updateRcvdAdvSession(ts.getid(), session) != 0){
					ret = ts.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = ts.getid();	
			}	
		}
		return ret;
	}

	public RcvdAdvSession query_RcvdAdvSession_db(long session){
		synchronized(dbLock_rcvdadv){
			return dbHandle.queryRcvdAdvSessionBySession(session);
		}
	}

	public List<RcvdAdvSession> query_RcvdAdvSession_db_All(){
		synchronized(dbLock_rcvdadv){
			return dbHandle.queryRcvdAdvSessionAll();
		}
	}

	public void delete_RcvdAdvSessionBySession(long session_id){
		synchronized(dbLock_rcvdadv){
			RcvdAdvSession session = dbHandle.queryRcvdAdvSessionBySession(session_id);
			if(session != null){
				dbHandle.deleteRcvdAdvAll(session.getname());
				dbHandle.deleteRcvdAdvSession(session_id);
			}
		}
	}

/********************************************Received Adv Table******************************************************************/	
	public void createRcvdAdvTable(String table){
		synchronized(dbLock_rcvdadv){
			dbHandle.createRcvdAdvTable(table);
		}
	}

	public long add_RcvdAdv_db(String table, Advertisement adv){
		long ret;
		synchronized(dbLock_rcvdadv){
			Advertisement tadv = dbHandle.queryRcvdAdv(table,adv.getadv_id());
			if(tadv == null){
				ret = dbHandle.addRcvdAdv(table, adv);
			}else{
				ret = -1;//tadv.getid();
			}
		}
		return ret;
	}

	public long update_RcvdAdv_db_response(String table, long adv_id, int response){
		long ret;
		synchronized(dbLock_rcvdadv){
			if((ret = dbHandle.updateRcvdAdvResponse( table,  adv_id,  response))==0){
				ret = -1;
			}
		}
		return ret;
	}

	public  List<Advertisement>  query_RcvdAdv_db_by_timestamp(String table,long timestamp, int limit){
		synchronized(dbLock_rcvdadv){
			return dbHandle.queryRcvdAdvByCondition( table, timestamp,  limit);		
		}
	}

	public  List<Advertisement>  query_RcvdAdv_db_Newest(String table, int count){
		synchronized(dbLock_rcvdadv){
			return dbHandle.queryRcvdAdvByNewest(table,count);
		}
	}

	public  Advertisement  query_RcvdAdv_db_by_advid(String table,long adv_id){
		synchronized(dbLock_rcvdadv){
			return dbHandle.queryRcvdAdv( table,  adv_id);
		}
	}
	
	public void delete_RcvdAdv_db(String table, long adv_id){
		synchronized(dbLock_rcvdadv){
			dbHandle.deleteRcvdAdv(table, adv_id);
		}
	}

	public void delete_RcvdAdv_db_ALL(String table){
		synchronized(dbLock_rcvdadv){
			dbHandle.deleteRcvdAdvAll(table);
		}
	}

/********************************************Msg Session Table******************************************************************/
	private boolean compareMsgSession(MsgSession old, MsgSession now){
		if( !stringEquals(old.getsession_id(),now.getsession_id())
			||old.getmode() != now.getmode()
			|| !stringEquals(old.getmsg_table_name(),now.getmsg_table_name())
			||old.gettotal_unread() != now.gettotal_unread()
			||old.getrecent_msg_type() != now.getrecent_msg_type()
			|| !stringEquals(old.getrecent_msg_uuid(),now.getrecent_msg_uuid())
			||old.getrecent_msg_subtype() != now.getrecent_msg_subtype()
			|| !stringEquals(old.getrecent_msg_content(),now.getrecent_msg_content())
			||old.getrecent_msg_time() != now.getrecent_msg_time()
			||old.getrecent_msg_status() != now.getrecent_msg_status()){
			return true;
		}else{
			return false;
		}
	}

	public long add_MsgSession_db(MsgSession session){
		long ret;
		synchronized(dbLock_msg){
			ret = dbHandle.addMsgSession(session);
		}
		return ret;
	}

	public long update_MsgSession_db(MsgSession session){
		long ret;
		synchronized(dbLock_msg){	
			MsgSession ts = dbHandle.queryMsgSession(session.getsession_id(), session.getmode());
			if(ts == null){
				ret = dbHandle.addMsgSession(session);
			}else if(compareMsgSession(ts, session)){
				if(dbHandle.updateMsgSession(ts.getid(), session) != 0){
					ret = ts.getid();
				}else{
					ret = -1;
				}
			}else{
				ret = ts.getid();	
			}	
		}
		return ret;
	}

	public long update_MsgSession_db_unread_count(String session_id, int mode, int count){
		synchronized(dbLock_msg){
			return dbHandle.updateMsgSessionUnreadCount( session_id,  mode, count);
		}
	}

	public long update_MsgSession_db_recent_status(String session_id, int mode, int status){
		synchronized(dbLock_msg){
			return dbHandle.updateMsgSessionRecentStatus( session_id,  mode, status);
		}
	}

	public MsgSession query_MsgSession_db(String session_id, int mode){
		synchronized(dbLock_msg){
			return dbHandle.queryMsgSession( session_id,  mode);
		}
	}

	public List<MsgSession> query_MsgSession_db(int mode){
		synchronized(dbLock_msg){
			return dbHandle.queryMsgSession( mode);
		}
	}

	public List<MsgSession> query_MsgSession_db_All(){
		synchronized(dbLock_msg){
			return dbHandle.queryMsgSessionAll();
		}
	}

	public void delete_MsgSession_db(String session_id, int mode){
		synchronized(dbLock_msg){
			dbHandle.deleteMsgSession( session_id,  mode);
		}
	}

/********************************************Msg Table******************************************************************/
	public void createMsgTable(String table){
		synchronized(dbLock_msg){
			dbHandle.createMsgTable(table);
		}
	}

	public long add_Message_db(String table, Message msg){
		long ret;
		synchronized(dbLock_msg){
			ret = dbHandle.addMessage( table, msg);
		}
		return ret;
	}

	public int add_Messages_db(String table, List < Message > msgs){
		int ret;
		synchronized(dbLock_msg){
			ret = dbHandle.addMessages( table, msgs);
		}
		return ret;
	}

	public long updateMessageDataID(String table, String uuid, long data_id){
		long ret;
		synchronized(dbLock_msg){
			ret = dbHandle.updateMessageDataID( table,  uuid,  data_id);
		}
		return (ret==0 ? -1: ret);
	}

	

	public List<Message> query_Messages_db_Newest(String table, int count){
		synchronized(dbLock_msg){
			return dbHandle.queryMessages( table,  count);
		}
	}

	public List<Message> query_Messages_db_by_anchor(String table, long id, int count){
		synchronized(dbLock_msg){
			return dbHandle.queryMessages( table, id,  count);
		}
	}

	public Message query_Message_db_by_uuid(String table, String uuid){
		synchronized(dbLock_msg){
			return dbHandle.queryMessageByUuid( table, uuid);
		}
	}

	public Message query_Message_db_by_type_data_id(String table, int type , long data_id){
		synchronized(dbLock_msg){
			return dbHandle.queryMessageByTypeAndDataID( table, type, data_id);
		}
	}

	public void delete_Message_db(String table, String uuid){
		long ret;
		synchronized(dbLock_msg){
			dbHandle.deleteMessage( table, uuid);
		}
	}

	public void delete_Message_db_all(String table){
		long ret;
		synchronized(dbLock_msg){
			dbHandle.deleteMessageAll(table);
		}
	}

	public void clear_Message_db_all(String table){
		long ret;
		synchronized(dbLock_msg){
			dbHandle.clearMessageAll(table);
		}
	}

}
