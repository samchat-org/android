package com.android.samservice.provider;

import com.android.samservice.info.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;

public class DBManager
{
	static final String TAG = "DBManager";
	public Context mContext;
	public String mDBFolder;
	//manage 4 db
	/*1. user db: contactuser, samprosuser, contact list, follow list*/
	public static final int USERINFO_DB_VERSION=1;
	public static final String USERINFO_DB_NAME = "user.db";
	private DatabaseHelper userinfo_db_helper;
	private SQLiteDatabase userinfo_db;
	/*2. question db: send question, received question*/
	public static final int QUESTION_DB_VERSION=1;
	public static final String QUESTION_DB_NAME = "question.db";
	private DatabaseHelper question_db_helper;
	private SQLiteDatabase question_db;
	/*3. received advtertisement db: each table for each followed sampros*/
	public static final int RCVDADV_DB_VERSION=1;
	public static final String RCVDADV_DB_NAME = "radv.db";
	private DatabaseHelper rcvd_adv_db_helper;
	private SQLiteDatabase rcvd_adv_db;
	/*4. write advtertisement db: only for sampros*/
	public static final int WRITE_ADV_DB_VERSION=1;
	public static final String WRITE_ADV_DB_NAME = "adv.db";
	private DatabaseHelper write_adv_db_helper;
	private SQLiteDatabase write_adv_db;
	/*5. msg db to distinguish mode*/
	public static final int MSG_DB_VERSION=1;
	public static final String MESSAGE_DB_NAME = "message.db";
	private DatabaseHelper message_db_helper;
	private SQLiteDatabase message_db;
	
	public DBManager(Context context,String dbFolder)
	{
		mContext = context;
		mDBFolder = dbFolder;
		userinfo_db_helper = new DatabaseHelper(mContext,mDBFolder,USERINFO_DB_NAME,USERINFO_DB_VERSION);
		userinfo_db = userinfo_db_helper.getWritableDatabase();

		question_db_helper = new DatabaseHelper(mContext,mDBFolder,QUESTION_DB_NAME,QUESTION_DB_VERSION);
		question_db = question_db_helper.getWritableDatabase();

		rcvd_adv_db_helper = new DatabaseHelper(mContext,mDBFolder,RCVDADV_DB_NAME,RCVDADV_DB_VERSION);
		rcvd_adv_db = rcvd_adv_db_helper.getWritableDatabase();

		write_adv_db_helper = new DatabaseHelper(mContext,mDBFolder,WRITE_ADV_DB_NAME,WRITE_ADV_DB_VERSION);
		write_adv_db = write_adv_db_helper.getWritableDatabase();

		message_db_helper = new DatabaseHelper(mContext,mDBFolder,MESSAGE_DB_NAME,MSG_DB_VERSION);
		message_db = message_db_helper.getWritableDatabase();
	}

/******************************Message Session DB*************************************************/
	public long addMsgSession(MsgSession session){
		String table =  DatabaseHelper.TABLE_NAME_MSG_SESSION;
		ContentValues cv = new ContentValues();
		cv.put("session_id",session.getsession_id());
		cv.put("mode",session.getmode());
		cv.put("msg_table_name",session.getmsg_table_name());
		cv.put("total_unread",session.gettotal_unread());
		cv.put("recent_msg_type",session.getrecent_msg_type());
		cv.put("recent_msg_uuid",session.getrecent_msg_uuid());
		cv.put("recent_msg_subtype",session.getrecent_msg_subtype());
		cv.put("recent_msg_content",session.getrecent_msg_content());
		cv.put("recent_msg_time",session.getrecent_msg_time());
		cv.put("recent_msg_status",session.getrecent_msg_status()); 

		return message_db.insert(table,null,cv);
	}

