package com.android.samchat.service;


import com.android.samservice.info.MsgSession;
import java.util.List;
import java.util.ArrayList;
import com.android.samservice.info.Message;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import java.util.Map;
import com.android.samchat.type.ModeEnum;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.session.sam_message.sam_message;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
/*DB Operation class used by UI*/
public class SamDBManager{
	private List<SamchatObserver<MsgSession>> MsgSessionObservers;
	private List<SamchatObserver<List<IMMessage>>> IncomingMsgObservers;
	private ExecutorService mFixedHttpThreadPool;
	private static SamDBManager instance;

	static public SamDBManager getInstance(){
		if(instance == null){
			instance	= new SamDBManager();
		}
		return instance;
	}

	public SamDBManager(){
		mFixedHttpThreadPool = Executors.newFixedThreadPool(5);
		MsgSessionObservers = new ArrayList<SamchatObserver<MsgSession>>();
		IncomingMsgObservers = new ArrayList<SamchatObserver<List<IMMessage>>>();
	}

	private void close(){
		if(mFixedHttpThreadPool!=null){
			if(!mFixedHttpThreadPool.isShutdown()){
				mFixedHttpThreadPool.shutdown();
			}
			mFixedHttpThreadPool = null;
		}
		instance = null;
	}



	public void callMsgServiceObserverCallback(MsgSession var){
		for(SamchatObserver<MsgSession> observer : MsgSessionObservers){
			observer.onEvent(var);
		}
	}

	public void callIncomingMsgObserverCallback(List<IMMessage> var){
		for(SamchatObserver<List<IMMessage>> observer : IncomingMsgObservers){
			observer.onEvent(var);
		}
	}

	private List<Message> createMessages(String session, int mode ,List<IMMessage> messages){
		List<Message> msgs = new ArrayList<Message>();
		if(messages ==  null || messages.size() == 0){
			return msgs;
		}

		for(IMMessage msg : messages){
			msgs.add( new Message(Constants.MSG_TYPE_IM, msg.getUuid()));
		}
		return msgs;
	}

