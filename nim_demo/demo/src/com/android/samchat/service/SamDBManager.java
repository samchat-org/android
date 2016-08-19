package com.android.samchat.service;

import com.android.samservice.info.MsgSession;
import java.util.List;
import java.util.ArrayList;
import com.android.samservice.info.Message;
import com.netease.nim.uikit.NimConstants;
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
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import java.util.Collections;
import java.util.Comparator;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
/*DB Operation class used by UI*/
public class SamDBManager{
	//msg session changed observer: used for last msg update, unread count update
	private List<SamchatObserver<MsgSession>> MsgSessionObservers;
	//incoming rcvd msg observer: samchatChatFragment and MessageFragment will register
	private List<SamchatObserver<List<IMMessage>>> IncomingMsgObservers;
	//customer msg send observer: MessageFragment will register
	private List<SamchatObserver<IMMessage>> SendCustomerMsgObservers;
	//thread pool for db operation
	private ExecutorService mFixedHttpThreadPool;
	//single instance
	private static SamDBManager instance;
	
	static public SamDBManager getInstance(){
		if(instance == null){
			instance	= new SamDBManager();
		}
		return instance;
	}

	public SamDBManager(){
		mFixedHttpThreadPool = Executors.newFixedThreadPool(1);
		MsgSessionObservers = new ArrayList<SamchatObserver<MsgSession>>();
		IncomingMsgObservers = new ArrayList<SamchatObserver<List<IMMessage>>>();
		SendCustomerMsgObservers = new ArrayList<SamchatObserver<IMMessage>>();
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

	//call msg session changed observer one by one
	public void callMsgServiceObserverCallback(MsgSession var){
		for(SamchatObserver<MsgSession> observer : MsgSessionObservers){
			observer.onEvent(var);
		}
	}

	//call incoming immessage observer one by one
	public void callIncomingMsgObserverCallback(List<IMMessage> var){
		for(SamchatObserver<List<IMMessage>> observer : IncomingMsgObservers){
			observer.onEvent(var);
		}
	}

	//call send customer immessage observer one by one
	public void callSendCustomerMsgObserverCallback(IMMessage var){
		for(SamchatObserver<IMMessage> observer : SendCustomerMsgObservers){
			observer.onEvent(var);
		}
	}


	//Message: message db
	//IMMessage: yunxin message

	//create message from IMMessage
	private List<Message> createMessages(String session, int mode ,List<IMMessage> messages){
		if(messages ==  null || messages.size() == 0){
			return null;
		}
		
		List<Message> msgs = new ArrayList<Message>();
		for(IMMessage im : messages){
			msgs.add( new Message(NimConstants.MSG_TYPE_IM, im.getUuid()));
			LogUtil.e("test","IMMessage received: "+im.getContent()+" uuid:"+im.getUuid());
		}
		return msgs;
	}

	/*store send IMMessage(convert to message) into message db
	*	
	*	return value: changed msg session
	*/
	private MsgSession storeSendMessage(String session_id, int mode, Message msg, IMMessage im){
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
		if(session == null){
			String msg_table_name = "msgdb_"+StringUtil.makeMd5(session_id)+"_"+mode;//StringUtil.makeMd5(session_id)+"_"+mode;
			session = new MsgSession( session_id,  mode,  msg_table_name);
			LogUtil.e("test","createMsgTable "+msg_table_name);
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		long rc = SamService.getInstance().getDao().add_Message_db(session.getmsg_table_name(), msg);
		
		if(rc != -1){
			updateSessionBySendMsg(session, im);
			if(SamService.getInstance().getDao().update_MsgSession_db(session) != -1){
				LogUtil.e("test","storeSendMessage update msg session succeed");
				return session;
			}else{
				LogUtil.e("test","storeSendMessage update msg session failed");
				return null;
			}
		}else{
			LogUtil.e("test","storeSendMessage add_Message_db failed");
			return null;
		}
		
	}

	private void updateSessionByRcvdMsg(MsgSession session, List<IMMessage> ims, boolean recordUnread){
		if(ims == null || ims.size() <=0 ){
			return;
		}

		if(recordUnread){
			session.settotal_unread(session.gettotal_unread()+ims.size());
		}
		session.setrecent_msg_type(NimConstants.MSG_TYPE_IM);
		session.setrecent_msg_uuid(ims.get(ims.size() - 1).getUuid());
		session.setrecent_msg_subtype(ims.get(ims.size() - 1).getMsgType().getValue());
		session.setrecent_msg_content(ims.get(ims.size() - 1).getContent());
		session.setrecent_msg_time(ims.get(ims.size() - 1).getTime());
		session.setrecent_msg_status(NimConstants.MSG_STATUS_DEFAULT);
	}

	private void updateSessionBySendMsg(MsgSession session, IMMessage im){
		if(im == null){
			return;
		}
		session.setrecent_msg_type(NimConstants.MSG_TYPE_IM);
		session.setrecent_msg_uuid(im.getUuid());
		session.setrecent_msg_subtype(im.getMsgType().getValue());
		session.setrecent_msg_content(im.getContent());
		session.setrecent_msg_time(im.getTime());
		session.setrecent_msg_status(im.getStatus().getValue());
	}

	private int getMsgType(IMMessage im){
		Map<String,Object> content = im.getLocalExtension();
		if(content == null ||!content.containsKey(NimConstants.MSG_TYPE)){
			return NimConstants.MSG_TYPE_IM;
		}else{
			return NimConstants.MSG_TYPE_SQ;
		} 
	}

	private void updateSessionByDeleteMsg(MsgSession session, IMMessage im){
		if(im == null){
			session.setrecent_msg_type(Constants.MSG_TYPE_IM);
			session.setrecent_msg_uuid(null);
			session.setrecent_msg_subtype(NimConstants.MSG_SUBTYPE_DEFAULT);
			session.setrecent_msg_content(null);
			session.setrecent_msg_time(0);
			session.setrecent_msg_status(NimConstants.MSG_STATUS_DEFAULT);
		}else{
			session.setrecent_msg_type(getMsgType(im));
			session.setrecent_msg_uuid(im.getUuid());
			session.setrecent_msg_subtype(im.getMsgType().getValue());
			session.setrecent_msg_content(im.getContent());
			session.setrecent_msg_time(im.getTime());
			session.setrecent_msg_status(im.getStatus().getValue());
		}
	}
	
	private MsgSession storeRcvdMessages(String session_id, int mode, List<Message> dbMsgs, List<IMMessage> ims, boolean recordUnread){
		SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getAccount()));
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
		if(session == null){
			String msg_table_name = "msgdb_"+StringUtil.makeMd5(session_id)+"_"+mode;//StringUtil.makeMd5(session_id)+"_"+mode;
			session = new MsgSession( session_id,  mode,  msg_table_name);
			LogUtil.e("test","createMsgTable "+msg_table_name);
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		int rc = SamService.getInstance().getDao().add_Messages_db(session.getmsg_table_name(), dbMsgs);
		
		if(rc != -1){
			updateSessionByRcvdMsg(session,ims,recordUnread);
			if(SamService.getInstance().getDao().update_MsgSession_db(session) != -1){
				LogUtil.e("test","storeRcvdMessages update msg session succeed");
				return session;
			}else{
				LogUtil.e("test","storeRcvdMessages update msg session failed");
				return null;
			}
		}else{
			LogUtil.e("test","storeRcvdMessages add_Message_db failed");
			return null;
		}		
	}

	public void asyncStoreSendMessage(final IMMessage im,final NIMCallback callback){
		SessionTypeEnum sessionType = im.getSessionType();
		final String sessionId = im.getSessionId();
		if(sessionType !=  SessionTypeEnum.P2P){
			callback.onResult(null,null, -1);
			return;
		}
			
		Map<String, Object> content = im.getRemoteExtension();
		if(content == null || !content.containsKey(Constants.MSG_FROM)){
			callback.onResult(null,null, -1);
			return;
		}

		final int mode = (int)content.get(Constants.MSG_FROM);
		List<IMMessage> ims = new ArrayList<IMMessage>();
		ims.add(im);
		final List<Message> msgs = createMessages(sessionId, mode, ims);
		im.setStatus(MsgStatusEnum.success);
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession changedSession;
				if((changedSession = storeSendMessage(sessionId,mode,msgs.get(0),im)) != null){
 					callback.onResult(im,null,0);
					callMsgServiceObserverCallback(changedSession);
				}else{
					callback.onResult(null,null,-1);
				}
			}
		});
		
	}

	public void asyncStoreSendCustomerMessage(final IMMessage im,final NIMCallback callback){
		SessionTypeEnum sessionType = im.getSessionType();
		final String sessionId = im.getSessionId();
		if(sessionType !=  SessionTypeEnum.P2P){
			callback.onResult(null,null, -1);
			return;
		}
			
		Map<String, Object> content = im.getRemoteExtension();
		if(content == null || !content.containsKey(Constants.MSG_FROM)){
			callback.onResult(null,null, -1);
			return;
		}

		final int mode = (int)content.get(Constants.MSG_FROM);
		List<IMMessage> ims = new ArrayList<IMMessage>();
		ims.add(im);
		final List<Message> msgs = createMessages(sessionId, mode, ims);
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession changedSession;
				if((changedSession = storeSendMessage(sessionId,mode,msgs.get(0),im)) != null){
 					callback.onResult(im,null,0);
					callMsgServiceObserverCallback(changedSession);
					callSendCustomerMsgObserverCallback(im);
				}else{
					callback.onResult(null,null,-1);
				}
			}
		});
		
	}


	public void asyncDeleteMessage(final String session_id, final int mode, final IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
				if(session == null){
					return;
				}

				String table = session.getmsg_table_name();
				SamService.getInstance().getDao().delete_Message_db(table, im.getUuid());
				List<Message> msgs = SamService.getInstance().getDao().query_Messages_db_Newest(table, 1);
				if(msgs == null || msgs.size() <= 0){
					updateSessionByDeleteMsg(session,null);
				}else{
					List<Message> tmsgs = new ArrayList<Message>(1);
					tmsgs.add(msgs.get(0));
                    List<IMMessage> fims = createIMMessages(tmsgs);
					if(fims == null || fims.size() <= 0){
						updateSessionByDeleteMsg(session,null);
					}else{
						updateSessionByDeleteMsg(session, fims.get(0));
					}
				}
				SamService.getInstance().getDao().update_MsgSession_db(session);
				callMsgServiceObserverCallback(session);
			}
		});
	}

	public void syncDeleteMessageSession(final String session_id, final int mode){
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
		String table = session.getmsg_table_name();
		SamService.getInstance().getDao().delete_MsgSession_db(session_id, mode);
		SamService.getInstance().getDao().delete_Message_db_all(table);
	}

	public void asyncDeleteMessageSession(final String session_id, final int mode){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
				String table = session.getmsg_table_name();
				SamService.getInstance().getDao().delete_MsgSession_db(session_id, mode);
				SamService.getInstance().getDao().delete_Message_db_all(table);
			}
		});
	}

	public void asyncStoreRecvCustomerMessages(final List<IMMessage> ims){
			mFixedHttpThreadPool.execute(new Runnable(){
					@Override
					public void run(){
						LogUtil.e("test","asyncStoreRcvdMessages1:"+ims);
						if(ims == null || ims.size() == 0){
							return;
						}
						LogUtil.e("test","asyncStoreRcvdMessages2");
						SessionTypeEnum sessionType = ims.get(0).getSessionType();
						String sessionId = ims.get(0).getSessionId();
						if(sessionType !=  SessionTypeEnum.P2P){
							return;
						}

						LogUtil.e("test","asyncStoreRcvdMessages3");
						Map<String, Object> content = ims.get(0).getRemoteExtension();
						if(content == null || !content.containsKey(NimConstants.MSG_FROM)){
							return;
						}

						LogUtil.e("test","asyncStoreRcvdMessages4");
						int msg_from = (int)content.get(Constants.MSG_FROM);
						int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
						List<Message> msgs = createMessages(sessionId, mode, ims);
                   MsgSession changedSession;
						if((changedSession = storeRcvdMessages(sessionId,mode,msgs,ims,false)) != null){
							LogUtil.e("test","asyncStoreRcvdMessages5");
							//call session changed observer
							callMsgServiceObserverCallback(changedSession);
							//call incoming IMMessage observer
							callIncomingMsgObserverCallback(ims);
						}
					}
			});
		}
	
	public void syncClearUnreadCount(String session_id, int mode){
		SamService.getInstance().getDao().update_MsgSession_db_unread_count(session_id, mode, 0);		
	}

	public void asyncClearUnreadCount(final String session_id, final int mode){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				if(SamService.getInstance().getDao().update_MsgSession_db_unread_count(session_id, mode, 0)!=-1){
					MsgSession changedSession = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
					if(changedSession !=null){
						callMsgServiceObserverCallback(changedSession);
					}
				}
				
			}
		});		
	}

	public void asyncReadTotalUnreadCount(final int mode, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				List<MsgSession> sessions = SamService.getInstance().getDao().query_MsgSession_db(mode);
				int total_unread = 0;
				for(MsgSession s:sessions){
					total_unread = total_unread + s.gettotal_unread();
				}
				callback.onResult(new Integer(total_unread),new Integer(mode),0);
			}
		});	
	}

	private IMMessage findMsg(List<IMMessage> messages,String uuid){
		for(IMMessage msg:messages){
			if(uuid.equals(msg.getUuid())){
				return msg;
			}
		}
		return null;
	}
	private List<IMMessage> createIMMessages(List<Message> messages){
		List<IMMessage> fims = new ArrayList<>();
		//query IM messages
		List<String> uuids = new ArrayList<String>();
		for(Message m:messages){
			if(m.gettype() == Constants.MSG_TYPE_IM){
				uuids.add(m.getuuid());
			}
		}

		if(uuids.size() == 0){
			return fims;
		}

		sortMessage(messages);
		List<IMMessage> ims = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
		for(Message msg:messages){
			IMMessage im = findMsg(ims,msg.getuuid());
			if(im!=null){
				fims.add(im);
			}
		}

		return fims;
	}

	private void sortMessage(List<Message> list) {
        if (list.size() == 0) {
            return;
        }
        Collections.sort(list, comp);
    }

	private static Comparator<Message> comp = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            long diff = o2.getid() - o1.getid();
            return diff == 0 ? 0 : (diff > 0 ? -1 : 1);
        }
    };
	
	public void queryMessage(final String session_id,final int mode,final IMMessage im,final QueryDirectionEnum direction,final int count,final NIMCallback callback){
		if(direction != QueryDirectionEnum.QUERY_OLD || count <=0 || callback == null){
			callback.onResult(null,null,-1);
			return;
		}

		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				List<IMMessage> ims = new ArrayList<IMMessage>();
				MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
				LogUtil.e("test","query message msgsession:"+session);
				if(session == null){
					callback.onResult(ims,null,-1);
					return;
				}

				String table = session.getmsg_table_name();
				List<Message> messages=null;
				if(im == null){
					messages = SamService.getInstance().getDao().query_Messages_db_Newest(table,  count);
					LogUtil.e("test","query message when im==null:"+messages);
				}else{
					Message tmsg = SamService.getInstance().getDao().query_Message_db_by_uuid(session.getmsg_table_name(),im.getUuid());
					LogUtil.e("test","query message when im!=null: "+tmsg);
					if(tmsg == null){
						LogUtil.e("test","query message tmg is null");
						callback.onResult(ims,null,-1);
						return;
					}
					messages = SamService.getInstance().getDao().query_Messages_db_by_anchor( table, tmsg.getid(), count);
					LogUtil.e("test","query message when im!=null: "+messages);
				}

				if(messages == null || messages.size() <=0){
					LogUtil.e("test","query message messages is null or size ==0");
					callback.onResult(ims,null,0);
				}else{
					ims = createIMMessages(messages);
					callback.onResult(ims, null ,0);
				}
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
		public void onEvent(final List<IMMessage> ims) {
			mFixedHttpThreadPool.execute(new Runnable(){
					@Override
					public void run(){
						LogUtil.e("test","incomingP2PMessageObserver is called1"+" Thread id:"+Thread.currentThread().getId());
						if(ims == null || ims.size() == 0){
							return;
						}
						LogUtil.e("test","incomingP2PMessageObserver is called2");
						SessionTypeEnum sessionType = ims.get(0).getSessionType();
						String sessionId = ims.get(0).getSessionId();
						if(sessionType !=  SessionTypeEnum.P2P){
							return;
						}

						LogUtil.e("test","incomingP2PMessageObserver is called3");
						Map<String, Object> content = ims.get(0).getRemoteExtension();
						if(content == null || !content.containsKey(Constants.MSG_FROM)){
							return;
						}
			
						int msg_from = (int)content.get(Constants.MSG_FROM);
						int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
						List<Message> msgs = createMessages(sessionId, mode, ims);
                   MsgSession changedSession;
						if((changedSession = storeRcvdMessages(sessionId,mode,msgs,ims,true)) != null){
							//call session changed observer
							callMsgServiceObserverCallback(changedSession);
							//call incoming IMMessage observer
							callIncomingMsgObserverCallback(ims);
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

	public void registerSendCustomerMsgObserver(SamchatObserver<IMMessage> observer,boolean register){
		if(register){
			SendCustomerMsgObservers.add(observer);
		}else{
			SendCustomerMsgObservers.remove(observer);
		}
	}

};
