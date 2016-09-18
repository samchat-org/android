package com.netease.nim.uikit.session.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.session.actions.ImageAction;
import com.netease.nim.uikit.session.actions.LocationAction;
import com.netease.nim.uikit.session.actions.VideoAction;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.module.Container;
import com.netease.nim.uikit.session.module.ModuleProxy;
import com.netease.nim.uikit.session.module.input.InputPanel;
import com.netease.nim.uikit.session.module.list.MessageListPanel;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.common.util.log.LogUtil;
/**
 * 聊天界面基类
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class MessageFragment extends TFragment implements ModuleProxy {

    private View rootView;

    private SessionCustomization customization;

    protected static final String TAG = "SamchatMessageFragment";

    // 聊天对象
    protected String sessionId; // p2p对方Account或者群id

    protected SessionTypeEnum sessionType;

    /*SAMC_BEGIN(support mode setting for p2p activity)*/
    protected int mode = 0;
    protected long question_id = 0;
    protected long adv_id=0;
    /*SAMC_BEGIN(support mode setting for p2p activity)*/

    // modules
    protected InputPanel inputPanel;
    protected MessageListPanel messageListPanel;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.nim_message_fragment, container, false);
        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();

        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                SessionTypeEnum.None);
        inputPanel.onPause();
        messageListPanel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        messageListPanel.onResume();
        NIMClient.getService(MsgService.class).setChattingAccount(sessionId, sessionType);
        getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL); // 默认使用听筒播放

        /*SAMC_BEGIN(clear unread count)*/
        if(sessionType == SessionTypeEnum.P2P){
            NimUIKit.getCallback().clearUnreadCount(sessionId,  mode);
        }
        /*SAMC_END(clear unread count)*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageListPanel.onDestroy();
        registerObservers(false);
        /*SAMC_BEGIN(clear unread count)*/
        if(sessionType == SessionTypeEnum.P2P){
            NimUIKit.getCallback().clearUnreadCount(sessionId,  mode);
        }
        /*SAMC_END(clear unread count)*/
    }

    public boolean onBackPressed() {
        if (inputPanel.collapse(true)) {
            return true;
        }

        if (messageListPanel.onBackPressed()) {
            return true;
        }
        return false;
    }

    public void refreshMessageList() {
        messageListPanel.refreshMessageList();
    }

    private void parseIntent() {
		  /*SAMC_BEGIN(support mode setting for p2p activity)*/
        mode = getArguments().getInt(Extras.EXTRA_MODE,0);
        question_id = getArguments().getLong(Extras.EXTRA_QUESTIONID,0);
        adv_id = getArguments().getLong(Extras.EXTRA_ADVID,0);
        /*SAMC_END(support mode setting for p2p activity)*/
        sessionId = getArguments().getString(Extras.EXTRA_ACCOUNT);
        sessionType = (SessionTypeEnum) getArguments().getSerializable(Extras.EXTRA_TYPE);

        customization = (SessionCustomization) getArguments().getSerializable(Extras.EXTRA_CUSTOMIZATION);
        /*SAMC_BEGIN(support mode setting for p2p activity)*/
        Container container = new Container(getActivity(), sessionId, mode ,sessionType, this);
        /*SAMC_END(support mode setting for p2p activity)*/

        if (messageListPanel == null) {
            messageListPanel = new MessageListPanel(container, rootView, false, false);
        } else {
            messageListPanel.reload(container, null);
        }

        if (inputPanel == null) {
            inputPanel = new InputPanel(container, rootView, getActionList());
            inputPanel.setCustomization(customization);
        } else {
            inputPanel.reload(container, customization);
        }

        registerObservers(true);

        if (customization != null) {
            messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
        }
    }

    /**
     * ************************* 消息收发 **********************************
     */
    // 是否允许发送消息
    protected boolean isAllowSendMessage(final IMMessage message) {
        return true;
    }

    /**
     * ****************** 观察者 **********************
     */

    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        /*SAMC_BEGIN(support mode setting for p2p activity)*/
        if(sessionType != SessionTypeEnum.P2P){
            service.observeReceiveMessage(incomingMessageObserver, register);
        }else{
            NimUIKit.getCallback().registerIncomingMsgObserver(P2PincomingMsgObserver, register);
            NimUIKit.getCallback().registerSendCustomerMsgObserver(P2PSendCustomerMsgObserver,register);
            service.observeMsgStatus(statusObserver, register);
        }
        /*SAMC_END(support mode setting for p2p activity)*/
        service.observeMessageReceipt(messageReceiptObserver, register);
    }

    Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
		@Override
		public void onEvent(IMMessage message){
			if(message.	getDirect() == MsgDirectionEnum.Out && message.getStatus() == MsgStatusEnum.success){
				Map<String, Object> content = message.getRemoteExtension();
				if(content != null && content.containsKey(NimConstants.QUEST_ID)){
					String quest_id = (String)content.get(NimConstants.QUEST_ID);
					if(quest_id.equals(""+question_id)){
                        NimUIKit.getCallback().asyncUpdateReceivedQuestionStatusToResponse(Long.valueOf(quest_id));
							question_id = 0;
					}
				}

				if(content != null && content.containsKey(NimConstants.ADV_ID)){
					String advertisement_id = (String)content.get(NimConstants.ADV_ID);
					if(advertisement_id.equals(""+adv_id)){
                        NimUIKit.getCallback().asyncUpdateReceivedAdvertisementStatusToResponse(Long.valueOf(message.getSessionId()),Long.valueOf(adv_id));
							adv_id = 0;
					}
				}
				
			}
		}
	};

    SamchatObserver <  IMMessage > P2PSendCustomerMsgObserver = new SamchatObserver <  IMMessage >(){
		@Override
		public void onEvent(final IMMessage msg) {
         getHandler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 if(msg.getSessionType() == sessionType && sessionId.equals(msg.getSessionId())
								&& messageListPanel.isMyMode(msg)){
                     messageListPanel.onMsgSend(msg);
					 }
             }
          }, 0);
		}
	};

    SamchatObserver < List < IMMessage >> P2PincomingMsgObserver = new SamchatObserver < List < IMMessage >>(){
		@Override
		public void onEvent(final List < IMMessage > messages) {
			if (messages == null || messages.isEmpty()) {
				return;
			}

         getHandler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 messageListPanel.onIncomingMessage(messages);
                 sendMsgReceipt();
             }
          }, 50);
		}
	};

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            messageListPanel.onIncomingMessage(messages);
            sendMsgReceipt(); // 发送已读回执
        }
    };

    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {
            receiveReceipt();
        }
    };


    /**
     * ********************** implements ModuleProxy *********************
     */
    @Override
    public boolean sendMessage(IMMessage message) {
        if (!isAllowSendMessage(message)) {
            return false;
        }

        /*SAMC_BEGIN(add local and remote tag)*/
        if(sessionType == SessionTypeEnum.P2P){
            Map<String, Object> msg_from = new HashMap<>();
            msg_from.put(NimConstants.MSG_FROM,new Integer(mode));
            if(question_id > 0){
                msg_from.put(NimConstants.QUEST_ID,new String(""+question_id));
            }
            if(adv_id > 0){
                msg_from.put(NimConstants.ADV_ID,new String(""+adv_id));
            }
            message.setRemoteExtension(msg_from);

            CustomMessageConfig config = message.getConfig();
            if(config == null){
               config = new CustomMessageConfig();
               config.enableRoaming = false;
            }else{
               config.enableRoaming = false;
            }
            message.setConfig(config);
            NimUIKit.getCallback().storeSendMessage(message,new NIMCallback(){
                @Override
                public void onResult(Object obj1, Object obj2, int code) {
                    if(obj1 == null || code !=0){
                        LogUtil.e(TAG, "storeMessage failed before send msg, will not send this msg");
                        return;
                    }
                   final IMMessage msg = (IMMessage)obj1;
                   getHandler().postDelayed(new Runnable() {
                       @Override
                       public void run() {
                       	  // send message to server and save to db
                          NIMClient.getService(MsgService.class).sendMessage(msg, false);
                          messageListPanel.onMsgSend(msg);
                       }
                    },0);
                 }
            });

				return true;
        }
       /*SAMC_END(add local and remote tag)*/

        // send message to server and save to db
        NIMClient.getService(MsgService.class).sendMessage(message, false);

        messageListPanel.onMsgSend(message);

        return true;
    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        inputPanel.onActivityResult(requestCode, resultCode, data);
        messageListPanel.onActivityResult(requestCode, resultCode, data);
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        actions.add(new ImageAction());
        actions.add(new VideoAction());
        actions.add(new LocationAction());

        if (customization != null && customization.actions != null) {
            actions.addAll(customization.actions);
        }
        return actions;
    }

    /**
     * 发送已读回执
     */
    private void sendMsgReceipt() {
        messageListPanel.sendReceipt();
    }

    /**
     * 收到已读回执
     */
    public void receiveReceipt() {
        messageListPanel.receiveReceipt();
    }
}
