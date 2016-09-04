package com.android.samchat.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samservice.info.AdvancedMessage;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.MsgSession;
import java.util.List;
import java.util.ArrayList;
import com.android.samservice.info.Message;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.session.sam_message.SAMMessage;
import com.netease.nimlib.sdk.RequestCallback;
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
import com.android.samservice.info.SendQuestion;
import com.android.samservice.HttpCommClient;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samservice.info.ContactUser;
/*DB Operation class used by UI*/
public class SamDBManager{
	public static final String TAG = "SamchatSamDBManager";
	//msg session changed observer: used for last msg update, unread count update
	private List<SamchatObserver<MsgSession>> MsgSessionObservers;
	//incoming rcvd msg observer: samchatChatFragment and MessageFragment will register
	private List<SamchatObserver<List<IMMessage>>> IncomingMsgObservers;
	//customer msg send observer: MessageFragment will register
	private List<SamchatObserver<IMMessage>> SendCustomerMsgObservers;
	//received question observer: SamchatRequestFragment of sp mode will register
	private List<SamchatObserver<ReceivedQuestion>> ReceivedQuestionObservers;
	//send question answered observer: SamchatRequestFragment of customer mode will register
	private List<SamchatObserver<List<SendQuestion>>> SendQuestionAnsweredObservers;
	//send question unread clear observer: SamchatRequestFragment of customer mode will register
	private List<SamchatObserver<SendQuestion>> SendQuestionUnreadClearObservers;
	//send adv observer: SamchatPublicFragment of sp mode will register
	private List<SamchatObserver<IMMessage>> SendAdvertisementObservers;
	//send adv status update observer: SamchatPublicFragment of sp mode will register
	private List<SamchatObserver<IMMessage>> SendAdvertisementStatusObservers;
	
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
		ReceivedQuestionObservers = new ArrayList<SamchatObserver<ReceivedQuestion>>();
		SendQuestionAnsweredObservers = new ArrayList<SamchatObserver<List<SendQuestion>>>();
		SendQuestionUnreadClearObservers = new ArrayList<SamchatObserver<SendQuestion>>();
		SendAdvertisementObservers = new ArrayList<SamchatObserver<IMMessage>>();
		SendAdvertisementStatusObservers = new ArrayList<SamchatObserver<IMMessage>>();
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

	//call received question observer one by one
	public void callReceivedQuestionObserverCallback(ReceivedQuestion rq){
		for(SamchatObserver<ReceivedQuestion> observer : ReceivedQuestionObservers){
			observer.onEvent(rq);
		}
	}

	//call send question answered observer one by one
	public void callSendQuestionAnsweredObserverCallback(List<SendQuestion> sqs){
		for(SamchatObserver<List<SendQuestion>> observer : SendQuestionAnsweredObservers){
			observer.onEvent(sqs);
		}
	}

	//call send question unread clear observer one by one
	public void callSendQuestionUnreadClearObserverCallback(SendQuestion sq){
		for(SamchatObserver<SendQuestion> observer : SendQuestionUnreadClearObservers){
			observer.onEvent(sq);
		}
	}

	//call send advertisement observer one by one
	public void callSendAdvertisementObserverCallback(IMMessage im){
		for(SamchatObserver<IMMessage> observer : SendAdvertisementObservers){
			observer.onEvent(im);
		}
	}

	//call send advertisement status observer one by one
	public void callSendAdvertisementStatusObserverCallback(IMMessage im){
		for(SamchatObserver<IMMessage> observer : SendAdvertisementStatusObservers){
			observer.onEvent(im);
		}
	}

	public void clearUserTable(long unique_id){

	}

