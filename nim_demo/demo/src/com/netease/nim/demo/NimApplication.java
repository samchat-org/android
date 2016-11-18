package com.netease.nim.demo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.samchat.R;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samservice.Constants;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.demo.avchat.AVChatProfile;
import com.netease.nim.demo.avchat.activity.AVChatActivity;
import com.netease.nim.demo.common.util.crash.AppCrashHandler;
import com.netease.nim.demo.common.util.sys.SystemUtil;
import com.netease.nim.demo.config.ExtraOptions;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHelper;
import com.netease.nim.demo.main.activity.WelcomeActivity;
import com.netease.nim.demo.rts.activity.RTSActivity;
import com.netease.nim.demo.session.NimDemoLocationProvider;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.util.string.ConvertHelper;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nim.uikit.contact.core.query.PinYin;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MessageNotifierCustomization;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.android.samchat.service.SamDBManager;
import com.android.samchat.factory.UuidFactory;

import org.json.JSONObject;

public class NimApplication extends Application {

    public void onCreate() {
        super.onCreate();

        DemoCache.setContext(this);
        DemoCache.setApp(this);

        NIMClient.init(this, getLoginInfo(), getOptions());

        ExtraOptions.provide();

        // crash handler
        AppCrashHandler.getInstance(this);

        if (inMainProcess()) {
            // init pinyin
            PinYin.init(this);
            PinYin.validate();

            initUIKit();

            if(!TextUtils.isEmpty(DemoCache.getAccount())){
					String account = Preferences.getUserAccount();
					String token = Preferences.getUserToken()+UuidFactory.getInstance().getDeviceId();
					SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getAccount()));
					SamchatDataCacheManager.buildDataCache(); // build data cache on auto login
					if(SamService.getInstance().get_current_user() == null || SamService.getInstance().get_current_token() == null){
						ContactUser cuser = SamchatUserInfoCache.getInstance().getUserByUniqueID(Long.valueOf(account));
						SamService.getInstance().set_current_user(cuser);
						SamService.getInstance().store_current_token(token);
					}
					UserPreferences.setRequestToggle(SamService.getInstance().get_current_user().getquestion_notify()==1);
                 SamDBManager.getInstance().registerObservers(true);
            }

            registerIMMessageFilter();

            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

            enableAVChat();

            enableRTS();