	private long storeMessage(String session_id, int mode, Message msg){
		SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getTAccount()));
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
		if(session == null){
			String msg_table_name = "msgdb_"+StringUtil.makeMd5(session_id)+"_"+mode;//StringUtil.makeMd5(session_id)+"_"+mode;
			int recent_msg_type = msg.gettype();
			String recent_msg_uuid = msg.getuuid();
			session = new MsgSession( session_id,  mode,  msg_table_name, 0, recent_msg_type,  recent_msg_uuid);
			LogUtil.e("test","createMsgTable "+msg_table_name);
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		long rc = SamService.getInstance().getDao().add_Message_db(session.getmsg_table_name(), msg);

		LogUtil.e("test","add_Message_db "+rc);
		
		if(rc != -1){
			session.settotal_unread(session.gettotal_unread() + 1);
			session.setrecent_msg_type(msg.gettype());
			session.setrecent_msg_uuid(msg.getuuid());
			if(SamService.getInstance().getDao().update_MsgSession_db(session) != -1){
				LogUtil.e("test","update_MsgSession_db not -1");
			}else{
				LogUtil.e("test","update_MsgSession_db -1");
			}
		}

		return rc;
		
	}
	
	private MsgSession storeMessages(String session_id, int mode, List<Message> dbMsgs){
		LogUtil.e("test","storeMessages "+ session_id + " "+mode +" "+dbMsgs+" Thread id:"+Thread.currentThread().getId());
		SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getTAccount()));
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
		if(session == null){
			String msg_table_name = "msgdb_"+StringUtil.makeMd5(session_id)+"_"+mode;//StringUtil.makeMd5(session_id)+"_"+mode;
			int recent_msg_type = dbMsgs.get(dbMsgs.size() - 1).gettype();
			String recent_msg_uuid = dbMsgs.get(dbMsgs.size() - 1).getuuid();
			session = new MsgSession( session_id,  mode,  msg_table_name, 0, recent_msg_type,  recent_msg_uuid);
			LogUtil.e("test","createMsgTable "+msg_table_name);
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		int rc = SamService.getInstance().getDao().add_Messages_db(session.getmsg_table_name(), dbMsgs);

		LogUtil.e("test","add_Messages_db "+rc);
		
		if(rc != -1){

			session.settotal_unread(session.gettotal_unread() + dbMsgs.size());
			session.setrecent_msg_type(dbMsgs.get(dbMsgs.size() - 1).gettype());
			session.setrecent_msg_uuid(dbMsgs.get(dbMsgs.size() - 1).getuuid());
			if(SamService.getInstance().getDao().update_MsgSession_db(session) != -1){
				LogUtil.e("test","update_MsgSession_db not -1");
				return session;
			}else{
				LogUtil.e("test","update_MsgSession_db -1");
				return null;
			}
		}

		return null;
		
	}

	public long syncStoreSendMessage(IMMessage msg){
		SessionTypeEnum sessionType = msg.getSessionType();
		final String sessionId = msg.getSessionId();
		if(sessionType !=  SessionTypeEnum.P2P){
			return -1;
		}
			
		Map<String, Object> content = msg.getLocalExtension();
		if(content == null){
			return -1;
		}

		int msg_to = (int)content.get(Constants.MSG_TO);
		final int mode = msg_to;
		List<IMMessage> messages = new ArrayList<IMMessage>();
		messages.add(msg);
		final List<Message> msgs = createMessages(sessionId, mode, messages);
		return storeMessage(sessionId,mode,msgs.get(0));
	}
	
	public void syncClearUnreadCount(String session_id, int mode){
		SamService.getInstance().getDao().update_MsgSession_db_unread_count(session_id, mode, 0);		
	}

	private Message findMsg(List<Message> messages,String uuid, int type){
		for(Message msg:messages){
			if(msg.gettype() == type && uuid.equals(msg.getuuid())){
				return msg;
			}
		}
		return null;
	}
	private List<sam_message> createNimMessage(List<Message> messages){
		List<sam_message> samMsgs = new ArrayList<>();
		//query IM messages
		List<String> uuids = new ArrayList<String>();
		for(Message m:messages){
			if(m.gettype() == Constants.MSG_TYPE_IM){
				uuids.add(m.getuuid());
			}
		}

		if(uuids.size() == 0){
			return samMsgs;
		}

		List<IMMessage> ims = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
		for(IMMessage im:ims){
			Message msg = findMsg(messages,im.getUuid(),Constants.MSG_TYPE_IM);
			sam_message samMsg = new sam_message(msg.getid(), sam_message.MSG_TYPE_IM, im.getUuid(),im);
			samMsgs.add(samMsg);
		}

		return samMsgs;
	}
	
	public void queryMessage(final String session_id,final int mode,final sam_message msg,final QueryDirectionEnum direction,final int count,final NIMCallback callback){
		if(direction != QueryDirectionEnum.QUERY_OLD || count <=0 || callback == null){
			return;
		}

		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
				if(session == null){
					callback.onResult(null, 0);
					return;
				}
				String table = session.getmsg_table_name();
				List<Message> messages=null;
				if(msg == null){
					messages = SamService.getInstance().getDao().query_Messages_db_Newest(table,  count);
				}else{
					messages = SamService.getInstance().getDao().query_Messages_db_by_anchor( table, msg.getid(), count);
				}

				List<sam_message> msgs = createNimMessage(messages);
				callback.onResult(msgs, 0);
			}
		});

		
	}



	public void registerObservers(boolean register){
		NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingP2PMessageObserver,register);
		LogUtil.e("test","incomingP2PMessageObserver is registered");
		if(!register){
			close();
		}
	}

	Observer<List<IMMessage>> incomingP2PMessageObserver = new Observer<List<IMMessage>>() {
		@Override
		public void onEvent(List<IMMessage> msgs) {
			final List<IMMessage> messages = msgs;
			mFixedHttpThreadPool.execute(new Runnable(){
					@Override
					public void run(){
						LogUtil.e("test","incomingP2PMessageObserver is called1"+" Thread id:"+Thread.currentThread().getId());
						if(messages.size() == 0){
							return;
						}
						LogUtil.e("test","incomingP2PMessageObserver is called2");
						SessionTypeEnum sessionType = messages.get(0).getSessionType();
						String sessionId = messages.get(0).getSessionId();
						if(sessionType !=  SessionTypeEnum.P2P){
							return;
						}

						LogUtil.e("test","incomingP2PMessageObserver is called3");
						Map<String, Object> content = messages.get(0).getRemoteExtension();
						if(content == null){
							return;
						}
			
						int msg_from = (int)content.get(Constants.MSG_FROM);
						int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
						List<Message> samMsgs = createMessages(sessionId, mode, messages);
						MsgSession changedSession = storeMessages(sessionId,mode,samMsgs);
						if(changedSession != null){
							//call session changed observer
							callMsgServiceObserverCallback(changedSession);
							//call incoming IMMessage observer
							callIncomingMsgObserverCallback(messages);
						}
					}
			});
		}
	};

	public void registerMsgSessionObserver(SamchatObserver<MsgSession> observer,boolean register){
		if(register){
			MsgSessionObservers.add(observer);
		}else{
			MsgSessionObservers.remove(observer);
		}
	}

	public void registerIncomingMsgObserver(SamchatObserver<List<IMMessage>> observer,boolean register){
		if(register){
			IncomingMsgObservers.add(observer);
		}else{
			IncomingMsgObservers.remove(observer);
		}
	}

};
