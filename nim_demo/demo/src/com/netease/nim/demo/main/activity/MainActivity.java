package com.netease.nim.demo.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.avchat.AVChatProfile;
import com.netease.nim.demo.avchat.activity.AVChatActivity;
import com.netease.nim.demo.chatroom.helper.ChatRoomHelper;
import com.netease.nim.demo.contact.activity.AddFriendActivity;
import com.netease.nim.demo.login.LoginActivity;
import com.netease.nim.demo.login.LogoutHelper;
import com.netease.nim.demo.main.fragment.HomeFragment;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.team.TeamCreateHelper;
import com.netease.nim.demo.team.activity.AdvancedTeamSearchActivity;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
/*SAMC_BEGIN(......)*/
import com.igexin.sdk.PushManager;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nim.uikit.common.util.string.StringUtil;
import android.os.HandlerThread;
import android.os.Handler;
import com.android.samservice.SamService;
import com.netease.nim.demo.config.preference.Preferences;
import com.android.samservice.info.ContactUser;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import com.netease.nim.demo.main.model.MainTab;
import android.support.v4.content.LocalBroadcastManager;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.SamchatGlobal;
import com.android.samchat.type.ModeEnum;
import com.android.samservice.Constants;
import com.android.samchat.service.SamDBManager;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nim.uikit.NIMCallback;
import java.util.List;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nim.demo.main.reminder.ReminderManager;
/*SAMC_END(......)*/

