package com.netease.nim.demo.main.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.samchat.activity.SamchatAddCustomerActivity;
import com.android.samchat.activity.SamchatAddServiceProviderActivity;
import com.android.samchat.activity.SamchatContactUserNameCardActivity;
import com.android.samchat.activity.SamchatContactUserSPNameCardActivity;
import com.android.samchat.activity.SamchatMemberSelectActivity;
import com.android.samchat.activity.SamchatSearchPublicActivity;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.common.BasicUserInfoHelper;
import com.android.samchat.fragment.SamchatPublicFragment;
import com.android.samchat.receiver.NetworkStateBroadcastReceiver;
import com.android.samchat.receiver.PushReceiver;
import com.android.samchat.R;
import com.android.samchat.service.ErrorString;
import com.android.samchat.ui.ReminderRedPointView;
import com.android.samservice.HttpCommClient;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.Contact;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.type.TypeEnum;
import com.netease.nim.demo.avchat.AVChatProfile;
import com.netease.nim.demo.avchat.activity.AVChatActivity;
import com.netease.nim.demo.chatroom.helper.ChatRoomHelper;
import com.netease.nim.demo.contact.activity.AddFriendActivity;
import com.android.samchat.activity.SamchatLoginActivity;
import com.netease.nim.demo.login.LogoutHelper;
import com.netease.nim.demo.main.fragment.HomeFragment;
import com.netease.nim.demo.main.reminder.ReminderSettings;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.team.TeamCreateHelper;
import com.netease.nim.demo.team.activity.AdvancedTeamSearchActivity;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.uikit.session.sam_message.SessionBasicInfo;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
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
import com.android.samservice.Constants;
import com.android.samchat.service.SamDBManager;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nim.uikit.NIMCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.android.samchat.factory.UuidFactory;

/*SAMC_END(......)*/

