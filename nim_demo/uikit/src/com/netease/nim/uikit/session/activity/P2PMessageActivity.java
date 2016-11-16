package com.netease.nim.uikit.session.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nim.uikit.session.sam_message.SessionBasicInfo;
import com.netease.nim.uikit.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nim.uikit.uinfo.UserInfoObservable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.List;


public class P2PMessageActivity extends BaseMessageActivity {

    private boolean isResume = false;
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(P2PMessageActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(NimConstants.BROADCAST_TEAM_ACTIVITY_START);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(NimConstants.BROADCAST_TEAM_ACTIVITY_START)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
		isBroadcastRegistered  = true;
	}
	private void unregisterBroadcastReceiver(){
	    if(isBroadcastRegistered){
			broadcastManager.unregisterReceiver(broadcastReceiver);
			isBroadcastRegistered = false;
		}
	}

	private void sendbroadcast(){
		Intent intent = new Intent();
		intent.setAction(NimConstants.BROADCAST_P2P_ACTIVITY_START);
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
		manager.sendBroadcast(intent);
	}
    /*SAMC_BEGIN(support mode setting for p2p activity)*/
    public static void start(Context context, String contactId, SessionCustomization customization,int mode, long question_id,long adv_id) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, contactId);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(Extras.EXTRA_MODE,mode);
        intent.putExtra(Extras.EXTRA_QUESTIONID,question_id);
        intent.putExtra(Extras.EXTRA_ADVID,adv_id);
        intent.setClass(context, P2PMessageActivity.class);
		 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intent);
    }
    /*SAMC_BEGIN(support mode setting for p2p activity)*/

    public static void start(Context context, String contactId, SessionCustomization customization) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, contactId);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.setClass(context, P2PMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 单聊特例话数据，包括个人信息，
        requestBuddyInfo();
        registerObservers(true);
        registerBroadcastReceiver();
        sendbroadcast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
        unregisterBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
    }

    private void requestBuddyInfo() {
        //setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
    }

    private void registerObservers(boolean register) {
        if (register) {
            registerUserInfoObserver();
        } else {
            unregisterUserInfoObserver();
        }
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(commandObserver, register);
        FriendDataCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver, register);
        NimUIKit.getCallback().registerClearHistoryObserver(ClearHistoryObserver, register);
    }

	SamchatObserver<SessionBasicInfo> ClearHistoryObserver = new SamchatObserver < SessionBasicInfo >(){
		@Override
		public void onEvent(SessionBasicInfo sinfo){
			if(sinfo.gettype() == SessionTypeEnum.P2P && sinfo.getsession_id().equals(sessionId) && sinfo.getmode()==mode){
				clearMessageList();
			}
		}
	};

    FriendDataCache.FriendDataChangedObserver friendDataChangedObserver = new FriendDataCache.FriendDataChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            //setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            //setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            //setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            //setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }
    };

    private UserInfoObservable.UserInfoObserver uinfoObserver;

    private void registerUserInfoObserver() {
        if (uinfoObserver == null) {
            uinfoObserver = new UserInfoObservable.UserInfoObserver() {
                @Override
                public void onUserInfoChanged(List<String> accounts) {
                    if (accounts.contains(sessionId)) {
                        requestBuddyInfo();
                    }
                }
            };
        }

        UserInfoHelper.registerObserver(uinfoObserver);
    }

    private void unregisterUserInfoObserver() {
        if (uinfoObserver != null) {
            UserInfoHelper.unregisterObserver(uinfoObserver);
        }
    }

    /**
     * 命令消息接收观察者
     */
    Observer<CustomNotification> commandObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification message) {
            if (!sessionId.equals(message.getSessionId()) || message.getSessionType() != SessionTypeEnum.P2P) {
                return;
            }
            showCommandMessage(message);
        }
    };

    protected void showCommandMessage(CustomNotification message) {
        if (!isResume) {
            return;
        }

        String content = message.getContent();
        try {
            JSONObject json = JSON.parseObject(content);
            int id = json.getIntValue("id");
            if (id == 1) {
                // 正在输入
                Toast.makeText(P2PMessageActivity.this, getString(R.string.samchat_typing), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(P2PMessageActivity.this, "command: " + content, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {

        }
    }

    @Override
    protected MessageFragment fragment() {
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.P2P);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_message_activity;
    }

	private FrameLayout back_arrow_layout;
	private ImageView back_icon_iv;
	private TextView titlebar_name_tv;

	private void setTitlebarCustomerMode(){
        getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
    }

    private void setTitlebarSPMode(){
        getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left_sp);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
    }

	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onNavigateUpClicked();
			}
		});
	}

	@Override
	protected void initToolBar() {
		setToolBar(R.id.toolbar);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		back_icon_iv = findView(R.id.back_icon);
		titlebar_name_tv = findView(R.id.titlebar_name);
		String titleString = UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P);
		titlebar_name_tv.setText(titleString);
		if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
			setTitlebarCustomerMode();
		}else{
			setTitlebarSPMode();
		}
		setupBackArrowClick();
    }
}