	public long updateMsgSession(long id, MsgSession session)
	{
		String table =  DatabaseHelper.TABLE_NAME_MSG_SESSION;
		ContentValues cv = new ContentValues();
		cv.put("session_id",session.getsession_id());
		cv.put("mode",session.getmode());
		cv.put("msg_table_name",session.getmsg_table_name());
		cv.put("total_unread",session.gettotal_unread());
		cv.put("recent_msg_type",session.getrecent_msg_type());
		cv.put("recent_msg_uuid",session.getrecent_msg_uuid());
		cv.put("recent_msg_subtype",session.getrecent_msg_subtype());
		cv.put("recent_msg_content",session.getrecent_msg_content());
		cv.put("recent_msg_time",session.getrecent_msg_time());
		cv.put("recent_msg_status",session.getrecent_msg_status()); 

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return message_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateMsgSessionRecentStatus(String session_id, int mode, int status)
	{
		String table =  DatabaseHelper.TABLE_NAME_MSG_SESSION;
		ContentValues cv = new ContentValues();
		cv.put("recent_msg_status",status); 

		String whereClause = "session_id=? and mode=?";
		String [] whereArgs = {session_id,""+mode};

		return message_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateMsgSessionUnreadCount(String session_id, int mode, int count)
	{
		String table =  DatabaseHelper.TABLE_NAME_MSG_SESSION;
		ContentValues cv = new ContentValues();
		cv.put("total_unread",count);

		String whereClause = "session_id=? and mode=?";
		String [] whereArgs = {session_id,""+mode};

		return message_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateMsgSessionRecentMsg(String session_id, int mode, int recent_msg_type, String recent_msg_uuid,
		int recent_msg_subtype,String recent_msg_content,long recent_msg_time,int recent_msg_status)
	{
		String table =  DatabaseHelper.TABLE_NAME_MSG_SESSION;
		ContentValues cv = new ContentValues();
		cv.put("recent_msg_type",recent_msg_type);
		cv.put("recent_msg_uuid",recent_msg_uuid);
		cv.put("recent_msg_subtype",recent_msg_subtype);
		cv.put("recent_msg_content",recent_msg_content);
		cv.put("recent_msg_time",recent_msg_time);
		cv.put("recent_msg_status",recent_msg_status); 

		String whereClause = "session_id=? and mode=?";
		String [] whereArgs = {session_id,""+mode};

		return message_db.update(table,cv,whereClause,whereArgs);
	}

	public void increaseMsgSessionUnreadCount(String session_id, int mode , int increase){
		String rawSQL = "UPDATE "+DatabaseHelper.TABLE_NAME_MSG_SESSION
			+" SET total_unread = total_unread + ? WHERE session_id=? and mode=?";
		Object [] bindArgs={new Integer(increase),session_id, new Integer(mode)};
		
		message_db.execSQL(rawSQL,bindArgs);
	}

	public MsgSession queryMsgSession(String session_id,int mode){
		String table = DatabaseHelper.TABLE_NAME_MSG_SESSION;
		MsgSession session = null;
		String name = null;
		Cursor c = message_db.query(table,null,"session_id=? and mode=?",new String[]{session_id,""+mode},null,null,null);

		while(c.moveToNext()){
			session = new MsgSession();
			session.setid(c.getLong(c.getColumnIndex("id")));
			session.setsession_id(c.getString(c.getColumnIndex("session_id")));
			session.setmode(c.getInt(c.getColumnIndex("mode")));
			session.setmsg_table_name(c.getString(c.getColumnIndex("msg_table_name")));
			session.settotal_unread(c.getInt(c.getColumnIndex("total_unread")));
			session.setrecent_msg_type(c.getInt(c.getColumnIndex("recent_msg_type")));
			session.setrecent_msg_uuid(c.getString(c.getColumnIndex("recent_msg_uuid")));
			session.setrecent_msg_subtype(c.getInt(c.getColumnIndex("recent_msg_subtype")));
			session.setrecent_msg_content(c.getString(c.getColumnIndex("recent_msg_content")));
			session.setrecent_msg_time(c.getLong(c.getColumnIndex("recent_msg_time")));
			session.setrecent_msg_status(c.getInt(c.getColumnIndex("recent_msg_status")));
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" msg session is found in db by session_id:"+session_id+" mode:"+mode);
		}

		return session;
	}

	public List<MsgSession> queryMsgSession(int mode){
		String table = DatabaseHelper.TABLE_NAME_MSG_SESSION;
		MsgSession session = null;
		String name = null;
		Cursor c = message_db.query(table,null,"mode=?",new String[]{""+mode},null,null,null);
		List<MsgSession> sessions = new ArrayList<MsgSession>();
		
		while(c.moveToNext()){
			session = new MsgSession();
			session.setid(c.getLong(c.getColumnIndex("id")));
			session.setsession_id(c.getString(c.getColumnIndex("session_id")));
			session.setmode(c.getInt(c.getColumnIndex("mode")));
			session.setmsg_table_name(c.getString(c.getColumnIndex("msg_table_name")));
			session.settotal_unread(c.getInt(c.getColumnIndex("total_unread")));
			session.setrecent_msg_type(c.getInt(c.getColumnIndex("recent_msg_type")));
			session.setrecent_msg_uuid(c.getString(c.getColumnIndex("recent_msg_uuid")));
			session.setrecent_msg_subtype(c.getInt(c.getColumnIndex("recent_msg_subtype")));
			session.setrecent_msg_content(c.getString(c.getColumnIndex("recent_msg_content")));
			session.setrecent_msg_time(c.getLong(c.getColumnIndex("recent_msg_time")));
			session.setrecent_msg_status(c.getInt(c.getColumnIndex("recent_msg_status")));
			sessions.add(session);
		}

		c.close();

		return sessions;
	}

	public List<MsgSession> queryMsgSessionAll(){
		String table = DatabaseHelper.TABLE_NAME_MSG_SESSION;
		MsgSession session = null;
		Cursor c = message_db.query(table,null,null,null,null,null,null);
		List<MsgSession> sessions = new ArrayList<MsgSession>();
		
		while(c.moveToNext()){
			session = new MsgSession();
			session.setid(c.getLong(c.getColumnIndex("id")));
			session.setsession_id(c.getString(c.getColumnIndex("session_id")));
			session.setmode(c.getInt(c.getColumnIndex("mode")));
			session.setmsg_table_name(c.getString(c.getColumnIndex("msg_table_name")));
			session.settotal_unread(c.getInt(c.getColumnIndex("total_unread")));
			session.setrecent_msg_type(c.getInt(c.getColumnIndex("recent_msg_type")));
			session.setrecent_msg_uuid(c.getString(c.getColumnIndex("recent_msg_uuid")));
			session.setrecent_msg_subtype(c.getInt(c.getColumnIndex("recent_msg_subtype")));
			session.setrecent_msg_content(c.getString(c.getColumnIndex("recent_msg_content")));
			session.setrecent_msg_time(c.getLong(c.getColumnIndex("recent_msg_time")));
			session.setrecent_msg_status(c.getInt(c.getColumnIndex("recent_msg_status")));
			sessions.add(session);
		}

		c.close();

		return sessions;
	}

	public void deleteMsgSession(String session_id,int mode){
		String table = DatabaseHelper.TABLE_NAME_MSG_SESSION ;
		
		message_db.delete(table, "session_id=? and mode=?", new String[]{session_id,""+mode});
	}

/******************************Message DB*************************************************/
	public void createMsgTable(String table){
		message_db_helper.createMsgTable(message_db,table);
	}

	public long addMessage(String table, Message msg){
		ContentValues cv = new ContentValues();
		
		cv.put("type",msg.gettype());
		cv.put("uuid",msg.getuuid());
		cv.put("data_id",msg.getdata_id());

		return message_db.insert(table,null,cv);
	}

	public int addMessages(String table, List<Message> msgs){
		message_db.beginTransaction();
		try{
			for(Message msg : msgs){
				String rawSQL = "INSERT INTO "+table+"(type,uuid,data_id) VALUES (?,?,?)";
				Object [] bindArgs={new Integer(msg.gettype()),new String(msg.getuuid()),new Long(msg.getdata_id())};
				message_db.execSQL(rawSQL, bindArgs);
			}
			message_db.setTransactionSuccessful();
		}catch(Exception e){
			return -1;
		}finally{
			message_db.endTransaction();
		}
		
		return msgs.size();
	}

	public long updateMessageDataID(String table, String uuid, long data_id){
		ContentValues cv = new ContentValues();
		
		cv.put("data_id",data_id);

		String whereClause = "uuid=?";
		String [] whereArgs = {uuid};

		return message_db.update(table,cv,whereClause,whereArgs);
	}

	public List<Message> queryMessages(String table, int count){
		Message msg = null;
		List<Message> msgs = new ArrayList<Message>();
		
		Cursor c = message_db.query(table,null,null,null,null,null,"id desc",""+count);

		while(c.moveToNext()){
			msg = new Message();
			msg.setid(c.getLong(c.getColumnIndex("id")));
			msg.settype(c.getInt(c.getColumnIndex("type")));
			msg.setuuid(c.getString(c.getColumnIndex("uuid")));
			msg.setdata_id(c.getLong(c.getColumnIndex("data_id")));

			msgs.add(0,msg);
		}

		c.close();

		return msgs;
	}

	public List<Message> queryMessages(String table, long id, int count){
		String selections = "id < ?";
		String limits = ""+count;
		String [] selectionArgs = {""+id};
		Message msg = null;
		List<Message> msgs = new ArrayList<Message>();

      Cursor c = message_db.query(table, null, selections, selectionArgs, null, null, "id desc",limits);

		while(c.moveToNext()){
			msg = new Message();
			msg.setid(c.getLong(c.getColumnIndex("id")));
			msg.settype(c.getInt(c.getColumnIndex("type")));
			msg.setuuid(c.getString(c.getColumnIndex("uuid")));
			msg.setdata_id(c.getLong(c.getColumnIndex("data_id")));

			msgs.add(0,msg);
		}

		c.close();

		return msgs;
	}



	public Message queryMessageByUuid(String table, String uuid){
		Message msg = null;

      Cursor c = message_db.query(table, null, "uuid = ?", new String[]{uuid}, null, null, null);

		while(c.moveToNext()){
			msg = new Message();
			msg.setid(c.getLong(c.getColumnIndex("id")));
			msg.settype(c.getInt(c.getColumnIndex("type")));
			msg.setuuid(c.getString(c.getColumnIndex("uuid")));
			msg.setdata_id(c.getLong(c.getColumnIndex("data_id")));
		}

		c.close();

		return msg;
	}

	public Message queryMessageByTypeAndDataID(String table, int type, long data_id){
		Message msg = null;

      Cursor c = message_db.query(table, null, "type = ? and data_id=?", new String[]{""+type,""+data_id}, null, null, null);

		while(c.moveToNext()){
			msg = new Message();
			msg.setid(c.getLong(c.getColumnIndex("id")));
			msg.settype(c.getInt(c.getColumnIndex("type")));
			msg.setuuid(c.getString(c.getColumnIndex("uuid")));
			msg.setdata_id(c.getLong(c.getColumnIndex("data_id")));
		}

		c.close();

		return msg;
	}


	public void deleteMessage(String table, long id){
		message_db.delete(table, "id=?", new String[]{""+id});
	}

	public void deleteMessage(String table, String uuid){
		message_db.delete(table, "uuid=?", new String[]{uuid});
	}

	public void deleteMessageAll(String table){
		message_db.execSQL("DROP TABLE IF EXISTS " + table);
	}

	public void clearMessageAll(String table){
		message_db.delete(table, null, null);
	}


/******************************ContactUser DB**********************************************/
	public long addContactUser(ContactUser user)
	{
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;

		ContentValues cv = new ContentValues();
		cv.put("unique_id",user.getunique_id());
		cv.put("username",user.getusername());
		cv.put("usertype",user.getusertype());
		cv.put("lastupdate",user.getlastupdate());
		cv.put("avatar",user.getavatar());
		cv.put("avatar_original",user.getavatar_original());
		cv.put("countrycode",user.getcountrycode());
		cv.put("cellphone",user.getcellphone());
		cv.put("email",user.getemail());
		cv.put("address",user.getaddress());
		cv.put("company_name",user.getcompany_name());
		cv.put("service_category",user.getservice_category());
		cv.put("service_description",user.getservice_description());
		cv.put("countrycode_sp",user.getcountrycode_sp());
		cv.put("phone_sp",user.getphone_sp());
		cv.put("email_sp",user.getemail_sp());
		cv.put("address_sp",user.getaddress_sp());

		return userinfo_db.insert(table,null,cv);
		
	}

	public long updateContactUser(long id, ContactUser user)
	{
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;

		ContentValues cv = new ContentValues();
		cv.put("unique_id",user.getunique_id());
		cv.put("username",user.getusername());
		cv.put("usertype",user.getusertype());
		cv.put("lastupdate",user.getlastupdate());
		cv.put("avatar",user.getavatar());
		cv.put("avatar_original",user.getavatar_original());
		cv.put("countrycode",user.getcountrycode());
		cv.put("cellphone",user.getcellphone());
		cv.put("email",user.getemail());
		cv.put("address",user.getaddress());
		cv.put("company_name",user.getcompany_name());
		cv.put("service_category",user.getservice_category());
		cv.put("service_description",user.getservice_description());
		cv.put("countrycode_sp",user.getcountrycode_sp());
		cv.put("phone_sp",user.getphone_sp());
		cv.put("email_sp",user.getemail_sp());
		cv.put("address_sp",user.getaddress_sp());


		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
		
	}

	public long updateContactUserByUniqueId(long unique_id, ContactUser user)
	{
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;

		ContentValues cv = new ContentValues();
		cv.put("unique_id",user.getunique_id());
		cv.put("username",user.getusername());
		cv.put("usertype",user.getusertype());
		cv.put("lastupdate",user.getlastupdate());
		cv.put("avatar",user.getavatar());
		cv.put("avatar_original",user.getavatar_original());
		cv.put("countrycode",user.getcountrycode());
		cv.put("cellphone",user.getcellphone());
		cv.put("email",user.getemail());
		cv.put("address",user.getaddress());
		cv.put("company_name",user.getcompany_name());
		cv.put("service_category",user.getservice_category());
		cv.put("service_description",user.getservice_description());
		cv.put("countrycode_sp",user.getcountrycode_sp());
		cv.put("phone_sp",user.getphone_sp());
		cv.put("email_sp",user.getemail_sp());
		cv.put("address_sp",user.getaddress_sp());


		String whereClause = "unique_id=?";
		String [] whereArgs = {""+unique_id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
		
	}

	public long updateContactUserAvtar(long unique_id, String avatar, String avatar_original)
	{
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;

		ContentValues cv = new ContentValues();
		cv.put("avatar",avatar);
		if(avatar_original != null){
			cv.put("avatar_original",avatar_original);
		}

		String whereClause = "unique_id=?";
		String [] whereArgs = {""+unique_id};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
		
	}

	public ContactUser queryContactUser(long id){
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;
		ContactUser user = null;
		Cursor c = userinfo_db.query(table,null,"id=?",new String[]{""+id},null,null,null);

		while(c.moveToNext()){
			user = new ContactUser();
			user.setid(c.getLong(c.getColumnIndex("id")));
			user.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			user.setusername(c.getString(c.getColumnIndex("username")));
			user.setusertype( c.getInt(c.getColumnIndex("usertype")));
 			user.setlastupdate(c.getLong(c.getColumnIndex("lastupdate")));
			user.setavatar(c.getString(c.getColumnIndex("avatar")));
			user.setavatar_original(c.getString(c.getColumnIndex("avatar_original")));
			user.setcountrycode(c.getString(c.getColumnIndex("countrycode")));
			user.setcellphone(c.getString(c.getColumnIndex("cellphone")));
			user.setemail(c.getString(c.getColumnIndex("email")));
			user.setaddress(c.getString(c.getColumnIndex("address")));
			user.setcompany_name(c.getString(c.getColumnIndex("company_name")));
			user.setservice_category(c.getString(c.getColumnIndex("service_category")));
			user.setservice_description(c.getString(c.getColumnIndex("service_description")));
			user.setcountrycode_sp(c.getString(c.getColumnIndex("countrycode_sp")));
			user.setphone_sp(c.getString(c.getColumnIndex("phone_sp")));
			user.setemail_sp(c.getString(c.getColumnIndex("email_sp")));
			user.setaddress_sp(c.getString(c.getColumnIndex("address_sp")));

		}

		c.close();

		return user;
	}


	public ContactUser queryContactUserByUniqueID(long unique_id){
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;
		ContactUser user = null;
		String name = null;
		
		Cursor c = userinfo_db.query(table,null,"unique_id=?",new String[]{""+unique_id},null,null,null);

		while(c.moveToNext()){
			user = new ContactUser();
			user.setid(c.getLong(c.getColumnIndex("id")));
			user.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			user.setusername(c.getString(c.getColumnIndex("username")));
			user.setusertype( c.getInt(c.getColumnIndex("usertype")));
 			user.setlastupdate(c.getLong(c.getColumnIndex("lastupdate")));
			user.setavatar(c.getString(c.getColumnIndex("avatar")));
			user.setavatar_original(c.getString(c.getColumnIndex("avatar_original")));
			user.setcountrycode(c.getString(c.getColumnIndex("countrycode")));
			user.setcellphone(c.getString(c.getColumnIndex("cellphone")));
			user.setemail(c.getString(c.getColumnIndex("email")));
			user.setaddress(c.getString(c.getColumnIndex("address")));
			user.setcompany_name(c.getString(c.getColumnIndex("company_name")));
			user.setservice_category(c.getString(c.getColumnIndex("service_category")));
			user.setservice_description(c.getString(c.getColumnIndex("service_description")));
			user.setcountrycode_sp(c.getString(c.getColumnIndex("countrycode_sp")));
			user.setphone_sp(c.getString(c.getColumnIndex("phone_sp")));
			user.setemail_sp(c.getString(c.getColumnIndex("email_sp")));
			user.setaddress_sp(c.getString(c.getColumnIndex("address_sp")));

			name += ":"+user.getusername()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" user is found in db by unique_id:"+unique_id+" name"+name);
		}

		return user;
	}

	public ContactUser queryContactUserByUsername(String username){
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;
		ContactUser user = null;
		String name = null;
		
		Cursor c = userinfo_db.query(table,null,"username=?",new String[]{username},null,null,null);

		while(c.moveToNext()){
			user = new ContactUser();
			user.setid(c.getLong(c.getColumnIndex("id")));
			user.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			user.setusername(c.getString(c.getColumnIndex("username")));
			user.setusertype( c.getInt(c.getColumnIndex("usertype")));
 			user.setlastupdate(c.getLong(c.getColumnIndex("lastupdate")));
			user.setavatar(c.getString(c.getColumnIndex("avatar")));
			user.setavatar_original(c.getString(c.getColumnIndex("avatar_original")));
			user.setcountrycode(c.getString(c.getColumnIndex("countrycode")));
			user.setcellphone(c.getString(c.getColumnIndex("cellphone")));
			user.setemail(c.getString(c.getColumnIndex("email")));
			user.setaddress(c.getString(c.getColumnIndex("address")));
			user.setcompany_name(c.getString(c.getColumnIndex("company_name")));
			user.setservice_category(c.getString(c.getColumnIndex("service_category")));
			user.setservice_description(c.getString(c.getColumnIndex("service_description")));
			user.setcountrycode_sp(c.getString(c.getColumnIndex("countrycode_sp")));
			user.setphone_sp(c.getString(c.getColumnIndex("phone_sp")));
			user.setemail_sp(c.getString(c.getColumnIndex("email_sp")));
			user.setaddress_sp(c.getString(c.getColumnIndex("address_sp")));

			name += ":"+user.getusername()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" user is found in db by username:"+username+" name"+name);
		}

		return user;
	}

	public List<ContactUser> queryContactUserAll(){
		String table = DatabaseHelper.TABLE_NAME_CONTACT_USER;
		ContactUser user = null;
		List<ContactUser> users = new ArrayList<ContactUser>();
		
		Cursor c = userinfo_db.query(table,null,null,null,null,null,null);

		while(c.moveToNext()){
			user = new ContactUser();
			user.setid(c.getLong(c.getColumnIndex("id")));
			user.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			user.setusername(c.getString(c.getColumnIndex("username")));
			user.setusertype( c.getInt(c.getColumnIndex("usertype")));
 			user.setlastupdate(c.getLong(c.getColumnIndex("lastupdate")));
			user.setavatar(c.getString(c.getColumnIndex("avatar")));
			user.setavatar_original(c.getString(c.getColumnIndex("avatar_original")));
			user.setcountrycode(c.getString(c.getColumnIndex("countrycode")));
			user.setcellphone(c.getString(c.getColumnIndex("cellphone")));
			user.setemail(c.getString(c.getColumnIndex("email")));
			user.setaddress(c.getString(c.getColumnIndex("address")));
			user.setcompany_name(c.getString(c.getColumnIndex("company_name")));
			user.setservice_category(c.getString(c.getColumnIndex("service_category")));
			user.setservice_description(c.getString(c.getColumnIndex("service_description")));
			user.setcountrycode_sp(c.getString(c.getColumnIndex("countrycode_sp")));
			user.setphone_sp(c.getString(c.getColumnIndex("phone_sp")));
			user.setemail_sp(c.getString(c.getColumnIndex("email_sp")));
			user.setaddress_sp(c.getString(c.getColumnIndex("address_sp")));

			users.add(user);
		}

		c.close();

		return users;
	}

/******************************SendQuestion DB**********************************************/
	public long addSendQuestion(SendQuestion question)
	{
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("question_id",question.getquestion_id());
		cv.put("question",question.getquestion());
		cv.put("address",question.getaddress());
		cv.put("status",question.getstatus());
		cv.put("datetime",question.getdatetime());
		cv.put("latest_answer_time",question.getlatest_answer_time());
		cv.put("sp_ids",question.getsp_ids());
		cv.put("unread",question.getunread());

		return question_db.insert(table,null,cv);
	}

	public long updateSendQuestion(long id, SendQuestion question)
	{
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("question_id",question.getquestion_id());
		cv.put("question",question.getquestion());
		cv.put("address",question.getaddress());
		cv.put("status",question.getstatus());
		cv.put("datetime",question.getdatetime());
		cv.put("latest_answer_time",question.getlatest_answer_time());
		cv.put("sp_ids",question.getsp_ids());
		cv.put("unread",question.getunread());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return question_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateSendQuestionSPIDS(long question_id, String sp_ids){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("sp_ids",sp_ids);

		String whereClause = "question_id=?";
		String [] whereArgs = {""+question_id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateSendQuestionUnread(long question_id, int unread){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("unread",unread);

		String whereClause = "question_id=?";
		String [] whereArgs = {""+question_id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public SendQuestion querySendQuestionByQuestionID(long question_id){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;
		SendQuestion question = null;
		String name = null;
		
		Cursor c = question_db.query(table,null,"question_id=?",new String[]{""+question_id},null,null,null);

		while(c.moveToNext()){
			question = new SendQuestion();	
			question.setid(c.getLong(c.getColumnIndex("id")));
			question.setquestion_id(c.getLong(c.getColumnIndex("question_id")));
			question.setquestion(c.getString(c.getColumnIndex("question")));
			question.setaddress(c.getString(c.getColumnIndex("address")));
			question.setstatus( c.getInt(c.getColumnIndex("status")));
			question.setdatetime(c.getLong(c.getColumnIndex("datetime")));
			question.setlatest_answer_time(c.getLong(c.getColumnIndex("latest_answer_time")));
			question.setsp_ids(c.getString(c.getColumnIndex("sp_ids")));
			question.setunread(c.getInt(c.getColumnIndex("unread")));

			name += ":"+question.getquestion_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" sendquestion is found in db by question_id:"+question_id+" question_id"+name);
		}

		return question;
	}

	public List<SendQuestion> querySendQuestionAll(){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;
		List<SendQuestion> questions = new ArrayList<SendQuestion>();
		SendQuestion question = null;
		
		Cursor c = question_db.query(table,null,null,null,null,null,null);

		while(c.moveToNext()){
			question = new SendQuestion();	
			question.setid(c.getLong(c.getColumnIndex("id")));
			question.setquestion_id(c.getLong(c.getColumnIndex("question_id")));
			question.setquestion(c.getString(c.getColumnIndex("question")));
			question.setaddress(c.getString(c.getColumnIndex("address")));
			question.setstatus( c.getInt(c.getColumnIndex("status")));
			question.setdatetime(c.getLong(c.getColumnIndex("datetime")));
			question.setlatest_answer_time(c.getLong(c.getColumnIndex("latest_answer_time")));
			question.setsp_ids(c.getString(c.getColumnIndex("sp_ids")));
			question.setunread(c.getInt(c.getColumnIndex("unread")));

			questions.add(question);
		}

		c.close();

		return questions;
	}

	public void deleteSendQuestionAll(){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;
		question_db.delete(table, null, null);
	}

	public void deleteSendQuestionByID(long question_id){
		String table = DatabaseHelper.TABLE_NAME_SEND_QUESTION;
		question_db.delete(table, "question_id=?", new String[]{""+question_id});
	}

/******************************ReceivedQuestion DB**********************************************/
	public long addReceivedQuestion(ReceivedQuestion question)
	{
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("question_id",question.getquestion_id());
		cv.put("question",question.getquestion());
		cv.put("sender_unique_id",question.getsender_unique_id());
		cv.put("sender_username",question.getsender_username());
		cv.put("status",question.getstatus());
		cv.put("datetime",question.getdatetime());
		cv.put("address",question.getaddress());
		cv.put("unread",question.getunread());

		return question_db.insert(table,null,cv);
	}

	public long updateReceivedQuestion(long id, ReceivedQuestion question)
	{
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;

		ContentValues cv = new ContentValues();
		
		cv.put("question_id",question.getquestion_id());
		cv.put("question",question.getquestion());
		cv.put("sender_unique_id",question.getsender_unique_id());
		cv.put("sender_username",question.getsender_username());
		cv.put("status",question.getstatus());
		cv.put("datetime",question.getdatetime());
		cv.put("address",question.getaddress());
		cv.put("unread",question.getunread());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return question_db.update(table,cv,whereClause,whereArgs);
	}

	public ReceivedQuestion queryReceivedQuestionByQuestionID(long question_id){
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;
		ReceivedQuestion question = null;
		String name = null;
		
		Cursor c = question_db.query(table,null,"question_id=?",new String[]{""+question_id},null,null,null);

		while(c.moveToNext()){
			question = new ReceivedQuestion();	
			question.setid(c.getLong(c.getColumnIndex("id")));
			question.setquestion_id(c.getLong(c.getColumnIndex("question_id")));
			question.setquestion(c.getString(c.getColumnIndex("question")));
			question.setsender_unique_id(c.getLong(c.getColumnIndex("sender_unique_id")));
			question.setsender_username(c.getString(c.getColumnIndex("sender_username")));
			question.setstatus( c.getInt(c.getColumnIndex("status")));
			question.setdatetime(c.getLong(c.getColumnIndex("datetime")));
			question.setaddress(c.getString(c.getColumnIndex("address")));
			question.setunread(c.getInt(c.getColumnIndex("unread")));

			name += ":"+question.getquestion_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" receivedquestion is found in db by question_id:"+question_id+" question_id"+name);
		}

		return question;
	}

	public List<ReceivedQuestion> queryReceivedQuestionBySenderUniqueID(long sender_unique_id){
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;
		ReceivedQuestion question = null;
		List<ReceivedQuestion> questions = new ArrayList<ReceivedQuestion>();
		
		Cursor c = question_db.query(table,null,"sender_unique_id=?",new String[]{""+sender_unique_id},null,null,null);

		while(c.moveToNext()){
			question = new ReceivedQuestion();	
			question.setid(c.getLong(c.getColumnIndex("id")));
			question.setquestion_id(c.getLong(c.getColumnIndex("question_id")));
			question.setquestion(c.getString(c.getColumnIndex("question")));
			question.setsender_unique_id(c.getLong(c.getColumnIndex("sender_unique_id")));
			question.setsender_username(c.getString(c.getColumnIndex("sender_username")));
			question.setstatus( c.getInt(c.getColumnIndex("status")));
			question.setdatetime(c.getLong(c.getColumnIndex("datetime")));
			question.setaddress(c.getString(c.getColumnIndex("address")));
			question.setunread(c.getInt(c.getColumnIndex("unread")));
			
			questions.add(question);
		}

		c.close();

		return questions;
	}

	public List<ReceivedQuestion> queryReceivedQuestionByTimestamp(long time,boolean after){
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;
		ReceivedQuestion question = null;
		List<ReceivedQuestion> questions = new ArrayList<ReceivedQuestion>();

		String selections = null;
		String limits = null;
		String selectionArgs [] = null;

		if(after){
			selections = "datetime >= ?";
			selectionArgs = new String[]{""+time};
		}else{
			selections = "datetime < ?";
			selectionArgs = new String[]{""+time};
		}
		
		Cursor c = question_db.query(table,null,selections,selectionArgs,null,null,null);

		while(c.moveToNext()){
			question = new ReceivedQuestion();	
			question.setid(c.getLong(c.getColumnIndex("id")));
			question.setquestion_id(c.getLong(c.getColumnIndex("question_id")));
			question.setquestion(c.getString(c.getColumnIndex("question")));
			question.setsender_unique_id(c.getLong(c.getColumnIndex("sender_unique_id")));
			question.setsender_username(c.getString(c.getColumnIndex("sender_username")));
			question.setstatus( c.getInt(c.getColumnIndex("status")));
			question.setdatetime(c.getLong(c.getColumnIndex("datetime")));
			question.setaddress(c.getString(c.getColumnIndex("address")));
			question.setunread(c.getInt(c.getColumnIndex("unread")));
			
			questions.add(question);
		}

		c.close();

		return questions;
	}

	public void deleteReceivedQuestionAll(){
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;
		
		question_db.delete(table, null, null);
	}

	public void deleteReceivedQuestionByID(long question_id){
		String table = DatabaseHelper.TABLE_NAME_RECEIVED_QUESTION;
		
		question_db.delete(table, "question_id=?", new String[]{""+question_id});
	}

/******************************Contact List DB**********************************************/
	public long addContact(Contact user, boolean isCustomer)
	{
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;

		ContentValues cv = new ContentValues();
		cv.put("unique_id",user.getunique_id());
		cv.put("username",user.getusername());
		cv.put("avatar",user.getavatar());
		cv.put("service_category",user.getservice_category());

		return userinfo_db.insert(table,null,cv);
	}

	public long updateContact(long id, Contact user,boolean isCustomer)
	{
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;

		ContentValues cv = new ContentValues();
		
		cv.put("unique_id",user.getunique_id());
		cv.put("username",user.getusername());
		cv.put("avatar",user.getavatar());
		cv.put("service_category",user.getservice_category());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public Contact queryContactByUniqueID(long unique_id,boolean isCustomer){
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;

		Contact user = null;
		String name = null;
		
		Cursor c = userinfo_db.query(table,null,"unique_id=?",new String[]{""+unique_id},null,null,null);

		while(c.moveToNext()){
			user = new Contact(c.getLong(c.getColumnIndex("unique_id")),
                    c.getString(c.getColumnIndex("username")),
                    c.getString(c.getColumnIndex("avatar")),
                    c.getString(c.getColumnIndex("service_category")));
			user.setid(c.getLong(c.getColumnIndex("id")));
			name += ":"+user.getunique_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" query contact is found in db by unique_id:"+unique_id+" unique_id"+name);
		}

		return user;
	}

	public List<Contact> queryContactAll(boolean isCustomer){
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;

		Contact user = null;
		String name = null;
		List<Contact> contacts = new ArrayList<Contact>();
		
		Cursor c = userinfo_db.query(table,null,null,null,null,null,null);

		while(c.moveToNext()){
			user = new Contact(c.getLong(c.getColumnIndex("unique_id")),
			c.getString(c.getColumnIndex("username")),
			c.getString(c.getColumnIndex("avatar")),
			c.getString(c.getColumnIndex("service_category")));
			user.setid(c.getLong(c.getColumnIndex("id")));
			contacts.add(user);
		}

		c.close();

		return contacts;
	}


	public void deleteContactByUniqueID(long unique_id,boolean isCustomer){
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;
		
		userinfo_db.delete(table, "unique_id=?", new String[]{""+unique_id});	
	}

	public void deleteContactAll(boolean isCustomer){
		String table = isCustomer? DatabaseHelper.TABLE_NAME_CUSTOMER_LIST:DatabaseHelper.TABLE_NAME_CONTACT_LIST;
		
		userinfo_db.delete(table, null, null);
	}

/******************************Follow List DB**********************************************/
	public long addFollowedSamPros(FollowedSamPros follower)
	{
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;

		ContentValues cv = new ContentValues();
		cv.put("unique_id",follower.getunique_id());
		cv.put("username",follower.getusername());
		cv.put("favourite_tag",follower.getfavourite_tag());
		cv.put("block_tag",follower.getblock_tag());
		cv.put("avatar",follower.getavatar());
		cv.put("service_category",follower.getservice_category());

		return userinfo_db.insert(table,null,cv);
	}

	public long updateFollowedSamPros(long id, FollowedSamPros follower)
	{
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;

		ContentValues cv = new ContentValues();
		
		cv.put("unique_id",follower.getunique_id());
		cv.put("username",follower.getusername());
		cv.put("favourite_tag",follower.getfavourite_tag());
		cv.put("block_tag",follower.getblock_tag());
		cv.put("avatar",follower.getavatar());
		cv.put("service_category",follower.getservice_category());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateFollowedSamProsFavouriteTag(long unique_id, int tag){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;

		ContentValues cv = new ContentValues();
		
		cv.put("favourite_tag",tag);

		String whereClause = "unique_id=?";
		String [] whereArgs = {""+unique_id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public long updateFollowedSamProsBlockTag(long unique_id, int tag){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;

		ContentValues cv = new ContentValues();
		
		cv.put("block_tag",tag);

		String whereClause = "unique_id=?";
		String [] whereArgs = {""+unique_id+""};

		return userinfo_db.update(table,cv,whereClause,whereArgs);
	}

	public FollowedSamPros queryFollowedSamProsByUniqueID(long unique_id){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;
		FollowedSamPros follower = null;
		String name = null;
		
		Cursor c = userinfo_db.query(table,null,"unique_id=?",new String[]{""+unique_id},null,null,null);

		while(c.moveToNext()){
			follower = new FollowedSamPros();	
			follower.setid(c.getLong(c.getColumnIndex("id")));
			follower.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			follower.setusername(c.getString(c.getColumnIndex("username")));
			follower.setfavourite_tag(c.getInt(c.getColumnIndex("favourite_tag")));
			follower.setblock_tag( c.getInt(c.getColumnIndex("block_tag")));
			follower.setavatar( c.getString(c.getColumnIndex("avatar")));
			follower.setservice_category( c.getString(c.getColumnIndex("service_category")));

			name += ":"+follower.getunique_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" query follower is found in db by unique_id:"+unique_id+" unique_id"+name);
		}

		return follower;
	}

	public List<FollowedSamPros> queryFollowedSamProsAll(){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST;
		List<FollowedSamPros> followSps = new ArrayList<FollowedSamPros>();
		FollowedSamPros follower = null;
		
		Cursor c = userinfo_db.query(table,null,null,null,null,null,null);

		while(c.moveToNext()){
			follower = new FollowedSamPros();	
			follower.setid(c.getLong(c.getColumnIndex("id")));
			follower.setunique_id(c.getLong(c.getColumnIndex("unique_id")));
			follower.setusername(c.getString(c.getColumnIndex("username")));
			follower.setfavourite_tag(c.getInt(c.getColumnIndex("favourite_tag")));
			follower.setblock_tag( c.getInt(c.getColumnIndex("block_tag")));
			follower.setavatar( c.getString(c.getColumnIndex("avatar")));
			follower.setservice_category( c.getString(c.getColumnIndex("service_category")));

			followSps.add(follower);
		}

		c.close();

		return followSps;
	}

	public void deleteFollowedSamProsByUniqueID(long unique_id){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST ;
		
		userinfo_db.delete(table, "unique_id=?", new String[]{""+unique_id});	
	}

	public void deleteFollowedSamProsAll(){
		String table = DatabaseHelper.TABLE_NAME_FOLLOW_LIST ;
		
		userinfo_db.delete(table, null, null);
	}

/******************************SamPros Adv **********************************************/
	public long addSamProsAdv(Advertisement adv)
	{
		String table = DatabaseHelper.TABLE_NAME_SAMPROS_ADV;

		ContentValues cv = new ContentValues();
		cv.put("adv_id",adv.getadv_id());
		cv.put("type",adv.gettype());
		cv.put("content",adv.getcontent());
		cv.put("content_thumb",adv.getcontent_thumb());
		cv.put("publish_timestamp",adv.getpublish_timestamp());

		return write_adv_db.insert(table,null,cv);
	}

	public long updateSamProsAdv(long id, Advertisement adv)
	{
		String table = DatabaseHelper.TABLE_NAME_SAMPROS_ADV;

		ContentValues cv = new ContentValues();
		cv.put("adv_id",adv.getadv_id());
		cv.put("type",adv.gettype());
		cv.put("content",adv.getcontent());
		cv.put("content_thumb",adv.getcontent_thumb());
		cv.put("publish_timestamp",adv.getpublish_timestamp());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return write_adv_db.update(table,cv,whereClause,whereArgs);
	}

	public Advertisement querySamProsAdvByAdvID(long adv_id){
		String table = DatabaseHelper.TABLE_NAME_SAMPROS_ADV;
		Advertisement adv = null;
		String name = null;
		
		Cursor c = write_adv_db.query(table,null,"adv_id=?",new String[]{""+adv_id},null,null,null);

		while(c.moveToNext()){
			adv = new Advertisement();
			adv.setid(c.getLong(c.getColumnIndex("id")));
			adv.setadv_id(c.getLong(c.getColumnIndex("adv_id")));
			adv.settype(c.getInt(c.getColumnIndex("type")));
			adv.setcontent(c.getString(c.getColumnIndex("content")));
			adv.setcontent_thumb(c.getString(c.getColumnIndex("content_thumb")));
			adv.setpublish_timestamp(c.getLong(c.getColumnIndex("publish_timestamp")));
			name += ":"+adv.getadv_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" adv is found in db by adv_id:"+adv_id+" adv_id"+name);
		}

		return adv;
	}

	public void deleteSamProsAdvByAdvID(long adv_id){
		String table = DatabaseHelper.TABLE_NAME_SAMPROS_ADV ;
		
		write_adv_db.delete(table, "adv_id=?", new String[]{""+adv_id});	
	}

	public void deleteSamProsAdvAll(){
		String table = DatabaseHelper.TABLE_NAME_SAMPROS_ADV ;
		
		write_adv_db.delete(table, null, null);
	}

/******************************Rcvd Adv Session**********************************************/
	public long addRcvdAdvSession(RcvdAdvSession radvsession)
	{
		String table = DatabaseHelper.TABLE_NAME_RCVD_ADV_SESSION;

		ContentValues cv = new ContentValues();
		cv.put("session",radvsession.getsession());
		cv.put("name",radvsession.getname());
		cv.put("recent_adv_id",radvsession.getrecent_adv_id());
		cv.put("recent_adv_type",radvsession.getrecent_adv_type());
		cv.put("recent_adv_content",radvsession.getrecent_adv_content());
		cv.put("recent_adv_content_thumb",radvsession.getrecent_adv_content_thumb());
		cv.put("recent_adv_publish_timestamp",radvsession.getrecent_adv_publish_timestamp());
		cv.put("unread",radvsession.getunread());

		return rcvd_adv_db.insert(table,null,cv);
	}

    public long updateRcvdAdvSession(long id, RcvdAdvSession radvsession)
	{
		String table = DatabaseHelper.TABLE_NAME_RCVD_ADV_SESSION;

		ContentValues cv = new ContentValues();
		cv.put("session",radvsession.getsession());
		cv.put("name",radvsession.getname());
		cv.put("recent_adv_id",radvsession.getrecent_adv_id());
		cv.put("recent_adv_type",radvsession.getrecent_adv_type());
		cv.put("recent_adv_content",radvsession.getrecent_adv_content());
		cv.put("recent_adv_content_thumb",radvsession.getrecent_adv_content_thumb());
		cv.put("recent_adv_publish_timestamp",radvsession.getrecent_adv_publish_timestamp());
		cv.put("unread",radvsession.getunread());

		String whereClause = "id=?";
		String [] whereArgs = {""+id+""};

		return rcvd_adv_db.update(table,cv,whereClause,whereArgs);
	}

    public RcvdAdvSession queryRcvdAdvSessionBySession(long session){
		String table = DatabaseHelper.TABLE_NAME_RCVD_ADV_SESSION;
		RcvdAdvSession radvsession = null;
		String name = null;
		
		Cursor c = rcvd_adv_db.query(table,null,"session=?",new String[]{""+session},null,null,null);

		while(c.moveToNext()){
			radvsession = new RcvdAdvSession();
			radvsession.setid(c.getLong(c.getColumnIndex("id")));
			radvsession.setsession(c.getLong(c.getColumnIndex("session")));
			radvsession.setname(c.getString(c.getColumnIndex("name")));
			radvsession.setrecent_adv_id(c.getLong(c.getColumnIndex("recent_adv_id")));
			radvsession.setrecent_adv_type(c.getInt(c.getColumnIndex("recent_adv_type")));
			radvsession.setrecent_adv_content(c.getString(c.getColumnIndex("recent_adv_content")));
			radvsession.setrecent_adv_content_thumb(c.getString(c.getColumnIndex("recent_adv_content_thumb")));
			radvsession.setrecent_adv_publish_timestamp(c.getLong(c.getColumnIndex("recent_adv_publish_timestamp")));
			radvsession.setunread(c.getInt(c.getColumnIndex("unread")));
			
			name += ":"+radvsession.getsession()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" radvsession is found in db by session:"+session+" session"+name);
		}

		return radvsession;
	}

	public List<RcvdAdvSession> queryRcvdAdvSessionAll(){
		String table = DatabaseHelper.TABLE_NAME_RCVD_ADV_SESSION;
		RcvdAdvSession radvsession = null;
		List<RcvdAdvSession> sessions = new ArrayList<RcvdAdvSession>();
		
		Cursor c = rcvd_adv_db.query(table,null,null,null,null,null,null);

		while(c.moveToNext()){
			radvsession = new RcvdAdvSession();
			radvsession.setid(c.getLong(c.getColumnIndex("id")));
			radvsession.setsession(c.getLong(c.getColumnIndex("session")));
			radvsession.setname(c.getString(c.getColumnIndex("name")));
			radvsession.setrecent_adv_id(c.getLong(c.getColumnIndex("recent_adv_id")));
			radvsession.setrecent_adv_type(c.getInt(c.getColumnIndex("recent_adv_type")));
			radvsession.setrecent_adv_content(c.getString(c.getColumnIndex("recent_adv_content")));
			radvsession.setrecent_adv_content_thumb(c.getString(c.getColumnIndex("recent_adv_content_thumb")));
			radvsession.setrecent_adv_publish_timestamp(c.getLong(c.getColumnIndex("recent_adv_publish_timestamp")));
			radvsession.setunread(c.getInt(c.getColumnIndex("unread")));
			sessions.add(radvsession);
		}

		c.close();

		return sessions;
	}

	public void deleteRcvdAdvSession(long session){
		String table = DatabaseHelper.TABLE_NAME_RCVD_ADV_SESSION ;
		
		rcvd_adv_db.delete(table, "session=?", new String[]{""+session});
	}

/******************************Rcvd Adv **********************************************/
	public void createRcvdAdvTable(String table){
		rcvd_adv_db_helper.createRcvdAdvTable(rcvd_adv_db, table);
	}

	public long addRcvdAdv(String table, Advertisement adv)
	{
		long ret;
		ContentValues cv = new ContentValues();
		cv.put("adv_id",adv.getadv_id());
		cv.put("type",adv.gettype());
		cv.put("content",adv.getcontent());
		cv.put("content_thumb",adv.getcontent_thumb());
		cv.put("publish_timestamp",adv.getpublish_timestamp());
		cv.put("response",adv.getresponse());
		cv.put("sender_unique_id",adv.getsender_unique_id());

		return rcvd_adv_db.insert(table,null,cv);
	}

	public long updateRcvdAdvResponse(String table, long adv_id, int response)
	{
		long ret;
		ContentValues cv = new ContentValues();
		
		cv.put("response",response);

		String whereClause = "adv_id=?";
		String [] whereArgs = {""+adv_id+""};

		return rcvd_adv_db.update(table,cv,whereClause,whereArgs);
	}

	public Advertisement queryRcvdAdv(String table,long adv_id){
		Advertisement adv = null;
		String name = null;
		Cursor c = rcvd_adv_db.query(table,null,"adv_id = ?",new String[]{""+adv_id},null,null,null);

		while(c.moveToNext()){
			adv = new Advertisement();
			adv.setid(c.getLong(c.getColumnIndex("id")));
			adv.setadv_id(c.getLong(c.getColumnIndex("adv_id")));
			adv.settype(c.getInt(c.getColumnIndex("type")));
			adv.setcontent(c.getString(c.getColumnIndex("content")));
			adv.setcontent_thumb(c.getString(c.getColumnIndex("content_thumb")));
			adv.setpublish_timestamp(c.getLong(c.getColumnIndex("publish_timestamp")));
			adv.setresponse(c.getInt(c.getColumnIndex("response")));
			adv.setsender_unique_id(c.getLong(c.getColumnIndex("sender_unique_id")));
			name += ":"+adv.getadv_id()+":";
		}

		c.close();

		if(c.getCount()>1){
			throw new RuntimeException("code error:"+c.getCount()+" radv is found in db by adv_id:"+adv_id+" adv_id"+name);
		}

		return adv;
	}

	public List<Advertisement> queryRcvdAdvByCondition(String table,long timestamp, int limit){
		List<Advertisement> radvs = new ArrayList<Advertisement>();
		Advertisement adv = null;

		String selections = null;
		String limits = null;
		String selectionArgs [] = null;

		if(timestamp > 0 && limit > 0){
			selections = "publish_timestamp < ?";
			selectionArgs =  new String[]{""+timestamp};
			limits = ""+limit;
		}else if(timestamp > 0 && limit == 0){
			selections = "publish_timestamp < ?";
			selectionArgs = new String[]{""+timestamp};
			limits = null;
		}else if(timestamp == 0 && limit > 0){
			selections = null;
			selectionArgs = null;
			limits = "" + limit;
		}else if(timestamp == 0 && limit == 0){
			selections = null;
			selectionArgs = null;
			limits = null;
		}else{
			throw new RuntimeException("code error: condition fatal for rcvd adv query:"+"timestamp:"+timestamp+" limit:"+limit);	
		}

		
		Cursor c = rcvd_adv_db.query(table,null,selections,selectionArgs,null,null,"publish_timestamp desc",limits);

		while(c.moveToNext()){
			adv = new Advertisement();
			adv.setid(c.getLong(c.getColumnIndex("id")));
			adv.setadv_id(c.getLong(c.getColumnIndex("adv_id")));
			adv.settype(c.getInt(c.getColumnIndex("type")));
			adv.setcontent(c.getString(c.getColumnIndex("content")));
			adv.setcontent_thumb(c.getString(c.getColumnIndex("content_thumb")));
			adv.setpublish_timestamp(c.getLong(c.getColumnIndex("publish_timestamp")));
			adv.setresponse(c.getInt(c.getColumnIndex("response")));
			adv.setsender_unique_id(c.getLong(c.getColumnIndex("sender_unique_id")));
			radvs.add(adv);
		}

		c.close();

		return radvs;
	}

	public void deleteRcvdAdv(String table, long adv_id){
		rcvd_adv_db.delete(table, "adv_id=?", new String[]{""+adv_id});	
	}

	public void deleteRcvdAdvAll(String table){
		message_db.execSQL("DROP TABLE IF EXISTS " + table);
	}

    /**
     * close database
     */
    public void closeDB()
    {
		userinfo_db.close();
		question_db.close();
		rcvd_adv_db.close();
		write_adv_db.close();
		message_db.close();
    }

}