/**
 * &#x4e3b;&#x754c;&#x9762;
 * <p/>
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends UI implements NimUIKit.NimUIKitInterface{

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    public static final int REQUEST_CODE_NORMAL = 1;
    public static final int REQUEST_CODE_ADVANCED = 2;
    public static final int REQUEST_CODE_SCAN_QRCODE = 3;
    private static final String TAG = "SamchatMainActivity";
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;

    private HomeFragment mainFragment;

    /*SAMC_BEGIN(Getu SDK initilized tag)*/
    //private static final int MSG_START_GETUI_INIT = 1;
    //private boolean isGetuInited = false;
    /*SAMC_END(Getu SDK initilized tag)*/

    /*SAMC_BEGIN(Customized title bar)*/
    private ImageView switch_icon;
    private FrameLayout switch_layout;
    //private TextView switch_reminder;
    private ReminderRedPointView switch_reminder_icon;
    private TextView titlebar_name;
    private RelativeLayout titlebar_right_layout;
    private ImageView titlebar_right_icon;
    private TextView titlebar_right_text;
    private int current_position = 0;
    /*SAMC_END(Customized title bar)*/ 

    /*SAMC_BEGIN(unread count for 2 mode)*/
    private int chat_unread_count_customer = 0;
    private int chat_unread_count_sp = 0;

    private int request_unread_count_customer = 0;
    private int request_unread_count_sp = 0;

    private int advertisement_unread_count_customer = 0;

    public int getchat_unread_count_customer(){
        return chat_unread_count_customer;
    }
		
    public void setchat_unread_count_customer(int count){
        chat_unread_count_customer = count;
        updateCustomerUnreadTotalCount();
    }

    public int getchat_unread_count_sp(){
        return chat_unread_count_sp;
    }
		
    public void setchat_unread_count_sp(int count){
        chat_unread_count_sp = count;
        updateSpUnreadTotalCount();
    }

    public int getrequest_unread_count_customer(){
        return request_unread_count_customer;
    }
		
    public void setrequest_unread_count_customer(int count){
        request_unread_count_customer = count;
        updateCustomerUnreadTotalCount();
    }

    public int getrequest_unread_count_sp(){
        return request_unread_count_sp;
    }
		
    public void setrequest_unread_count_sp(int count){
        request_unread_count_sp = count;
        updateSpUnreadTotalCount();
    }

     public int getadvertisement_unread_count_customer(){
        return advertisement_unread_count_customer;
    }
		
    public void setadvertisement_unread_count_customer(int count){
        advertisement_unread_count_customer = count;
        updateCustomerUnreadTotalCount();
    }
    /*SAMC_END(unread count for 2 mode)*/

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
                //ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                //NimUIKit.startContactSelect(MainActivity.this, option, REQUEST_CODE_NORMAL);
                SamchatMemberSelectActivity.startActivityForResult(MainActivity.this, new SamchatMemberSelectActivity.Option(1,50,null), REQUEST_CODE_NORMAL);
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
        initSamAutoLogin();
        initMode();
        registerObservers(true);
        /*if(NIMClient.getStatus() == StatusCode.LOGINED){
            initGeTui();
        }*/
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

        /*SAMC_BEGIN(sync samchat data including contact list, follow list, ...)*/
        registerNetworkReceiver();
        SamService.getInstance().startSync();
        /*SAMC_END(sync samchat data including contact list, follow list, ...)*/

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
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess(){
        //Toast.makeText(this, getString(R.string.samchat_permission_grant), Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed(){
        Toast.makeText(this, getString(R.string.samchat_permission_refused_storage), Toast.LENGTH_SHORT).show();
    }

	@OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionNeverAskAgainFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_storage), Toast.LENGTH_SHORT).show();
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
            /*SAMC_BEGIN(do not jump into P2P for samchat)*/
            /*IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
                default:
                    break;

            }*/
            /*SAMC_END(do not jump into P2P for samchat)*/
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
            /*SAMC_BEGIN(do not jump into P2P for samchat)*/
            //Intent data = intent.getParcelableExtra(com.netease.nim.demo.main.model.Extras.EXTRA_DATA);
            //String account = data.getStringExtra(com.netease.nim.demo.main.model.Extras.EXTRA_ACCOUNT);
            //if (!TextUtils.isEmpty(account)) {
            //    SessionHelper.startP2PSession(this, account);
            //}
            /*SAMC_END(do not jump into P2P for samchat)*/
        }
    }

    private void showMainFragment() {
        if (mainFragment == null && !isDestroyedCompatible()) {
            mainFragment = new HomeFragment();
            switchFragmentContent(mainFragment);
        }
    }

    private long transferToUniqueID(String content){
		long unique_id = -1;
		if(content.indexOf(NimConstants.QRCODE_PREFIX)!=0){
			return 0;
		}

		String content2 = content.substring(NimConstants.QRCODE_PREFIX.length());
		try{
			unique_id = Long.valueOf(content2);
		}catch(Exception e){
			e.printStackTrace();
			LogUtil.i(TAG,"warning: invalid qr code");
		}finally{
			return unique_id;
		}
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NORMAL) {
                /*final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
                } else {
                    Toast.makeText(MainActivity.this, "请选择至少一个联系人！", Toast.LENGTH_SHORT).show();
                }*/
            } else if (requestCode == REQUEST_CODE_ADVANCED) {
                /*final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);*/
            } else if(requestCode == REQUEST_CODE_SCAN_QRCODE){
                if (resultCode == RESULT_OK && data != null) {
                    String content = data.getStringExtra("codedContent");
				      long unique_id = transferToUniqueID(content);
				      if(unique_id >0){
					      if(isSending){
						     return;
					      }
					      query_user_precise(unique_id);
				       }else{
					      Toast.makeText(MainActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
				       }
                  }
			  }else{
                mainFragment.onActivityResult( requestCode,  resultCode,  data);
			 }
        }

    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听
        LogoutHelper.logout();

        // 启动登录
        SamchatLoginActivity.start(this);
        finish();
    }



