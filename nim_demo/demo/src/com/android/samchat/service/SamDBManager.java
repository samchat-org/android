package com.android.samchat.service;

import com.android.samchat.cache.MsgSessionDataCache;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.common.SamchatFileNameUtils;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.AdvancedMessage;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.MsgSession;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import com.android.samservice.info.Message;
import com.android.samservice.info.RcvdAdvSession;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.cache.SendIMMessageCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.session.sam_message.SessionBasicInfo;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import java.util.Map;
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

/*DB time-consumption operation should be handle by this class used by UI*/
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
	//received question unread clear observer: SamchatRequestFragment of customer mode will register
	private List<SamchatObserver<ReceivedQuestion>> ReceivedQuestionUnreadClearObservers;
	//send adv observer: SamchatPublicFragment of sp mode will register
	private List<SamchatObserver<IMMessage>> SendAdvertisementObservers;
	//send adv status update observer: SamchatPublicFragment of sp mode will register
	private List<SamchatObserver<IMMessage>> SendAdvertisementStatusObservers;
	//received adv session observer: SamchatPublicFragment of customer mode will register
	private List<SamchatObserver<RcvdAdvSession>> RcvdAdvSessionObservers;
	//received adv session unread clear observer: SamchatPublicFragment of customer mode will register
	private List<SamchatObserver<RcvdAdvSession>> ReceivedAdvertisementUnreadClearObservers;
	//received adv observer: Advertisement fragment mode will register
	private List<SamchatObserver<Advertisement>> RcvdAdvObservers;
	//chat history clear observer: message fragment will register
	private List<SamchatObserver<SessionBasicInfo>> ClearHistoryObservers;
	
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
		ReceivedQuestionUnreadClearObservers = new ArrayList<SamchatObserver<ReceivedQuestion>>();
		SendAdvertisementObservers = new ArrayList<SamchatObserver<IMMessage>>();
		SendAdvertisementStatusObservers = new ArrayList<SamchatObserver<IMMessage>>();
		ReceivedAdvertisementUnreadClearObservers = new ArrayList<SamchatObserver<RcvdAdvSession>>();
		RcvdAdvSessionObservers = new ArrayList<SamchatObserver<RcvdAdvSession>>();
		RcvdAdvObservers = new ArrayList<SamchatObserver<Advertisement>>();
		ClearHistoryObservers = new ArrayList<SamchatObserver<SessionBasicInfo>>();
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

	//call send question unread clear observer one by one
	public void callReceivedQuestionUnreadClearObserverCallback(ReceivedQuestion rq){
		for(SamchatObserver<ReceivedQuestion> observer : ReceivedQuestionUnreadClearObservers){
			observer.onEvent(rq);
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

	//call rcvd advertisement session observer one by one
	public void callRcvdAdvSessionObserversObserverCallback(RcvdAdvSession session){
		for(SamchatObserver<RcvdAdvSession> observer : RcvdAdvSessionObservers){
			observer.onEvent(session);
		}
	}

	//call rcvd advertisement observer one by one
	public void callRcvdAdvObserversObserverCallback(Advertisement adv){
		for(SamchatObserver<Advertisement> observer : RcvdAdvObservers){
			observer.onEvent(adv);
		}
	}
	
	//call received advertisement unread clear observer one by one
	public void callReceivedAdvertisementUnreadClearObserverCallback(RcvdAdvSession session){
		for(SamchatObserver<RcvdAdvSession> observer : ReceivedAdvertisementUnreadClearObservers){
			observer.onEvent(session);
		}
	}

	//call chat history clear observer one by one
	public void callClearHistoryObserverCallback(SessionBasicInfo sinfo){
		for(SamchatObserver<SessionBasicInfo> observer : ClearHistoryObservers){
			observer.onEvent(sinfo);
		}
	}


	
	
/*********************************************************************************************/
	public void clearUserTable(long unique_id){

	}

	public void handleReceivedQuestion(HttpCommClient hcc){
		final ReceivedQuestion rq = hcc.rq;
		//only unique_id/username/lastupdate in userinfo
		final ContactUser ui = hcc.userinfo;
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					boolean userinfo_need_update = false;
					ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ui.getunique_id());
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
						LogUtil.i(TAG,"user info update:"+ui.getAccount());
						SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(user.getunique_id());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	private void updateSessionByRcvdAdv(RcvdAdvSession session, Advertisement adv){
		if(adv.getpublish_timestamp() >= session.getrecent_adv_publish_timestamp()){
			session.setrecent_adv_id(adv.getadv_id());
			session.setrecent_adv_type(adv.gettype());
			session.setrecent_adv_content(adv.getcontent());
			session.setrecent_adv_publish_timestamp(adv.getpublish_timestamp());
			session.setrecent_adv_content_thumb(adv.getcontent_thumb());
		}
		session.setunread(session.getunread()+1);
	}


	private RcvdAdvSession storeRcvdAdvertisement(Advertisement adv){
		RcvdAdvSession session = SamService.getInstance().getDao().query_RcvdAdvSession_db(adv.getsender_unique_id());
		if(session == null){
			String table_name = "rcvdadvdb_"+StringUtil.makeMd5(""+adv.getsender_unique_id());
			session = new RcvdAdvSession(adv.getsender_unique_id(),table_name);
			SamService.getInstance().getDao().createRcvdAdvTable(table_name);
		}

		long rc = SamService.getInstance().getDao().add_RcvdAdv_db(session.getname(),adv);
		if(rc != -1){
			updateSessionByRcvdAdv(session,adv);
			if(SamService.getInstance().getDao().update_RcvdAdvSession_db(session)!= -1){
				return session;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	private boolean saveFile(String dest,byte[] data){
		File filePath = null;
		File file = null;
		FileOutputStream fos = null;

		try{
			file = new File(dest);

			if(!file.exists()){
				file.createNewFile();
			}
  
			fos = new FileOutputStream(file);    
  
			fos.write(data); 

			return true;
  
		}catch(Exception e){
			return false;
			
		}finally{
			try{
				if(fos!=null) fos.close();
			}catch(Exception e){

			}

		}

	}
	
	public void handleReceivedAdvertisement(HttpCommClient hcc){
		final Advertisement adv = hcc.adv;
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					if(adv.gettype() == Constants.ADV_TYPE_TEXT){
						RcvdAdvSession changedSession = storeRcvdAdvertisement(adv);
						if(changedSession != null){
							//call rcvd adv session changed observer
							callRcvdAdvSessionObserversObserverCallback(changedSession);
							//call incoming adv observer
							callRcvdAdvObserversObserverCallback(adv);
						}
					}else if(adv.gettype() == Constants.ADV_TYPE_PIC){
						LogUtil.i(TAG,"received adv:" + adv.getcontent_thumb());
						String MD5Path = SamchatFileNameUtils.getMD5Path(StorageType.TYPE_THUMB_IMAGE,adv.getcontent_thumb());
						SamService.getInstance().download(adv.getcontent_thumb(),  MD5Path, new SMCallBack(){
							@Override
							public void onSuccess(final Object obj, final int WarningCode) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" successfully");
								RcvdAdvSession changedSession = storeRcvdAdvertisement(adv);
								if(changedSession != null){
									//call rcvd adv session changed observer
									callRcvdAdvSessionObserversObserverCallback(changedSession);
									//call incoming adv observer
									callRcvdAdvObserversObserverCallback(adv);
								}
							}
							@Override
							public void onFailed(int code) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" failed");
							}
							@Override
							public void onError(int code) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" error");
							}
						});
					}else{
						LogUtil.i(TAG,"received adv:" + adv.getcontent_thumb());
						String MD5Path = SamchatFileNameUtils.getMD5Path(StorageType.TYPE_THUMB_VIDEO,adv.getcontent_thumb());
						SamService.getInstance().download(adv.getcontent_thumb(), MD5Path, new SMCallBack(){
							@Override
							public void onSuccess(final Object obj, final int WarningCode) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" successfully");
								RcvdAdvSession changedSession = storeRcvdAdvertisement(adv);
								if(changedSession != null){
									//call rcvd adv session changed observer
									callRcvdAdvSessionObserversObserverCallback(changedSession);
									//call incoming adv observer
									callRcvdAdvObserversObserverCallback(adv);
								}
							}
							@Override
							public void onFailed(int code) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" failed");
							}
							@Override
							public void onError(int code) {
								LogUtil.i(TAG,"download:" + adv.getcontent_thumb()+" error");
							}
						});
					}
				}catch(Exception e){
					e.printStackTrace();
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
			}else if(content != null && content.containsKey(NimConstants.SA_ADV_ID)){
				String data_id = (String)content.get(NimConstants.SA_ADV_ID);
				Message msg = new Message(NimConstants.MSG_TYPE_SEND_ADV,im.getUuid(),Long.valueOf(data_id));
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
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		long rc = SamService.getInstance().getDao().add_Message_db(session.getmsg_table_name(), msg);
		
		if(rc != -1){
			updateSessionBySendMsg(session, msg, im);
			MsgSession dbSession = new MsgSession(session);
			dbSession.setrecent_msg_status(MsgStatusEnum.fail.getValue());
			if(SamService.getInstance().getDao().update_MsgSession_db(dbSession) != -1){
				return session;
			}else{
				return null;
			}
		}else{
			return null;
		}
		
	}

	private int numberOfIMMessage(List<IMMessage> ims){
		int num = 0;
		for(IMMessage im : ims){
			Map<String, Object> content = im.getRemoteExtension();
			if(content == null || (!content.containsKey(NimConstants.QUEST_ID) && !content.containsKey(NimConstants.SQ_QUEST_ID))){
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
			session.settotal_unread(session.gettotal_unread()+numberOfIMMessage(ims));
		}
		session.setrecent_msg_type(dbMsgs.get(dbMsgs.size()-1).gettype());
		session.setrecent_msg_uuid(ims.get(ims.size() - 1).getUuid());
		session.setrecent_msg_subtype(ims.get(ims.size() - 1).getMsgType().getValue());
		session.setrecent_msg_content(ims.get(ims.size() - 1).getContent());

		session.setrecent_msg_time(ims.get(ims.size() - 1).getTime());
		session.setrecent_msg_status(MsgStatusEnum.success.getValue());
		MsgSessionDataCache.getInstance().addMsgSession( session.getsession_id(),  session.getmode(), session);
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
		MsgSessionDataCache.getInstance().addMsgSession( session.getsession_id(),  session.getmode(), session);
	}

	private void updateSessionByDeleteMsg(MsgSession session, IMMessage im,int msg_type){
		if(im == null){
			session.setrecent_msg_type(NimConstants.MSG_TYPE_IM);
			session.setrecent_msg_uuid(null);
			session.setrecent_msg_subtype(MsgTypeEnum.undef.getValue());
			session.setrecent_msg_content(null);
			session.setrecent_msg_time(0);
			session.setrecent_msg_status(MsgStatusEnum.success.getValue());
		}else{
			session.setrecent_msg_type(msg_type);
			session.setrecent_msg_uuid(im.getUuid());
			session.setrecent_msg_subtype(im.getMsgType().getValue());
			session.setrecent_msg_content(im.getContent());
			session.setrecent_msg_time(im.getTime());
			session.setrecent_msg_status(im.getStatus().getValue());
		}
		MsgSessionDataCache.getInstance().addMsgSession( session.getsession_id(),  session.getmode(), session);
	}

	private void updateSessionByClearHisotry(MsgSession session){
		session.settotal_unread(0);
		session.setrecent_msg_type(NimConstants.MSG_TYPE_IM);
		session.setrecent_msg_uuid(null);
		session.setrecent_msg_subtype(MsgTypeEnum.undef.getValue());
		session.setrecent_msg_content(null);
		session.setrecent_msg_time(0);
		session.setrecent_msg_status(MsgStatusEnum.success.getValue());
		MsgSessionDataCache.getInstance().addMsgSession( session.getsession_id(),  session.getmode(), session);
	}
	
	private MsgSession storeRcvdMessages(String session_id, int mode, List<Message> dbMsgs, List<IMMessage> ims, boolean recordUnread){
		SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getAccount()));
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
		if(session == null){
			String msg_table_name = "msgdb_"+StringUtil.makeMd5(session_id)+"_"+mode;//StringUtil.makeMd5(session_id)+"_"+mode;
			session = new MsgSession( session_id,  mode,  msg_table_name);
			SamService.getInstance().getDao().createMsgTable(msg_table_name);
		}

		int rc = SamService.getInstance().getDao().add_Messages_db(session.getmsg_table_name(), dbMsgs);
		if(rc != -1){
			updateSessionByRcvdMsg(session,dbMsgs,ims,recordUnread);
			if(SamService.getInstance().getDao().update_MsgSession_db(session) != -1){
				return session;
			}else{
				return null;
			}
		}else{
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
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession changedSession;
					if((changedSession = storeSendMessage(sessionId,mode,msgs.get(0),im)) != null){
 						callback.onResult(im,null,0);
						callMsgServiceObserverCallback(changedSession);
					}else{
						callback.onResult(null,null,-1);
					}
				}catch(Exception e){
					e.printStackTrace();
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
			try{
					MsgSession changedSession;
					if((changedSession = storeSendMessage(sessionId,mode,msgs.get(0),im)) != null){
 						callback.onResult(im,null,0);
						callMsgServiceObserverCallback(changedSession);
						callSendCustomerMsgObserverCallback(im);
					}else{
						callback.onResult(null,null,-1);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}


	public void asyncDeleteMessage(final String session_id, final int mode, final IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
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
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void syncDeleteMessageSession(final String session_id, final int mode){
		MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
		if(session == null){
			return;
		}
		String table = session.getmsg_table_name();
		MsgSessionDataCache.getInstance().removeMsgSession( session_id,  mode);
		SamService.getInstance().getDao().delete_MsgSession_db(session_id, mode);
		SamService.getInstance().getDao().delete_Message_db_all(table);
	}

	public void asyncDeleteMessageSession(final String session_id, final int mode){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
					if(session == null){
						return;
					}
					String table = session.getmsg_table_name();
					MsgSessionDataCache.getInstance().removeMsgSession( session_id,  mode);
					SamService.getInstance().getDao().delete_MsgSession_db(session_id, mode);
					SamService.getInstance().getDao().delete_Message_db_all(table);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncUpdateMessageSessionDBRecentMessageStatus(final String session_id, final int mode, final int status){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					SamService.getInstance().getDao().update_MsgSession_db_recent_status( session_id,  mode,  status);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncStoreRecvCustomerMessages(final List<IMMessage> ims){
			mFixedHttpThreadPool.execute(new Runnable(){
				@Override
				public void run(){
					try{
						if(ims == null || ims.size() == 0){
							return;
						}
						SessionTypeEnum sessionType = ims.get(0).getSessionType();
						String sessionId = ims.get(0).getSessionId();
						if(sessionType !=  SessionTypeEnum.P2P){
							return;
						}

						Map<String, Object> content = ims.get(0).getRemoteExtension();
						if(content == null || !content.containsKey(NimConstants.MSG_FROM)){
							return;
						}

						int msg_from = (int)content.get(NimConstants.MSG_FROM);
						int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
						List<Message> msgs = createMessages(sessionId,NimConstants.MSG_TYPE_IM, ims);
                 	 	MsgSession changedSession;
						if((changedSession = storeRcvdMessages(sessionId,mode,msgs,ims,false)) != null){
							//call session changed observer
							callMsgServiceObserverCallback(changedSession);
							//call incoming IMMessage observer
							callIncomingMsgObserverCallback(ims);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
		});
	}

	public void asyncClearUnreadCount(final String session_id, final int mode){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = MsgSessionDataCache.getInstance().getMsgSession( session_id,  mode);
					if(session != null){
						session.settotal_unread(0);
					}
					if(SamService.getInstance().getDao().update_MsgSession_db_unread_count(session_id, mode, 0)!=-1){
						MsgSession changedSession = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
						if(changedSession !=null){
							callMsgServiceObserverCallback(changedSession);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});		
	}

	public void asyncReadTotalUnreadAdvertisementCount(final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					List<RcvdAdvSession> sessions = SamService.getInstance().getDao().query_RcvdAdvSession_db_All();
					int total_unread = 0;
					for(RcvdAdvSession s:sessions){
						total_unread = total_unread + s.getunread();
					}
					callback.onResult(new Integer(total_unread),null,0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncReadTotalUnreadCount(final int mode, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					List<MsgSession> sessions = SamService.getInstance().getDao().query_MsgSession_db(mode);
					int total_unread = 0;
					for(MsgSession s:sessions){
						total_unread = total_unread + s.gettotal_unread();
					}
					callback.onResult(new Integer(total_unread),new Integer(mode),0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncQuerySendQuestion(final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					List<SendQuestion> sqs = SamService.getInstance().getDao().query_SendQuestion_db_ALL();
					callback.onResult(sqs, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncClearSendQuestionUnreadCount(final long question_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					SendQuestion  sq = SamService.getInstance().getDao().query_SendQuestion_db_by_question_id(question_id);
					if(sq!=null){
						sq.setunread(0);
						SamService.getInstance().getDao().update_SendQuestion_db(sq);
						callSendQuestionUnreadClearObserverCallback(sq);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncClearReceivedQuestionUnreadCount(final long question_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					ReceivedQuestion rq = SamService.getInstance().getDao().query_ReceivedQuestion_db_by_question_id(question_id);
					if(rq!=null){
						rq.setunread(Constants.QUESTION_READ);
						SamService.getInstance().getDao().update_ReceivedQuestion_db(rq);
						callReceivedQuestionUnreadClearObserverCallback(rq);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncClearReceivedAdvertisementUnreadCount(final long unique_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					RcvdAdvSession session = SamService.getInstance().getDao().query_RcvdAdvSession_db(unique_id);
					if(session!=null){
						session.setunread(0);
						SamService.getInstance().getDao().update_RcvdAdvSession_db(session);
						callRcvdAdvSessionObserversObserverCallback(session);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	private void syncNoticeReceivedQuestionMessage(final ReceivedQuestion rq, final IMMessage im){
		final List<IMMessage> ims = new ArrayList<IMMessage>();
		ims.add(im);
		MsgSession changedSession = SamService.getInstance().getDao().query_MsgSession_db(""+rq.getsender_unique_id(), ModeEnum.SP_MODE.ordinal());
			if(changedSession != null){
				//call session changed observer
				callMsgServiceObserverCallback(changedSession);
				//call incoming IMMessage observer
				callIncomingMsgObserverCallback(ims);
			}
	}

	private void asyncStoreReceivedQuestionMessage(final ReceivedQuestion rq, final IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					Map<String, Object> content = im.getRemoteExtension();
					int msg_from = (int)content.get(NimConstants.MSG_FROM);
					int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
					Message msg = createMessage(im.getSessionId(),NimConstants.MSG_TYPE_RQ, im,rq.getquestion_id());
					List<IMMessage> ims = new ArrayList<IMMessage>();
					ims.add(im);
					List<Message> msgs = new ArrayList<Message>();
					msgs.add(msg);
					storeRcvdMessages(im.getSessionId(),mode,msgs,ims,false);
					syncNoticeReceivedQuestionMessage(rq, im);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncInsertReceivedQuestionMessage(final ReceivedQuestion rq){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(""+rq.getsender_unique_id(),ModeEnum.SP_MODE.ordinal());
					if(session!=null && SamService.getInstance().getDao().query_Message_db_by_type_data_id(session.getmsg_table_name(), NimConstants.MSG_TYPE_RQ, rq.getquestion_id())!=null){
						return;
					}
			
					final IMMessage im = SAMMessageBuilder.createReceivedQuestionMessage(rq);
					NIMClient.getService(MsgService.class).saveMessageToLocal(im, false).setCallback(new RequestCallback<Void>() {
           			@Override
           			public void onSuccess(Void a) {
           				LogUtil.e(TAG,"saveMessageToLocal successful");
							asyncStoreReceivedQuestionMessage(rq, im);
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
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncStoreSendAdvertisementMessage(final IMMessage im, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					Message msg = createMessage(im.getSessionId(), NimConstants.MSG_TYPE_SEND_ADV,  im, 0L);
					MsgSession changedSession;
					if((changedSession = storeSendMessage(im.getSessionId(),ModeEnum.SP_MODE.ordinal(),msg,im)) != null){
						callSendAdvertisementObserverCallback(im);
						callback.onResult(im,null, 0);
					}else{
						callback.onResult(im,null, -1);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncUpdateSendAdvertisementMessage(final Advertisement adv, final IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					if(im.getStatus() == MsgStatusEnum.success){
						MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(im.getSessionId(), ModeEnum.SP_MODE.ordinal());
						if(SamService.getInstance().getDao().updateMessageDataID(session.getmsg_table_name(), im.getUuid(), adv.getadv_id())!=-1){
							NIMClient.getService(MsgService.class).updateIMMessageStatus(im);
							callSendAdvertisementStatusObserverCallback(im);
							session.setrecent_msg_status(im.getStatus().getValue());
							SamDBManager.getInstance().asyncUpdateMessageSessionDBRecentMessageStatus(im.getSessionId(), ModeEnum.SP_MODE.ordinal(),im.getStatus().getValue());
						}else{
							im.setStatus(MsgStatusEnum.fail);
							callSendAdvertisementStatusObserverCallback(im);
						}
					}else{
						callSendAdvertisementStatusObserverCallback(im);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}


	

	private void asyncStoreReceivedAdvertisementMessage(final Advertisement adv, final IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					Map<String, Object> content = im.getRemoteExtension();
					int msg_from = (int)content.get(NimConstants.MSG_FROM);
					int mode = (msg_from == Constants.FROM_CUSTOMER ? ModeEnum.SP_MODE.ordinal():ModeEnum.CUSTOMER_MODE.ordinal());
					Message msg = createMessage(im.getSessionId(),NimConstants.MSG_TYPE_RCVD_ADV, im,adv.getadv_id());

					List<IMMessage> ims = new ArrayList<IMMessage>();
					ims.add(im);
					List<Message> msgs = new ArrayList<Message>();
					msgs.add(msg);
					List<Advertisement> advs = new ArrayList<Advertisement>();
					storeRcvdMessages(im.getSessionId(),mode,msgs,ims,false);
					syncNoticeRcvdAdvMessage(adv,im);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	private void syncNoticeRcvdAdvMessage( Advertisement adv,  IMMessage im){
		final List<IMMessage> ims = new ArrayList<IMMessage>();
		ims.add(im);
		MsgSession changedSession = SamService.getInstance().getDao().query_MsgSession_db(""+adv.getsender_unique_id(), ModeEnum.CUSTOMER_MODE.ordinal());
		if(changedSession != null){
			//call session changed observer
			callMsgServiceObserverCallback(changedSession);
			//call incoming IMMessage observer
			callIncomingMsgObserverCallback(ims);
		}
	}

	public void asyncInsertRcvdAdvMessage(final Advertisement adv){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(""+adv.getsender_unique_id(),ModeEnum.CUSTOMER_MODE.ordinal());
					if(session!=null && SamService.getInstance().getDao().query_Message_db_by_type_data_id(session.getmsg_table_name(), NimConstants.MSG_TYPE_RCVD_ADV, adv.getadv_id())!=null){
						return;
					}

					final IMMessage im = SAMMessageBuilder.createReceivedAdvertisementMessage(adv);
				
					NIMClient.getService(MsgService.class).saveMessageToLocal(im, false).setCallback(new RequestCallback<Void>() {
           			@Override
           			public void onSuccess(Void a) {
           				LogUtil.e(TAG,"saveMessageToLocal successful");
							asyncStoreReceivedAdvertisementMessage(adv,im);
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
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncDeleteSendAdvertisementMessage(final String session_id,final int mode,final IMMessage msg){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(session_id,  mode);
					if(session == null){
						return;
					}

					String table = session.getmsg_table_name();
					Message dbMsg = SamService.getInstance().getDao().query_Message_db_by_uuid( table, msg.getUuid());
					if(dbMsg != null && dbMsg.getdata_id() != 0){
						SamService.getInstance().getDao().delete_SamProsAdv_db_by_adv_id(dbMsg.getdata_id());
					}
					SamService.getInstance().getDao().delete_Message_db(table, msg.getUuid());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncNoticeLastMsgSending(final String account, final int mode, IMMessage im){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(account,  mode);
					if(session != null){
						session.setrecent_msg_status(MsgStatusEnum.sending.getValue());
						callMsgServiceObserverCallback(session);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncQueryAllRecvAdvSession(final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					List<RcvdAdvSession> sessions = SamService.getInstance().getDao().query_RcvdAdvSession_db_All();
					callback.onResult(sessions, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncQuerySendQuestionByID(final long question_id, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					SendQuestion sq = SamService.getInstance().getDao().query_SendQuestion_db_by_question_id(question_id);
					callback.onResult(sq, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncDeleteSendQuestionByID(final long question_id, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					SamService.getInstance().getDao().delete_SendQuestion_db_by_question_id(question_id);
					callback.onResult(null, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncDeleteReceivedQuestionByID(final long question_id, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					SamService.getInstance().getDao().delete_ReceivedQuestion_db_by_question_id(question_id);
					callback.onResult(null, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncQueryReceivedQuestionByTimeStamp(final long timestamp,final boolean after, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					List<ReceivedQuestion> rqs = SamService.getInstance().getDao().query_ReceivedQuestion_db_by_timestamp(timestamp,after);
					callback.onResult(rqs, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncClearChatHisotry(final SessionTypeEnum type, final String account, final int mode, final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					if(type == SessionTypeEnum.P2P){
						int opposite_mode = (mode==ModeEnum.CUSTOMER_MODE.getValue())?ModeEnum.SP_MODE.getValue():ModeEnum.CUSTOMER_MODE.getValue();
						MsgSession session = SamService.getInstance().getDao().query_MsgSession_db(account,mode);
						if(session == null){
							callback.onResult(null, null, NIMCallback.SUCCEED);
							return;
						}
						updateSessionByClearHisotry(session);
						String table = session.getmsg_table_name();
						SamService.getInstance().getDao().clear_Message_db_all(table);
						if(SamService.getInstance().getDao().query_MsgSession_db(account, opposite_mode)==null){
							NIMClient.getService(MsgService.class).clearChattingHistory(account, type);
						}
						callClearHistoryObserverCallback(new SessionBasicInfo(type,account,mode));
					}else{
						NIMClient.getService(MsgService.class).clearChattingHistory(account, type);
						callClearHistoryObserverCallback(new SessionBasicInfo(type,account,mode));
					}

					callback.onResult(null, null, NIMCallback.SUCCEED);
				}catch(Exception e){
					e.printStackTrace();
					callback.onResult(null, null, NIMCallback.EXCEPTION);
				}
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
				if(im.getSessionType() == SessionTypeEnum.P2P && im.getDirect()== MsgDirectionEnum.Out && SendIMMessageCache.getInstance().contains(im.getUuid())){
					im.setStatus(MsgStatusEnum.sending);
				}
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
				try{
					List<IMMessage> ims = new ArrayList<IMMessage>();
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
					if(session == null){
						callback.onResult(ims,null,-1);
						return;
					}

					String table = session.getmsg_table_name();
					List<Message> messages=null;
					if(im == null){
						messages = SamService.getInstance().getDao().query_Messages_db_Newest(table,  count);
					}else{
						Message tmsg = SamService.getInstance().getDao().query_Message_db_by_uuid(session.getmsg_table_name(),im.getUuid());
						if(tmsg == null){
							callback.onResult(ims,null,-1);
							return;
						}
						messages = SamService.getInstance().getDao().query_Messages_db_by_anchor( table, tmsg.getid(), count);
					}

					if(messages == null || messages.size() <=0){
						callback.onResult(ims,null,0);
					}else{
						ims = createIMMessages(messages);
						callback.onResult(ims, null ,0);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		
	}

	public void queryMsgSession(final String session_id, final int mode ,final NIMCallback callback){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					MsgSession session = SamService.getInstance().getDao().query_MsgSession_db( session_id,  mode);
					callback.onResult(session, null, 0);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});	
	}

	public void asyncUpdateReceivedQuestionStatusToResponse(final long question_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					ReceivedQuestion rq = SamService.getInstance().getDao().query_ReceivedQuestion_db_by_question_id(question_id);
					if(rq != null){
						rq.setstatus(Constants.QUESTION_RESPONSED);
						SamService.getInstance().getDao().update_ReceivedQuestion_db(rq);
						callReceivedQuestionObserverCallback(rq);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void asyncUpdateReceivedAdvertisementStatusToResponse(final long unique_id, final long adv_id){
		mFixedHttpThreadPool.execute(new Runnable(){
			@Override
			public void run(){
				try{
					RcvdAdvSession session = SamService.getInstance().getDao().query_RcvdAdvSession_db(unique_id);
					if(session!=null){
						Advertisement adv =SamService.getInstance().getDao().query_RcvdAdv_db_by_advid(session.getname(), adv_id);
						if(adv != null){
							adv.setresponse(Constants.ADV_RESPONSED);
							SamService.getInstance().getDao().update_RcvdAdv_db_response(session.getname(),  adv_id, Constants.ADV_RESPONSED);
							callRcvdAdvObserversObserverCallback(adv);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}



	public void registerObservers(boolean register){
		NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingP2PMessageObserver,register);
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

	private List<AdvancedMessage> handleAdvId(List<IMMessage> ims){
       List<AdvancedMessage> result = new ArrayList<>();
		if(ims == null || ims.size() == 0){
			return result;
		}
		
		for(IMMessage m:ims){
			Map<String, Object> content = m.getRemoteExtension();
			if(content != null && content.containsKey(NimConstants.ADV_ID)){
				String adv_id = (String)content.get(NimConstants.ADV_ID);
				LogUtil.i(TAG,"find im with adv_id:"+adv_id);
				Advertisement adv = SamService.getInstance().getDao().query_SamProsAdv_db_by_adv_id(Long.valueOf(adv_id));
				if(adv == null){
					continue;
				}
				result.add(new AdvancedMessage(null,m,adv));
			}
		}

		return result;
	}

	private void saveSAimsToMsgDB(final MsgSession changedSession, final List<IMMessage> ims_sp_mode){
		for(IMMessage im: ims_sp_mode){
			Map<String, Object> content = im.getRemoteExtension();
			if(content != null && content.containsKey(NimConstants.SA_ADV_ID)){
				NIMClient.getService(MsgService.class).saveMessageToLocal(im, false).setCallback(new RequestCallback<Void>() {
           		@Override
           		public void onSuccess(Void a) {
						//call session changed observer
						callMsgServiceObserverCallback(changedSession);
						//call incoming IMMessage observer
						callIncomingMsgObserverCallback(ims_sp_mode);
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

	private IMMessage findSendAdvertisementIMMessage(Advertisement adv){
		MsgSession session=SamService.getInstance().getDao().query_MsgSession_db(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT,ModeEnum.SP_MODE.ordinal());
		if(session != null){
			Message msg = SamService.getInstance().getDao().query_Message_db_by_type_data_id(session.getmsg_table_name(),NimConstants.MSG_TYPE_SEND_ADV,adv.getadv_id());
			if(msg != null){
				List<String> uuids  =  new ArrayList<>();
				uuids.add(msg.getuuid());
				List<IMMessage> ims = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
				if(ims.size()>0){
					return ims.get(0);
				}
			}
		}

		return null;
	}

	Observer<List<IMMessage>> incomingP2PMessageObserver = new Observer<List<IMMessage>>() {
		@Override
		public void onEvent(final List<IMMessage> ims) {
			mFixedHttpThreadPool.execute(new Runnable(){
					@Override
					public void run(){
						try{
							if(ims == null || ims.size() == 0){
								return;
							}
							SessionTypeEnum sessionType = ims.get(0).getSessionType();
							String sessionId = ims.get(0).getSessionId();
							if(sessionType !=  SessionTypeEnum.P2P){
								return;
							}

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
							/*parse advertisement if existed*/
							List<AdvancedMessage> ammsgs = handleAdvId(valid_ims);
							boolean adv_im_existed = false;
							int index = -1;
							for(AdvancedMessage am: ammsgs){
								index = -1;
								for(int i=0; i<valid_ims.size();i++){
									if(am.getim().getUuid().equals(valid_ims.get(i).getUuid())){
										index = i;
										break;
									}
								}
								if(index > -1){
									if(am.getadv().gettype() == Constants.ADV_TYPE_TEXT){
										valid_ims.add(index,SAMMessageBuilder.createSendAdvertisementTextMessage(am.getadv(), am.getim()));
										adv_im_existed = true;
									}else{
										IMMessage sm = findSendAdvertisementIMMessage(am.getadv());
										if(sm!=null){
											valid_ims.add(index,SAMMessageBuilder.createSendAdvertisementImageMessage(am.getadv(),sm,am.getim()));
											adv_im_existed = true;
										}
									}
									LogUtil.i(TAG,"find adv im message");
								}
							}
			
							/*parse question id if existed*/
							ammsgs = handleQuestId(valid_ims);
							boolean sq_im_existed = false;
							/*insert send question IMMessage into yunxin msg db*/
							index = -1;
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

							LogUtil.i(TAG,"ims_sp_mode ims size:"+ims_sp_mode.size());

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
								if(!adv_im_existed){
									//call session changed observer
									callMsgServiceObserverCallback(changedSession);
									//call incoming IMMessage observer
									callIncomingMsgObserverCallback(ims_sp_mode);
								}else{
									saveSAimsToMsgDB(changedSession, ims_sp_mode);
								}
							}
						}catch(Exception e){
							e.printStackTrace();
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

	synchronized public void registerReceivedQuestionUnreadClearObserver(SamchatObserver<ReceivedQuestion> observer,boolean register){
		if(register){
			ReceivedQuestionUnreadClearObservers.add(observer);
		}else{
			ReceivedQuestionUnreadClearObservers.remove(observer);
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

	synchronized public void registerRcvdAdvSessionObserver(SamchatObserver<RcvdAdvSession> observer,boolean register){
		if(register){
			RcvdAdvSessionObservers.add(observer);
		}else{
			RcvdAdvSessionObservers.remove(observer);
		}
	}

	synchronized public void registerRcvdAdvObserver(SamchatObserver<Advertisement> observer,boolean register){
		if(register){
			RcvdAdvObservers.add(observer);
		}else{
			RcvdAdvObservers.remove(observer);
		}
	}

	synchronized public void registerReceivedAdvertisementUnreadClearObserver(SamchatObserver<RcvdAdvSession> observer,boolean register){
		if(register){
			ReceivedAdvertisementUnreadClearObservers.add(observer);
		}else{
			ReceivedAdvertisementUnreadClearObservers.remove(observer);
		}
	}

	synchronized public void registerClearHistoryObserver(SamchatObserver<SessionBasicInfo> observer, boolean register){
		if(register){
			ClearHistoryObservers.add(observer);
		}else{
			ClearHistoryObservers.remove(observer);
		}
	}

};