	public void handleReceivedQuestion(HttpCommClient hcc){
		final ReceivedQuestion rq = hcc.rq;
		//only unique_id/username/lastupdate in userinfo
		final ContactUser ui = hcc.userinfo;
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				boolean userinfo_need_update = false;
				ContactUser user = SamService.getInstance().getDao().query_ContactUser_db_by_unique_id(ui.getunique_id());
				if(user == null){
					user = new ContactUser();
					user.setunique_id(ui.getunique_id());
					user.setusername(ui.getusername());
					if(SamService.getInstance().getDao().update_ContactUser_db(user) == -1){
						LogUtil.e(TAG,"db warning : update user db error, drop this question");
						return;
					}
					userinfo_need_update = true;
					SamchatUserInfoCache.getInstance().addUser(user.getunique_id(),user);
				}else if(user.getlastupdate() != ui.getlastupdate()){
					userinfo_need_update = true;
				}

				if(SamService.getInstance().getDao().update_ReceivedQuestion_db(rq) == -1){
					LogUtil.e(TAG,"db warning : update received question db error, drop this question");
					return;
				}

				callReceivedQuestionObserverCallback(rq);

				if(userinfo_need_update){
					SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(user.getunique_id());
				}
				
			}
		});
	}


	//Message: message db
	//IMMessage: yunxin message

	//create message from IMMessage
	private List<Message> createMessages(String session,int msg_type,List<IMMessage> messages){
		if(messages ==  null || messages.size() == 0){
			return null;
		}
		
		List<Message> msgs = new ArrayList<Message>();
		for(IMMessage im : messages){
			msgs.add( new Message(msg_type, im.getUuid()));
			LogUtil.e("test","IMMessage received: "+im.getContent()+" uuid:"+im.getUuid());
		}
		return msgs;
	}
	
	private List<Message> createMessages(List<IMMessage> messages){
		if(messages ==  null || messages.size() == 0){
			return null;
		}
		
		List<Message> msgs = new ArrayList<Message>();
		for(IMMessage im : messages){
			Map<String, Object> content = im.getRemoteExtension();
			if(content != null && content.containsKey(NimConstants.SQ_QUEST_ID)){
				String data_id = (String)content.get(NimConstants.SQ_QUEST_ID);
				Message msg= new Message(NimConstants.MSG_TYPE_SQ,im.getUuid(),Long.valueOf(data_id));
				msgs.add(msg);
			}else{
				Message msg = new Message(NimConstants.MSG_TYPE_IM, im.getUuid());
				msgs.add(msg);
			}
			
		}
		return msgs;
	}

	//Message: message db
	//IMMessage: yunxin message

	//create message from IMMessage and question/adv
	private Message createMessage(String session,int msg_type,IMMessage im, long data_id){
		if(im == null){
			return null;
		}

		Message msg = new Message(msg_type, im.getUuid(), data_id);
		
		return msg;
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
			updateSessionBySendMsg(session, msg, im);
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

	private int numberOfIMMessage(List<Message> dbMsgs){
		int num = 0;
		for(Message msg:dbMsgs){
			if(msg.gettype() ==  NimConstants.MSG_TYPE_IM ){
				num++;
			}
		}

		return num;
	}

	private void updateSessionByRcvdMsg(MsgSession session, List<Message> dbMsgs, List<IMMessage> ims, boolean recordUnread){
		if(ims == null || ims.size() <=0 ){
			return;
		}

		if(recordUnread){
			session.settotal_unread(session.gettotal_unread()+numberOfIMMessage(dbMsgs));
		}
		session.setrecent_msg_type(dbMsgs.get(dbMsgs.size()-1).gettype());
		session.setrecent_msg_uuid(ims.get(ims.size() - 1).getUuid());
		session.setrecent_msg_subtype(ims.get(ims.size() - 1).getMsgType().getValue());
		session.setrecent_msg_content(ims.get(ims.size() - 1).getContent());
		session.setrecent_msg_time(ims.get(ims.size() - 1).getTime());
		session.setrecent_msg_status(NimConstants.MSG_STATUS_DEFAULT);
	}

	private void updateSessionBySendMsg(MsgSession session, Message msg,IMMessage im){
		if(im == null){
			return;
		}
		session.setrecent_msg_type(msg.gettype());
		session.setrecent_msg_uuid(im.getUuid());
		session.setrecent_msg_subtype(im.getMsgType().getValue());
		session.setrecent_msg_content(im.getContent());
		session.setrecent_msg_time(im.getTime());
		session.setrecent_msg_status(im.getStatus().getValue());
	}

	private void updateSessionByDeleteMsg(MsgSession session, IMMessage im,int msg_type){
		if(im == null){
			session.setrecent_msg_type(NimConstants.MSG_TYPE_IM);
			session.setrecent_msg_uuid(null);
			session.setrecent_msg_subtype(NimConstants.MSG_SUBTYPE_DEFAULT);
			session.setrecent_msg_content(null);
			session.setrecent_msg_time(0);
			session.setrecent_msg_status(NimConstants.MSG_STATUS_DEFAULT);
		}else{
			session.setrecent_msg_type(msg_type);
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
			updateSessionByRcvdMsg(session,dbMsgs,ims,recordUnread);
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
		final List<Message> msgs = createMessages(sessionId,NimConstants.MSG_TYPE_IM, ims);
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
		final List<Message> msgs = createMessages(sessionId,NimConstants.MSG_TYPE_IM, ims);
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
					updateSessionByDeleteMsg(session,null,NimConstants.MSG_TYPE_IM);
				}else{
					List<Message> tmsgs = new ArrayList<Message>(1);
					tmsgs.add(msgs.get(0));
					List<IMMessage> fims = createIMMessages(tmsgs);
					if(fims == null || fims.size() <= 0){
						updateSessionByDeleteMsg(session,null,NimConstants.MSG_TYPE_IM);
					}else{
						updateSessionByDeleteMsg(session, fims.get(0),tmsgs.get(0).gettype());
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
						int msg_from = (int)content.get(NimConstants.MSG_FROM);
						int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
						List<Message> msgs = createMessages(sessionId,NimConstants.MSG_TYPE_IM, ims);
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

	public void asyncQuerySendQuestion(final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				List<SendQuestion> sqs = SamService.getInstance().getDao().query_SendQuestion_db_ALL();
				callback.onResult(sqs, null, 0);
			}
		});	
	}

	public void asyncClearSendQuestionUnreadCount(final long question_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				SendQuestion  sq = SamService.getInstance().getDao().query_SendQuestion_db_by_question_id(question_id);
				if(sq!=null){
					sq.setunread(0);
					SamService.getInstance().getDao().update_SendQuestion_db(sq);
					callSendQuestionUnreadClearObserverCallback(sq);
				}
			}
		});	
	}

	public void asyncNoticeReceivedQuestionMessage(final ReceivedQuestion rq, final IMMessage im){
			final List<IMMessage> ims = new ArrayList<IMMessage>();
			ims.add(im);
			mFixedHttpThreadPool.execute(new Runnable(){
				@Override
				public void run(){
					MsgSession changedSession = SamService.getInstance().getDao().query_MsgSession_db(""+rq.getsender_unique_id(), ModeEnum.SP_MODE.ordinal());
					if(changedSession != null){
						//call session changed observer
						callMsgServiceObserverCallback(changedSession);
						//call incoming IMMessage observer
						callIncomingMsgObserverCallback(ims);
					}
				}
			});
		}

	private boolean syncStoreReceivedQuestionMessage(ReceivedQuestion rq, IMMessage im){
		Map<String, Object> content = im.getRemoteExtension();
		int msg_from = (int)content.get(NimConstants.MSG_FROM);
		int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
		Message msg = createMessage(im.getSessionId(),NimConstants.MSG_TYPE_RQ, im,rq.getquestion_id());

		List<IMMessage> ims = new ArrayList<IMMessage>();
		ims.add(im);
		List<Message> msgs = new ArrayList<Message>();
		msgs.add(msg);

		if(storeRcvdMessages(im.getSessionId(),mode,msgs,ims,false) != null){
			return true;		
		}
		
		return false;

	}

	public void asyncInsertReceivedQuestionMessage(final ReceivedQuestion rq){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(""+rq.getsender_unique_id(),ModeEnum.SP_MODE.ordinal());
				if(session!=null && SamService.getInstance().getDao().query_Message_db_by_type_data_id(session.getmsg_table_name(), NimConstants.MSG_TYPE_RQ, rq.getquestion_id())!=null){
					return;
				}
			
				final IMMessage im = SAMMessageBuilder.createReceivedQuestionMessage(rq);
				if(!syncStoreReceivedQuestionMessage(rq,im)){
					return;
				}
				
				NIMClient.getService(MsgService.class).saveMessageToLocal(im, false).setCallback(new RequestCallback<Void>() {
           		@Override
           		public void onSuccess(Void a) {
           			LogUtil.e(TAG,"saveMessageToLocal successful");
               		asyncNoticeReceivedQuestionMessage(rq, im);
            		}

            		@Override
            		public void onFailed(int code) {
						LogUtil.e(TAG,"saveMessageToLocal failed:"+code);
					}

            		@Override
            		public void onException(Throwable exception) {
						LogUtil.e(TAG,"saveMessageToLocal exception:"+exception);
					}
        		});
			}
		});	
	}

	public void asyncStoreSendAdvertisementMessage(final Advertisement adv, final IMMessage im, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				Message msg = createMessage(im.getSessionId(), NimConstants.MSG_TYPE_SEND_ADV,  im, adv.getadv_id());
				MsgSession changedSession;
				if((changedSession = storeSendMessage(im.getSessionId(),ModeEnum.SP_MODE.ordinal(),msg,im)) != null){
					callSendAdvertisementObserverCallback(im);
					callback.onResult(adv, im, 0);
				}
			}
		});	
	}

	public void syncUpdateSendAdvertisementMessage(final Advertisement adv, final IMMessage im){
		if(im.getStatus() == MsgStatusEnum.success){
			MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT,ModeEnum.SP_MODE.ordinal());
			if(SamService.getInstance().getDao().updateMessageDataID(session.getmsg_table_name(), im.getUuid(), adv.getadv_id())!=0){
				NIMClient.getService(MsgService.class).updateIMMessageStatus(im);
				callSendAdvertisementStatusObserverCallback(im);
			}else{
				im.setStatus(MsgStatusEnum.fail);
				NIMClient.getService(MsgService.class).updateIMMessageStatus(im);
				callSendAdvertisementStatusObserverCallback(im);
			}
		}else{
			NIMClient.getService(MsgService.class).updateIMMessageStatus(im);
			callSendAdvertisementStatusObserverCallback(im);
		}
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
			uuids.add(m.getuuid());
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

	public void asyncUpdateReceivedQuestionStatusToResponse(final long question_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				ReceivedQuestion rq = SamService.getInstance().getDao().query_ReceivedQuestion_db_by_question_id(question_id);
				if(rq != null){
					rq.setstatus(Constants.QUESTION_RESPONSED);
					SamService.getInstance().getDao().update_ReceivedQuestion_db(rq);
					callReceivedQuestionObserverCallback(rq);
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

	private boolean isIdExisted(String ids, String id){
		if(ids == null){
			return false;
		}

		String []  array = ids.split(":");
		for(String i:array){
			if(i.equals(id)){
				return true;
			}
		}

		return false;

	}

	private List<AdvancedMessage> handleQuestId(List<IMMessage> ims){
        List<AdvancedMessage> result = new ArrayList<>();
		if(ims == null || ims.size() == 0){
			return result;
		}
		for(IMMessage m:ims){
			Map<String, Object> content = m.getRemoteExtension();
			if(content != null && content.containsKey(NimConstants.QUEST_ID)){
				String quest_id = (String)content.get(NimConstants.QUEST_ID);
				SendQuestion sq = SamService.getInstance().getDao().query_SendQuestion_db_by_question_id(Long.valueOf(quest_id));
				if(sq == null){
					continue;
				}
				
				if(sq.getsp_ids() == null){
					sq.setsp_ids(m.getSessionId());
					sq.setunread(sq.getunread()+1);
					result.add(new AdvancedMessage(null, m, sq));
				}else if(!isIdExisted(sq.getsp_ids(),m.getSessionId())){
					sq.setsp_ids(sq.getsp_ids()+":"+m.getSessionId());
					sq.setunread(sq.getunread()+1);
					result.add(new AdvancedMessage(null, m, sq));
				}
				sq.setlatest_answer_time(m.getTime());
				SamService.getInstance().getDao().update_SendQuestion_db(sq);
			}
		}

		return result;
	}

	private void saveSQimsToMsgDB(final MsgSession changedSession, final List<IMMessage> ims_customer_mode){
		for(IMMessage im: ims_customer_mode){
			Map<String, Object> content = im.getRemoteExtension();
			if(content != null && content.containsKey(NimConstants.SQ_QUEST_ID)){
				NIMClient.getService(MsgService.class).saveMessageToLocal(im, false).setCallback(new RequestCallback<Void>() {
           		@Override
           		public void onSuccess(Void a) {
						//call session changed observer
						callMsgServiceObserverCallback(changedSession);
						//call incoming IMMessage observer
						callIncomingMsgObserverCallback(ims_customer_mode);
					}

					@Override
					public void onFailed(int code) {

					}

					@Override
					public void onException(Throwable exception) {

					}
				});
			}
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
						List<IMMessage> valid_ims = new ArrayList<IMMessage>();
						for(IMMessage m:ims){
							Map<String, Object> content = m.getRemoteExtension();
							if(content != null && content.containsKey(Constants.MSG_FROM)){
								valid_ims.add(m);
							}
						}
						if(valid_ims.size() == 0){
							return;
						}
			
						/*parse question id if existed*/
						List<AdvancedMessage> ammsgs = handleQuestId(valid_ims);
						boolean sq_im_existed = false;
						/*insert send question IMMessage into yunxin msg db*/
						int index = -1;
						for(AdvancedMessage am : ammsgs){
							index = -1;
							for(int i=0; i<valid_ims.size();i++){
								if(am.getim().getUuid().equals(valid_ims.get(i).getUuid())){
										index = i;
										break;
								}
							}
							if(index > -1){
								valid_ims.add(index,SAMMessageBuilder.createSendQuestionMessage(am.getsq(), am.getim()));
								sq_im_existed = true;
							}
						}
						
						List<Message> msgs_customer_mode = new ArrayList<Message>();
						List<IMMessage> ims_customer_mode = new ArrayList<IMMessage>();
						List<Message> msgs_sp_mode = new ArrayList<Message>();
						List<IMMessage> ims_sp_mode = new ArrayList<IMMessage>();

						List<Message> dbmsgs = createMessages(valid_ims);

						for(int i=0;i<valid_ims.size();i++){
							Map<String, Object> content = valid_ims.get(i).getRemoteExtension();
							int msg_from = (int)content.get(Constants.MSG_FROM);
							int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
							if(mode == ModeEnum.CUSTOMER_MODE.ordinal()){
								ims_customer_mode.add(valid_ims.get(i));
								msgs_customer_mode.add(dbmsgs.get(i));
							}else{
								ims_sp_mode.add(valid_ims.get(i));
								msgs_sp_mode.add(dbmsgs.get(i));
							}
						}

						MsgSession changedSession;
						if(ims_customer_mode.size()>0 && (changedSession = storeRcvdMessages(sessionId,ModeEnum.CUSTOMER_MODE.ordinal(),msgs_customer_mode,ims_customer_mode,true)) != null){
							if(!sq_im_existed){
								//call session changed observer
								callMsgServiceObserverCallback(changedSession);
								//call incoming IMMessage observer
								callIncomingMsgObserverCallback(ims_customer_mode);
							}else{
								saveSQimsToMsgDB(changedSession, ims_customer_mode);
							}
						}
						if(ims_sp_mode.size()>0 && (changedSession = storeRcvdMessages(sessionId,ModeEnum.SP_MODE.ordinal(),msgs_sp_mode,ims_sp_mode,true)) != null){
							//call session changed observer
							callMsgServiceObserverCallback(changedSession);
							//call incoming IMMessage observer
							callIncomingMsgObserverCallback(ims_sp_mode);
						}
						
					}
			});
		}
	};

	synchronized public void registerMsgSessionObserver(SamchatObserver<MsgSession> observer,boolean register){
		if(register){
			MsgSessionObservers.add(observer);
		}else{
			MsgSessionObservers.remove(observer);
		}
	}

	synchronized public void registerIncomingMsgObserver(SamchatObserver<List<IMMessage>> observer,boolean register){
		if(register){
			IncomingMsgObservers.add(observer);
		}else{
			IncomingMsgObservers.remove(observer);
		}
	}

	synchronized public void registerSendCustomerMsgObserver(SamchatObserver<IMMessage> observer,boolean register){
		if(register){
			SendCustomerMsgObservers.add(observer);
		}else{
			SendCustomerMsgObservers.remove(observer);
		}
	}

	synchronized public void registerReceivedQuestionObserver(SamchatObserver<ReceivedQuestion> observer,boolean register){
		if(register){
			ReceivedQuestionObservers.add(observer);
		}else{
			ReceivedQuestionObservers.remove(observer);
		}
	}

	synchronized public void registerSendQuestionAnsweredObserver(SamchatObserver<List<SendQuestion>> observer,boolean register){
		if(register){
			SendQuestionAnsweredObservers.add(observer);
		}else{
			SendQuestionAnsweredObservers.remove(observer);
		}
	}

	synchronized public void registerSendQuestionUnreadClearObserver(SamchatObserver<SendQuestion> observer,boolean register){
		if(register){
			SendQuestionUnreadClearObservers.add(observer);
		}else{
			SendQuestionUnreadClearObservers.remove(observer);
		}
	}

	synchronized public void registerSendAdvertisementObserver(SamchatObserver<IMMessage> observer,boolean register){
		if(register){
			SendAdvertisementObservers.add(observer);
		}else{
			SendAdvertisementObservers.remove(observer);
		}
	}

	synchronized public void registerSendAdvertisementStatusObserver(SamchatObserver<IMMessage> observer,boolean register){
		if(register){
			SendAdvertisementStatusObservers.add(observer);
		}else{
			SendAdvertisementStatusObservers.remove(observer);
		}
	}

};