/**
 * &#x4e3b;&#x754c;&#x9762;
 * <p/>
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends UI implements NimUIKit.NimUIKitInterface{

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;

    private HomeFragment mainFragment;

    /*SAMC_BEGIN(Getu SDK initilized tag)*/
    private boolean isGetuInited = false;
    /*SAMC_END(Getu SDK initilized tag)*/

    /*SAMC_BEGIN(Customized title bar)*/
    private ImageView switch_icon;
    private TextView switch_reminder;
    private TextView titlebar_name;
    private ImageView titlebar_right_icon;
    private int current_position = 0;
    /*SAMC_END(Customized title bar)*/ 

    /*SAMC_BEGIN(unread count for 2 mode)*/
    private int chat_unread_count_customer = 0;
    private int chat_unread_count_sp = 0;

    public int getchat_unread_count_customer(){
        return chat_unread_count_customer;
    }
		
    public void setchat_unread_count_customer(int count){
        chat_unread_count_customer = count;
    }

    public int getchat_unread_count_sp(){
        return chat_unread_count_customer;
    }
		
    public void setchat_unread_count_sp(int count){
        chat_unread_count_sp = count;
    }
    /*SAMC_END(unread count for 2 mode)*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(MainActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(MainActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*SAMC_BEGIN(import GETU)*/
        initMode();
        registerObservers(true);
        if(NIMClient.getStatus() == StatusCode.LOGINED){
            initSamAutoLogin();
            initGeTui();
        }
        /*SAMC_END(import GETU)*/
        setContentView(R.layout.activity_main_tab);

        /*SAMC_END(tool bar customized)*/
        //setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
		  setToolBar(R.id.toolbar);
        titlebarInit();
        //setTitle(R.string.app_name);
        /*SAMC_END(tool bar customized)*/
        requestBasicPermission();

        onParseIntent();

        // 等待同步数据完成
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });

        Log.i(TAG, "sync completed = " + syncCompleted);
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        }

        onInit();
    }

    /**
     * 基本权限管理
     */
    private void requestBasicPermission() {
        MPermission.with(MainActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                        )
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess(){
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed(){
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }

    private void onInit() {
        // 加载主页面
        showMainFragment();

        // 聊天室初始化
        ChatRoomHelper.init();

        LogUtil.ui("NIM SDK cache path=" + NIMClient.getSdkStorageDirPath());
        /*SAMC_BEGIN(add uikit interface)*/
        NimUIKit.setCallback(this);
        /*SAMC_END(add uikit interface)*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }

    @Override
    public void onBackPressed() {
        if (mainFragment != null) {
            if (mainFragment.onBackPressed()) {
                return;
            } else {
                moveTaskToBack(true);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.clear();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(MainActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelect(MainActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    private void onParseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
                default:
                    break;
            }
        } else if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        } else if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT)) {
            if (AVChatProfile.getInstance().isAVChatting()) {
                Intent localIntent = new Intent();
                localIntent.setClass(this, AVChatActivity.class);
                startActivity(localIntent);
            }
        } else if (intent.hasExtra(com.netease.nim.demo.main.model.Extras.EXTRA_JUMP_P2P)) {
            Intent data = intent.getParcelableExtra(com.netease.nim.demo.main.model.Extras.EXTRA_DATA);
            String account = data.getStringExtra(com.netease.nim.demo.main.model.Extras.EXTRA_ACCOUNT);
            if (!TextUtils.isEmpty(account)) {
                SessionHelper.startP2PSession(this, account);
            }
        }
    }

    private void showMainFragment() {
        if (mainFragment == null && !isDestroyedCompatible()) {
            mainFragment = new HomeFragment();
            switchFragmentContent(mainFragment);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NORMAL) {
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
                } else {
                    Toast.makeText(MainActivity.this, "请选择至少一个联系人！", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_ADVANCED) {
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);
            }
        }

    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听
        LogoutHelper.logout();

        // 启动登录
        LoginActivity.start(this);
        finish();
    }



/*******************************Samchat add******************************************/
/*SAMC_BEGIN(...)*/
	private void registerObservers(boolean register) {
		NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onLineStatusObserver, register);
	}

	private Observer<StatusCode> onLineStatusObserver = new Observer<StatusCode>() {
		@Override
		public void onEvent(StatusCode code) {
			if (code.wontAutoLogin()) {
				LogUtil.ui("SDK will not try auto login again");
			} else {
				if (code == StatusCode.LOGINED) {
					LogUtil.ui("SDK auto login succeed");
					initSamAutoLogin();
					initGeTui();
				} 
			}
		}
	};

	@Override
	public void onDestroy() {
		registerObservers(false);
		super.onDestroy();
	}

	private void initGeTui(){
		if(!isGetuInited){
			PushManager.getInstance().initialize(getApplicationContext());
			isGetuInited = true;
		}
	}

	private void initSamAutoLogin(){
		String account = Preferences.getUserAccount();
		String token = Preferences.getUserToken();
		SamService.getInstance().initDao(StringUtil.makeMd5(account));
		if(SamService.getInstance().get_current_user() == null || SamService.getInstance().get_current_token() == null){
			ContactUser cuser = SamService.getInstance().getDao().query_ContactUser_db_by_unique_id(Long.valueOf(account));
			SamService.getInstance().set_current_user(cuser);
			SamService.getInstance().store_current_token(token);
		}
	}

	public void refreshToolBar(int position){
		switch_icon.setImageResource(MainTab.getTabIcon(position));
		titlebar_name.setText(MainTab.getTabTitle(position));
	}

	public void refreshTabUnreadCount(ModeEnum currentMode){
		if(currentMode == ModeEnum.CUSTOMER_MODE){
			ReminderManager.getInstance().updateSessionUnreadNum(chat_unread_count_customer);
		}else{
			ReminderManager.getInstance().updateSessionUnreadNum(chat_unread_count_sp);
		}
	}

	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void sendSwitchModeBroadcast(){
		int to=0;
		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			to = ModeEnum.valueOfType(ModeEnum.SP_MODE);
		}else{
			to = ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE);
		}

		Bundle bundle = new Bundle();
		bundle.putInt("to",to);
		Intent intent = new Intent();
		intent.setAction(Constants.BROADCAST_SWITCH_MODE);
		intent.putExtras(bundle);
		sendbroadcast(intent);
	}

	private void switchMode(){
		LogUtil.e(TAG,"start switch samchat Mode");
		startSwitchProgress();
		sendSwitchModeBroadcast();
	}

	private void titlebarInit(){
		switch_icon = (ImageView) findViewById(R.id.switch_icon);
		switch_reminder = (TextView) findViewById(R.id.switch_reminder);
		titlebar_name = (TextView) findViewById(R.id.titlebar_name);
		titlebar_right_icon = (ImageView) findViewById(R.id.titlebar_right_icon);

		refreshToolBar(current_position);
		switch_icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchMode();
			}
		});
		
	}

	public void startSwitchProgress(){
		DemoCache.setSwitching(true);
		DemoCache.clearTag();
		DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.mode_switching)).setCanceledOnTouchOutside(false);
	}

	public void dimissSwitchProgress(){
		DemoCache.addTag();
		if(DemoCache.getTag() == MainTab.values().length){
			DialogMaker.dismissProgressDialog();
			DemoCache.setSwitching(false);
			DemoCache.clearTag();
			SamchatGlobal.switchMode();
			saveMode(SamchatGlobal.getmode());
			refreshToolBar(current_position);
			refreshTabUnreadCount(SamchatGlobal.getmode());
		}
	}

	public void setCurrentPostition(int pos){
		current_position = pos;
	}

	public void initMode(){
		SamchatGlobal.setmode(ModeEnum.typeOfValue(Preferences.getMode())); 
	}
	public void saveMode(ModeEnum mode){
		Preferences.saveMode(ModeEnum.valueOfType(mode));
	}

	//NimUIKitInterface
	public  int getCurrentMode(){
		return ModeEnum.valueOfType(SamchatGlobal.getmode());
	}

	public void storeSendMessage(IMMessage msg,NIMCallback callback){
		SamDBManager.getInstance().asyncStoreSendMessage(msg,  callback);
	}

	public void clearUnreadCount(String session_id, int mode){
		SamDBManager.getInstance().asyncClearUnreadCount( session_id, mode);
	}

	public void queryMessage(String session_id, int mode,IMMessage msg, QueryDirectionEnum direction, int count, NIMCallback callback){
		SamDBManager.getInstance().queryMessage(session_id,  mode,  msg,  direction,  count,  callback);
	}

	public void deleteMessage(String session_id, int mode,IMMessage msg){
		SamDBManager.getInstance().asyncDeleteMessage(session_id, mode, msg);
	}

	public void registerIncomingMsgObserver(SamchatObserver<List<IMMessage>> observer,boolean register){
		SamDBManager.getInstance().registerIncomingMsgObserver(observer, register);
	}

	public void registerSendCustomerMsgObserver(SamchatObserver < IMMessage > observer, boolean register){
		SamDBManager.getInstance().registerSendCustomerMsgObserver(observer, register);
	}

	public void storeSendCustomerMessage(IMMessage msg,NIMCallback callback){
		SamDBManager.getInstance().asyncStoreSendCustomerMessage(msg,  callback);
	}

	public void storeRecvCustomerMessage(IMMessage msg,NIMCallback callback){
		List<IMMessage> ims = new ArrayList<IMMessage>(1);
		ims.add(msg);
		SamDBManager.getInstance().asyncStoreRecvCustomerMessages(ims);
	}
	
/*SAMC_END(...)*/
}
