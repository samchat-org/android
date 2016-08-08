package com.android.samservice.provider;

import com.android.samservice.SamLog;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper
{
	public static final String TAG = "DatabaseHelper";
	
	public static final String TABLE_NAME_CONTACT_USER = "ContactUserTable";
	public static final String TABLE_NAME_SAMPROS_USER = "SamProsTable";
	public static final String TABLE_NAME_SEND_QUESTION = "SendQuestionTable";
	public static final String TABLE_NAME_RECEIVED_QUESTION = "ReceivedQuestionTable";
	public static final String TABLE_NAME_CONTACT_LIST = "ContactListTable";
	public static final String TABLE_NAME_CUSTOMER_LIST = "CustomerListTable";
	public static final String TABLE_NAME_FOLLOW_LIST = "FollowListTable";
	public static final String TABLE_NAME_SAMPROS_ADV = "SamProsAdvTable";
	
	public static final String TABLE_NAME_RCVD_ADV_SESSION = "RcvdADVSessionTable";

	public static final String TABLE_NAME_MSG_SESSION="MsgSessionTable";

	public String dbname;
	
    public DatabaseHelper(Context context,String dbFolder, String dbname, int version)
    {
		super(new DatabaseContext(context,dbFolder), dbname, null, version);
		this.dbname = dbname;
    }

	private void createContactUserTable(SQLiteDatabase db){
	/*
	id(primary) | unique_id | username | usertype | lastupdate | avatar | avatar_original | countrycode |cellphone | email | address
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_CONTACT_USER + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[unique_id] INTEGER, ");
		sBuffer.append("[username] TEXT ,");
		sBuffer.append("[usertype] INTEGER, ");
		sBuffer.append("[lastupdate] INTEGER, ");
		sBuffer.append("[avatar] TEXT ,");
		sBuffer.append("[avatar_original] TEXT ,");
		sBuffer.append("[countrycode] TEXT ,");
		sBuffer.append("[cellphone] TEXT ,");
		sBuffer.append("[email] TEXT ,");
		sBuffer.append("[address] TEXT )");
		
		db.execSQL(sBuffer.toString());
	}

	private void createSamProsUserTable(SQLiteDatabase db){
	/*
	id(primary) | lastupdate | unique_id | company_name | service_category | service_description 
	               | countrycode | phone | email | address |
	//avatar | avatar_original
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_SAMPROS_USER + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[lastupdate] INTEGER, ");
		sBuffer.append("[unique_id] INTEGER, ");
		sBuffer.append("[company_name] TEXT ,");
		sBuffer.append("[service_category] TEXT ,");
		sBuffer.append("[service_description] TEXT ,");

		sBuffer.append("[countrycode] TEXT ,");
		sBuffer.append("[phone] TEXT ,");
		sBuffer.append("[email] TEXT ,");
		sBuffer.append("[address] TEXT )");
		//sBuffer.append("[avatar] TEXT ,");
		//sBuffer.append("[avatar_original] TEXT )");
		db.execSQL(sBuffer.toString());
	
	}

	private void createSendQuestionTable(SQLiteDatabase db){
	/*
	id(primary) | question_id | question | address | status | datetime | latest_answer_time
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_SEND_QUESTION + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[question_id] INTEGER, ");
		sBuffer.append("[question] TEXT ,");
		sBuffer.append("[address] TEXT ,");
		sBuffer.append("[status] INTEGER ,");
		sBuffer.append("[datetime] INTEGER ,");
		sBuffer.append("[latest_answer_time] INTEGER)");
		db.execSQL(sBuffer.toString());
	}

	private void createReceivedQuestionTable(SQLiteDatabase db){
	/*
	id(primary) | question_id | question | sender_unique_id | status | datetime |address
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_RECEIVED_QUESTION + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[question_id] INTEGER, ");
		sBuffer.append("[question] TEXT ,");
		sBuffer.append("[sender_unique_id] INTEGER ,");
		sBuffer.append("[status] INTEGER ,");
		sBuffer.append("[datetime] INTEGER ,");
		sBuffer.append("[address] TEXT )");
		db.execSQL(sBuffer.toString());
	}

	private void createContactListTable(SQLiteDatabase db){
	/*
	id(primary) | unique_id | username |avatar | service_category
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_CONTACT_LIST + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[unique_id] INTEGER, ");
		sBuffer.append("[username] TEXT,");
		sBuffer.append("[avatar] TEXT,");
		sBuffer.append("[service_category] TEXT )");
		db.execSQL(sBuffer.toString());
	}

	private void createCustomerListTable(SQLiteDatabase db){
	/*
	id(primary) | unique_id | username |avatar | service_category
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_CUSTOMER_LIST + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[unique_id] INTEGER, ");
		sBuffer.append("[username] TEXT,");
		sBuffer.append("[avatar] TEXT,");
		sBuffer.append("[service_category] TEXT )");
		db.execSQL(sBuffer.toString());
	}

	private void createFollowListTable(SQLiteDatabase db){
	/*
	id(primary) | unique_id | username | favourite_tag | block_tag
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_FOLLOW_LIST + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[unique_id] INTEGER, ");
		sBuffer.append("[username] TEXT ,");
		sBuffer.append("[favourite_tag] INTEGER ,");
		sBuffer.append("[block_tag] INTEGER )");
		db.execSQL(sBuffer.toString());
	}

	private void createSamProsAdvTable(SQLiteDatabase db){
	/*
	id(primary) | adv_id | type | content | publish_timestamp 
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_SAMPROS_ADV + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[adv_id] INTEGER, ");
		sBuffer.append("[type] INTEGER ,");
		sBuffer.append("[content] TEXT ,");
		sBuffer.append("[publish_timestamp] INTEGER )");
		db.execSQL(sBuffer.toString());
	}

	private void createRcvdAdvSessionTable(SQLiteDatabase db){
	/*
	id(primary) | session | name
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_RCVD_ADV_SESSION + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[session] INTEGER, ");
		sBuffer.append("[name] TEXT )");
		db.execSQL(sBuffer.toString());
	}

	public void createRcvdAdvTable(SQLiteDatabase db,String table_name){
	/*
	id(primary) | adv_id | publish_timestamp | type | content
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + table_name + "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[adv_id] INTEGER, ");
		sBuffer.append("[publish_timestamp] INTEGER, ");
		sBuffer.append("[type] INTEGER, ");
		sBuffer.append("[content] TEXT )");
		db.execSQL(sBuffer.toString());
	}

	private void createMsgSessionTable(SQLiteDatabase db){
	/*
	id(primary) | session_id | mode | msg_table_name | total_unread | recent_msg_type |recent_msg_uuid
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE [" + TABLE_NAME_MSG_SESSION+ "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[session_id] TEXT, ");
		sBuffer.append("[mode] INTEGER ,");
		sBuffer.append("[msg_table_name] TEXT ,");
		sBuffer.append("[total_unread] INTEGER ,");
		sBuffer.append("[recent_msg_type] INTEGER ,");
		sBuffer.append("[recent_msg_uuid] TEXT )");
		db.execSQL(sBuffer.toString());

		sBuffer = new StringBuffer();
		sBuffer.append("CREATE INDEX IF NOT EXISTS UNIQUE_ID_INDEX ON "+TABLE_NAME_MSG_SESSION+ "(");
		sBuffer.append("session_id)");
		db.execSQL(sBuffer.toString());
	}

	public void createMsgTable(SQLiteDatabase db,String table_name){
	/*
	id(primary) | type | uuid
	*/
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("CREATE TABLE IF NOT EXISTS [" + table_name+ "] (");
		sBuffer.append("[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sBuffer.append("[type] INTEGER ,");
		sBuffer.append("[uuid] TEXT )");
		db.execSQL(sBuffer.toString());
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		SamLog.i(TAG,"onCreate");
		if(dbname.equals(DBManager.USERINFO_DB_NAME)){
			createContactUserTable(db);
			createSamProsUserTable(db);
			createContactListTable(db);
			createCustomerListTable(db);
			createFollowListTable(db);
		}else if(dbname.equals(DBManager.QUESTION_DB_NAME)){
			createSendQuestionTable(db);
			createReceivedQuestionTable(db);
		}else if(dbname.equals(DBManager.RCVDADV_DB_NAME)){
			createRcvdAdvSessionTable(db);
		}else if(dbname.equals(DBManager.WRITE_ADV_DB_NAME)){
			createSamProsAdvTable(db);
		}else if(dbname.equals(DBManager.MESSAGE_DB_NAME)){
			createMsgSessionTable(db);
		}
		
		
		
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if(dbname.equals(DBManager.USERINFO_DB_NAME)){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTACT_USER);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SAMPROS_USER);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTACT_LIST);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FOLLOW_LIST);
		}else if(dbname.equals(DBManager.QUESTION_DB_NAME)){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SEND_QUESTION);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RECEIVED_QUESTION);
		}else if(dbname.equals(DBManager.RCVDADV_DB_NAME)){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RCVD_ADV_SESSION);
		}else if(dbname.equals(DBManager.WRITE_ADV_DB_NAME)){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SAMPROS_ADV);
		}else if(dbname.equals(DBManager.MESSAGE_DB_NAME)){
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MSG_SESSION);
		}
		
		onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
    }

}