/*******************************Samchat add******************************************/
/*SAMC_BEGIN(...)*/
	private boolean isSending=false;
	private void query_user_precise(long unique_id){
		isSending = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID, null, unique_id, null, false, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					final HttpCommClient hcc = (HttpCommClient)obj;
					if(hcc.users.getcount() > 0){
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								isSending = false;
								if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
									if( hcc.users.getusers().get(0).getusertype() != Constants.SAM_PROS){
										Toast.makeText(MainActivity.this, R.string.samchat_no_sp, Toast.LENGTH_LONG).show();
									}else{
										SamchatContactUserSPNameCardActivity.start(MainActivity.this, hcc.users.getusers().get(0));
									}
								}else{
									SamchatContactUserNameCardActivity.start(MainActivity.this, hcc.users.getusers().get(0),false);
								}
							}
						}, 0);
					}else{
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								Toast.makeText(MainActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
								isSending = false;
							}
						}, 0);
					}
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(MainActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(MainActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(MainActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(MainActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}



	private void logout() {
        removeLoginState();
        logout(this, false);
        NIMClient.getService(AuthService.class).logout();
    }

    private void removeLoginState() {
        Preferences.saveUserToken("");
        Preferences.saveUserAccount("");
    }
    private void kickOut(StatusCode code) {
        Preferences.saveUserToken("");

        if (code == StatusCode.PWD_ERROR) {
            LogUtil.e("Auth", "user password error");
        } else {
            LogUtil.i("Auth", "Kicked!");
        }
        logout();
    }

	private BroadcastReceiver networkBroadcast=new NetworkStateBroadcastReceiver();
	private void registerNetworkReceiver() {  
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(networkBroadcast, filter);  
	} 

	private void unregisterNetworkReceiver() {  
        this.unregisterReceiver(networkBroadcast);  
	}  

	private void registerObservers(boolean register) {
		NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onLineStatusObserver, register);
	}

	private Observer<StatusCode> onLineStatusObserver = new Observer<StatusCode>() {
		@Override
		public void onEvent(StatusCode code) {
			if (code.wontAutoLogin()) {
				LogUtil.ui("SDK will not try auto login again");
				kickOut(code);
			} else {
				if (code == StatusCode.LOGINED) {
					LogUtil.ui("SDK auto login succeed");
					initSamAutoLogin();
					//initGeTui();
				} 
			}
		}
	};

	@Override
	public void onDestroy() {
		unregisterNetworkReceiver();
		registerObservers(false);
		/*if(isGetuInited){
			LogUtil.e(TAG,"stop getui service");
			//PushManager.getInstance().turnOffPush(DemoCache.getContext());
			PushManager.getInstance().stopService(DemoCache.getContext());
			//registerGetuiReceiver(false);
			isGetuInited = false;
		}*/
		super.onDestroy();
	}

	/*private void initGeTui(){
		if(!isGetuInited){
			LogUtil.e(TAG,"init getui service");
			//registerGetuiReceiver(true);
			PushManager.getInstance().initialize(getApplicationContext());
			//PushManager.getInstance().turnOnPush(DemoCache.getContext());
			isGetuInited = true;
		}
	}*/
	
	private void initSamAutoLogin(){
		String account = Preferences.getUserAccount();
		String token = Preferences.getUserToken()+UuidFactory.getInstance().getDeviceId();
		LogUtil.i(TAG,"auto login account:"+account+" token:"+token);
		if(SamService.getInstance().get_current_user() == null || SamService.getInstance().get_current_token() == null){
			ContactUser cuser = SamchatUserInfoCache.getInstance().getUserByUniqueID(Long.valueOf(account));
			SamService.getInstance().set_current_user(cuser);
			SamService.getInstance().store_current_token(token);
		}
	}

	public void refreshToolBar(int position){
		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
			titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
			titlebar_right_text.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
			switch_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
			titlebar_right_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
		}else{
			getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
			titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
			titlebar_right_text.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
			switch_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
			titlebar_right_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
		}
		switch_icon.setImageResource(MainTab.getTabIcon(position));
		titlebar_name.setText(MainTab.getTabTitle(position));
		titlebar_right_icon.setImageResource(MainTab.getTabRightIcon(position));

		if(MainTab.isTabRightIconShow(position)){
			titlebar_right_icon.setVisibility(View.VISIBLE);
		}else{
			titlebar_right_icon.setVisibility(View.GONE);
		}

		if(MainTab.isTabRightTextShow(position)){
			titlebar_right_text.setVisibility(View.VISIBLE);
		}else{
			titlebar_right_text.setVisibility(View.GONE);
		}

		if(!MainTab.isTabRightIconShow(position) && !MainTab.isTabRightTextShow(position)){
			titlebar_right_layout.setVisibility(View.GONE);
		}else{
			titlebar_right_layout.setVisibility(View.VISIBLE);
		}
	}

	public void refreshTabUnreadCount(ModeEnum currentMode){
		if(currentMode == ModeEnum.CUSTOMER_MODE){
			ReminderManager.getInstance().updateRequestUnreadNum(request_unread_count_customer);
			ReminderManager.getInstance().updateReceivedAdvertisementUnreadNum(advertisement_unread_count_customer);
			ReminderManager.getInstance().updateSessionUnreadNum(chat_unread_count_customer);
			int sp_total_reminder = request_unread_count_sp + chat_unread_count_sp;
			//switch_reminder.setVisibility(sp_total_reminder > 0 ? View.VISIBLE : View.GONE);
			switch_reminder_icon.setVisibility(sp_total_reminder > 0 ? View.VISIBLE : View.GONE);
			if (sp_total_reminder > 0) {
				//switch_reminder.setText(String.valueOf(ReminderSettings.unreadMessageShowRule(sp_total_reminder)));
			}
		}else{
			ReminderManager.getInstance().updateRequestUnreadNum(request_unread_count_sp);
			ReminderManager.getInstance().updateReceivedAdvertisementUnreadNum(0);
			ReminderManager.getInstance().updateSessionUnreadNum(chat_unread_count_sp);
			int customer_total_reminder = request_unread_count_customer + advertisement_unread_count_customer+chat_unread_count_customer;
			//switch_reminder.setVisibility(customer_total_reminder > 0 ? View.VISIBLE : View.GONE);
			switch_reminder_icon.setVisibility(customer_total_reminder > 0 ? View.VISIBLE : View.GONE);
			if (customer_total_reminder > 0) {
				//switch_reminder.setText(String.valueOf(ReminderSettings.unreadMessageShowRule(customer_total_reminder)));
			}
		}
	}

	private void updateSpUnreadTotalCount(){
		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			int sp_total_reminder = request_unread_count_sp + chat_unread_count_sp;
			switch_reminder_icon.setVisibility(sp_total_reminder > 0 ? View.VISIBLE : View.GONE);
			//switch_reminder.setVisibility(sp_total_reminder > 0 ? View.VISIBLE : View.GONE);
			if (sp_total_reminder > 0) {
				//switch_reminder.setText(String.valueOf(ReminderSettings.unreadMessageShowRule(sp_total_reminder)));
			}
		}
	}

	
	private void updateCustomerUnreadTotalCount(){
		if(SamchatGlobal.getmode() == ModeEnum.SP_MODE){
			int customer_total_reminder = request_unread_count_customer + advertisement_unread_count_customer+chat_unread_count_customer;
			//switch_reminder.setVisibility(customer_total_reminder > 0 ? View.VISIBLE : View.GONE);
			switch_reminder_icon.setVisibility(customer_total_reminder > 0 ? View.VISIBLE : View.GONE);
			if (customer_total_reminder > 0) {
				//switch_reminder.setText(String.valueOf(ReminderSettings.unreadMessageShowRule(customer_total_reminder)));
			}
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

	public void switchMode(){
		LogUtil.e(TAG,"start switch samchat Mode");
		startSwitchProgress();
		sendSwitchModeBroadcast();
	}

	private void titlebarInit(){
		switch_icon = (ImageView) findViewById(R.id.switch_icon);
		switch_layout = (FrameLayout) findViewById(R.id.switch_layout);
		//switch_reminder = (TextView) findViewById(R.id.switch_reminder);
		switch_reminder_icon = (ReminderRedPointView) findViewById(R.id.switch_reminder_icon);
		titlebar_name = (TextView) findViewById(R.id.titlebar_name);
		titlebar_right_layout = (RelativeLayout) findViewById(R.id.titlebar_right_layout);
		titlebar_right_icon = (ImageView) findViewById(R.id.titlebar_right_icon);
		titlebar_right_text = (TextView) findViewById(R.id.titlebar_right_text);

		refreshToolBar(current_position);
		switch_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(SamService.getInstance().get_current_user().getusertype() == Constants.SAM_PROS){
					switchMode();
				}
			}
		});

		titlebar_right_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
					if(current_position == MainTab.SAMCHAT_REQUEST.tabIndex){

					}else if(current_position == MainTab.SAMCHAT_PUBLIC.tabIndex){
						SamchatSearchPublicActivity.start(MainActivity.this);
					}else if(current_position == MainTab.SAMCHAT_CHAT.tabIndex){

					}else if(current_position == MainTab.SAMCHAT_CONTACT.tabIndex){
						SamchatAddServiceProviderActivity.start(MainActivity.this);
					}else if(current_position == MainTab.SAMCHAT_SETTING.tabIndex){
						
					}
				}else{
					if(current_position == MainTab.SAMCHAT_REQUEST.tabIndex){

					}else if(current_position == MainTab.SAMCHAT_PUBLIC.tabIndex){
						LogUtil.i(TAG,"send BROADCAST_POST_ADV");
						Intent intent = new Intent();
						intent.setAction(Constants.BROADCAST_POST_ADV);
						sendbroadcast(intent);
					}else if(current_position == MainTab.SAMCHAT_CHAT.tabIndex){

					}else if(current_position == MainTab.SAMCHAT_CONTACT.tabIndex){
						SamchatAddCustomerActivity.start(MainActivity.this);
					}else if(current_position == MainTab.SAMCHAT_SETTING.tabIndex){

					}

				}
				
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
		if(current_position == MainTab.SAMCHAT_CONTACT.tabIndex){
			titlebar_right_icon.setVisibility(View.VISIBLE);
		}else{
			titlebar_right_icon.setVisibility(View.GONE);
		}
	}

	public int getCurrentPostition(){
		return current_position;
	}

	public void initMode(){
		SamchatGlobal.setmode(ModeEnum.typeOfValue(Preferences.getMode())); 
	}
	public void saveMode(ModeEnum mode){
		Preferences.saveMode(ModeEnum.valueOfType(mode));
	}

	//NimUIKitInterface
	public void startMemberSelectActivity(Context context,List<String> selected,int requestCode){
		SamchatMemberSelectActivity.startActivityForResult(context, 
			new SamchatMemberSelectActivity.Option(1,NimConstants.MAX_TEAM_MEMBER_NUMBERS, selected,true,
				SamchatMemberSelectActivity.MemberSelectType.BUDDY_CUSTOMER), 
			requestCode);
	}
	
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

	public void queryMsgSession(String session_id, int mode, NIMCallback callback){
		SamDBManager.getInstance().queryMsgSession( session_id,  mode, callback);
	}

	public void deleteMessage(String session_id, int mode,IMMessage msg){
		SamDBManager.getInstance().asyncDeleteMessage(session_id, mode, msg);
	}

	public void deleteSendAdvertisementMessage(String session_id, int mode,IMMessage im){
		SamDBManager.getInstance().asyncDeleteSendAdvertisementMessage( session_id,  mode, im);
	}

	public void lastMsgResending(String account, int mode, IMMessage im){
		SamDBManager.getInstance().asyncNoticeLastMsgSending( account,  mode, im);
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

	public void asyncUpdateReceivedQuestionStatusToResponse(long question_id){
		SamDBManager.getInstance().asyncUpdateReceivedQuestionStatusToResponse(question_id);
	}

	public void asyncUpdateReceivedAdvertisementStatusToResponse(long unique_id, long adv_id){
		SamDBManager.getInstance().asyncUpdateReceivedAdvertisementStatusToResponse(unique_id,adv_id);
	}
	
	public void registerSendAdvertisementObserver(SamchatObserver < IMMessage > observer, boolean register){
		SamDBManager.getInstance().registerSendAdvertisementObserver(observer, register);
	}

    public void registerSendAdvertisementStatusObserver(SamchatObserver < IMMessage > observer, boolean register){
        SamDBManager.getInstance().registerSendAdvertisementStatusObserver(observer, register);
    }

	public void registerClearHistoryObserver(SamchatObserver<SessionBasicInfo> observer, boolean register){
		SamDBManager.getInstance().registerClearHistoryObserver(observer, register);
	}

	public void asyncClearChatHisotry(final SessionTypeEnum type, final String account, final int mode, final NIMCallback callback){
		SamDBManager.getInstance().asyncClearChatHisotry(type, account, mode, callback);
	}

	private long stringTolong(String s){
	 	long ret = -1;
		String account = s;
		if (s.startsWith(NimConstants.PUBLIC_ACCOUNT_PREFIX)) {
            account = s.substring(s.indexOf(NimConstants.PUBLIC_ACCOUNT_PREFIX) + NimConstants.PUBLIC_ACCOUNT_PREFIX.length());
		}
		try{
			ret = Long.valueOf(account);
		}catch(Exception e){
			e.printStackTrace();
			return ret;
		}
		return ret;
	 }
	
	public String getServiceCategory(String account){
       ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(stringTolong(account));
		if(user != null){
			return user.getservice_category();
		}

		Contact contact = ContactDataCache.getInstance().getContactByUniqueID(stringTolong(account));
		if(contact != null){
			return contact.getservice_category();
		}

		contact = CustomerDataCache.getInstance().getCustomerByUniqueID(stringTolong(account));
		if(contact != null){
			return contact.getservice_category();
		}	
						
		FollowedSamPros fsp = FollowDataCache.getInstance().getFollowSPByUniqueID(stringTolong(account));
		if(fsp !=null){
			return fsp.getservice_category();
		}
		return null;
    }

	public void onAvatarClick(String clickAccount, String creator){
		if(clickAccount.equals(NimUIKit.getAccount())){
			return;
		}

		ContactUser user = SamchatUserInfoCache.getInstance().getUserByAccount(clickAccount);
		String username = BasicUserInfoHelper.getUserName(clickAccount);

		if(clickAccount.equals(creator)){
			//click owner
			if(user != null)
				SamchatContactUserSPNameCardActivity.start(MainActivity.this,user);
			else
				SamchatContactUserSPNameCardActivity.start(MainActivity.this, clickAccount, username);
		}else{
			if(creator.equals(NimUIKit.getAccount())){
				if(user != null)
					SamchatContactUserNameCardActivity.start(MainActivity.this,user,false);
				else
					SamchatContactUserNameCardActivity.start(MainActivity.this, clickAccount, username, false);
			}else{
				if(user != null)
					SamchatContactUserNameCardActivity.start(MainActivity.this,user,true);
				else
					SamchatContactUserNameCardActivity.start(MainActivity.this, clickAccount, username, true);
			}
		}
	}

	public void selectForwardMember(Context context, int requestCode){
		SamchatMemberSelectActivity.Option option=null;
		if(NimUIKit.getCallback().getCurrentMode() == ModeEnum.CUSTOMER_MODE.getValue()){
			option = new SamchatMemberSelectActivity.Option(1,1,null,false,SamchatMemberSelectActivity.MemberSelectType.BUDDY_SP);
		}else{
			option = new SamchatMemberSelectActivity.Option(1,1,null,false,SamchatMemberSelectActivity.MemberSelectType.BUDDY_CUSTOMER);
		}
		SamchatMemberSelectActivity.startActivityForResult(context,option, requestCode);
	}

	public void selectForwardTeam(Context context, int requestCode){
		SamchatMemberSelectActivity.Option option = new SamchatMemberSelectActivity.Option(1,1,null,false,SamchatMemberSelectActivity.MemberSelectType.TEAM);
		SamchatMemberSelectActivity.startActivityForResult(context,option, requestCode);
	}
	
/*SAMC_END(...)*/
}