            registerLocaleReceiver(true);
        }
    }

    public LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token+UuidFactory.getInstance().getDeviceId());
        } else {
            return null;
        }
    }

    public SDKOptions getOptions() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给SDK完成，需要添加以下配置。
        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
        if (config == null) {
            config = new StatusBarNotificationConfig();
        }
        // 点击通知需要跳转到的界面
        config.notificationEntrance = WelcomeActivity.class;
        config.notificationSmallIconId = R.drawable.ic_stat_notify_msg;

        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.android.samchat/raw/msg";

        // 呼吸灯配置
        //config.ledARGB = Color.GREEN;
        //config.ledOnMs = 1000;
        //config.ledOffMs = 1500;
        config.ledARGB = -1;
        config.ledOnMs = -1;
        config.ledOffMs = -1;

        options.statusBarNotificationConfig  = config;
        DemoCache.setNotificationConfig(config);
        UserPreferences.setStatusConfig(config);

        // 配置保存图片，文件，log等数据的目录
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置数据库加密秘钥
        options.databaseEncryptKey = "NETEASE";

        // 配置是否需要预下载附件缩略图
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，
        options.thumbnailSize = MsgViewHolderThumbBase.getImageMaxEdge();

        // 用户信息提供者
        options.userInfoProvider = infoProvider;

        // 定制通知栏提醒文案（可选，如果不定制将采用SDK默认文案）
        options.messageNotifierCustomization = messageNotifierCustomization;

        return options;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    private void registerIMMessageFilter() {
        NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (UserPreferences.getMsgIgnore() && message.getAttachment() != null) {
                    if (message.getAttachment() instanceof UpdateTeamAttachment) {
                        UpdateTeamAttachment attachment = (UpdateTeamAttachment) message.getAttachment();
                        for (Map.Entry<TeamFieldEnum, Object> field : attachment.getUpdatedFields().entrySet()) {
                            if (field.getKey() == TeamFieldEnum.ICON) {
                                return true;
                            }
                        }
                    } else if (message.getAttachment() instanceof AVChatAttachment) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * 音视频通话配置与监听
     */
    private void enableAVChat() {
        registerAVChatIncomingCallObserver(true);
    }

    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                String extra = data.getExtra();
                Log.e("Extra", "Extra Message->" + extra);
                // 有网络来电打开AVChatActivity
                AVChatProfile.getInstance().setAVChatting(true);
                AVChatActivity.launch(DemoCache.getContext(), data, AVChatActivity.FROM_BROADCASTRECEIVER);
            }
        }, register);
    }

    /**
     * 白板实时时会话配置与监听
     */
    private void enableRTS() {
        registerRTSIncomingObserver(true);
    }


    private void registerRTSIncomingObserver(boolean register) {
        RTSManager.getInstance().observeIncomingSession(new Observer<RTSData>() {
            @Override
            public void onEvent(RTSData rtsData) {
                RTSActivity.incomingSession(DemoCache.getContext(), rtsData, RTSActivity.FROM_BROADCAST_RECEIVER);
            }
        }, register);
    }

    private void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            registerReceiver(localeReceiver, filter);
        } else {
            unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };

    private void updateLocale() {
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = getString(R.string.nim_status_bar_multi_messages_incoming);
        strings.status_bar_image_message = getString(R.string.nim_status_bar_image_message);
        strings.status_bar_audio_message = getString(R.string.nim_status_bar_audio_message);
        strings.status_bar_custom_message = getString(R.string.nim_status_bar_custom_message);
        strings.status_bar_file_message = getString(R.string.nim_status_bar_file_message);
        strings.status_bar_location_message = getString(R.string.nim_status_bar_location_message);
        strings.status_bar_notification_message = getString(R.string.nim_status_bar_notification_message);
        strings.status_bar_ticker_text = getString(R.string.nim_status_bar_ticker_text);
        strings.status_bar_unsupported_message = getString(R.string.nim_status_bar_unsupported_message);
        strings.status_bar_video_message = getString(R.string.nim_status_bar_video_message);
        strings.status_bar_hidden_message_content = getString(R.string.nim_status_bar_hidden_msg_content);
        NIMClient.updateStrings(strings);
    }

    private void initUIKit() {
        // 初始化，需要传入用户信息提供者
        NimUIKit.init(this, infoProvider, contactProvider);

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // 会话窗口的定制初始化。
        SessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();
    }

	 /*SAMC_BEGIN(samchat user info provider)*/
    private UserInfoProvider infoProvider = new UserInfoProvider() {
        @Override
        public UserInfo getUserInfo(String account) {
			ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ConvertHelper.stringTolong(account));
			if(user != null){
				return user;
			}

			Contact contact = ContactDataCache.getInstance().getContactByUniqueID(ConvertHelper.stringTolong(account));
			if(contact != null){
				return contact;
			}

			contact = CustomerDataCache.getInstance().getCustomerByUniqueID(ConvertHelper.stringTolong(account));
			if(contact != null){
				return contact;
			}	
						
			FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(ConvertHelper.stringTolong(account));
			if(fsp !=null){
				return fsp;
			}
			
			SamchatUserInfoCache.getInstance().getUserByUniqueIDFromRemote(ConvertHelper.stringTolong(account));
			return null;
        }

        @Override
        public int getDefaultIconResId() {
            return R.drawable.avatar_def;
        }

        @Override
        public Bitmap getTeamIcon(String teamId) {
            Drawable drawable = getResources().getDrawable(R.drawable.nim_avatar_group);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }

            return null;
        }

        @Override
        public Bitmap getAvatarForMessageNotifier(String account) {
            UserInfo user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ConvertHelper.stringTolong(account));
            return (user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user) : null;
        }

        @Override
        public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionType) {
            String nick = null;
            if (sessionType == SessionTypeEnum.P2P) {
					UserInfo user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ConvertHelper.stringTolong(account));
					nick = (user != null) ? user.getName():null;
            } else if (sessionType == SessionTypeEnum.Team) {
                nick = TeamDataCache.getInstance().getTeamNick(sessionId, account);
                if (TextUtils.isEmpty(nick)) {
                    nick = NimUserInfoCache.getInstance().getAlias(account);
                }
            }
            if (TextUtils.isEmpty(nick)) {
                return null;
            }

            return nick;
        }
    };
	 /*SAMC_END(samchat user info provider)*/

    private ContactProvider contactProvider = new ContactProvider() {
        @Override
        public List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends() {
            List<NimUserInfo> nimUsers = NimUserInfoCache.getInstance().getAllUsersOfMyFriend();
            List<UserInfoProvider.UserInfo> users = new ArrayList<>(nimUsers.size());
            if (!nimUsers.isEmpty()) {
                users.addAll(nimUsers);
            }

            return users;
        }

        @Override
        public int getMyFriendsCount() {
            return FriendDataCache.getInstance().getMyFriendCounts();
        }

        @Override
        public String getUserDisplayName(String account) {
            ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ConvertHelper.stringTolong(account));
            if(user != null){
               return user.getusername();
            }

            Contact contact = ContactDataCache.getInstance().getContactByUniqueID(ConvertHelper.stringTolong(account));
            if(contact != null){
                return contact.getusername();
            }

            contact = CustomerDataCache.getInstance().getCustomerByUniqueID(ConvertHelper.stringTolong(account));
            if(contact != null){
                return contact.getusername();
            }	
						
            FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(ConvertHelper.stringTolong(account));
            if(fsp !=null){
                return fsp.getusername();
            }

            return account;
        }
    };

	 public String getTicker(String data){
		try{
			JSONObject obj = new JSONObject(data);
			JSONObject header = obj.getJSONObject("header");
			JSONObject body = obj.getJSONObject("body");

			int type = body.getInt("type");
			String content = body.getString("content");
			if(type == Constants.ADV_TYPE_TEXT){
				return content;
			}else{
				return "["+DemoCache.getContext().getString(R.string.samchat_picture)+"]";
			}
		}catch (Exception e) {  
			e.printStackTrace();
			return "";
		}
	}

	private MessageNotifierCustomization messageNotifierCustomization = new MessageNotifierCustomization() {
		@Override
		public String makeNotifyContent(String nick, IMMessage message) {
			if(message.getSessionType() == SessionTypeEnum.P2P){
				String sessionId = message.getSessionId();
				if(sessionId.contains(NimConstants.PUBLIC_ACCOUNT_PREFIX)){
					String jsonString = message.getContent();
					return getTicker(jsonString);
				}else{
					return "";
				}
			}else{
				return "";
			}
        }

		@Override
		public String makeTicker(String nick, IMMessage message) {
			if(message.getSessionType() == SessionTypeEnum.P2P){
                String sessionId = message.getSessionId();
				if(sessionId.contains(NimConstants.PUBLIC_ACCOUNT_PREFIX)){
					String jsonString = message.getContent();
                    return nick + ":" + getTicker(jsonString);
				}else{
					return "";
				}
			}else{
				return "";
			}
		}
	};
}
