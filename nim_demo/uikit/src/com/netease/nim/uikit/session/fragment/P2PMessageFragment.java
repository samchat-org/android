package com.netease.nim.uikit.session.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nim.uikit.session.module.list.P2PMessageListPanel;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
public class P2PMessageFragment extends TFragment implements ModuleProxy {

    private View rootView;

    private SessionCustomization customization;

    protected static final String TAG = "MessageActivity";

    protected String sessionId; 

    protected SessionTypeEnum sessionType;

    private int mode = 0;

    // modules
    protected InputPanel inputPanel;
    protected P2PMessageListPanel messageListPanel;

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
        NimUIKit.getCallback().clearUnreadCount(sessionId,  mode);
        /*SAMC_END(clear unread count)*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageListPanel.onDestroy();
        registerObservers(false);
        /*SAMC_BEGIN(clear unread count)*/
        NimUIKit.getCallback().clearUnreadCount(sessionId,  mode);
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
        mode = getArguments().getInt(Extras.EXTRA_MODE,0);
        sessionId = getArguments().getString(Extras.EXTRA_ACCOUNT);
        sessionType = (SessionTypeEnum) getArguments().getSerializable(Extras.EXTRA_TYPE);

        customization = (SessionCustomization) getArguments().getSerializable(Extras.EXTRA_CUSTOMIZATION);
        Container container = new Container(getActivity(), sessionId, mode ,sessionType, this);

        messageListPanel = new P2PMessageListPanel(container, rootView, null);

        inputPanel = new InputPanel(container, rootView, getActionList());
        inputPanel.setCustomization(customization);

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
        //service.observeReceiveMessage(incomingMessageObserver, register);
        NimUIKit.getCallback().registerIncomingMsgObserver(IncomingMsgObserver, register);
        service.observeMessageReceipt(messageReceiptObserver, register);
    }

	SamchatObserver < List < IMMessage >> IncomingMsgObserver = new SamchatObserver < List < IMMessage >>(){
		@Override
		public void onEvent(final List < IMMessage > messages) {
			if (messages == null || messages.isEmpty()) {
				return;
			}

         getHandler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 messageListPanel.onIncomingMessage(messages);
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
        Map<String, Object> msg_from = new HashMap<>(1);
        int current_mode = NimUIKit.getCallback().getCurrentMode();
        msg_from.put("msg_from",new Integer(current_mode));
        message.setRemoteExtension(msg_from);

        Map<String, Object> msg_to = new HashMap<>(1);
        msg_to.put("msg_to",new Integer(current_mode));
        message.setLocalExtension(msg_to);

        CustomMessageConfig config = message.getConfig();
        if(config == null){
            config = new CustomMessageConfig();
            config.enableRoaming = false;
        }else{
            config.enableRoaming = false;
        }
        message.setConfig(config);
        long id = NimUIKit.getCallback().storeMessage(message);
	     if(id == -1){
            return false;
        }
        /*SAMC_END(add local and remote tag)*/
        // send message to server and save to db
        NIMClient.getService(MsgService.class).sendMessage(message, false);

        messageListPanel.onMsgSend(id,message);

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
        //messageListPanel.sendReceipt();
    }

    /**
     * 收到已读回执
     */
    public void receiveReceipt() {
        //messageListPanel.receiveReceipt();
    }
}

