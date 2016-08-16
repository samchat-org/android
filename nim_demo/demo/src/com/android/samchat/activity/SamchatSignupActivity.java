package com.android.samchat.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.android.samchat.service.SamDBManager;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.text.InputType;
import android.text.Selection;
import com.android.samservice.HttpCommClient;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;


public class SamchatSignupActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatSignupActivity.class.getSimpleName();
	private static final String COUNTRYCODE="countrycode";
	private static final String CELLPHONE="cellphone";
	private static final String VERIFYCODE="verifycode";
	private static final String DEVICEID="deviceid";

	private FrameLayout back_arrow_layout;
	private EditText username_edittext;
	private EditText password_edittext;
	private RelativeLayout hidden_layout;
	private ImageView selected_imageview;
	private TextView done_textview;

	private boolean ready_username=false;
	private boolean ready_password=false;
	private boolean ready_selected=true;

	private boolean isPwdShown=false;

	private String username;
	private String password;
	private String countrycode;
	private String cellphone;
	private String verifycode;
	private String deviceid;

	private AbortableFuture<LoginInfo> loginRequest;
	
	
	public static void start(Context context,String countrycode, String cellphone, String verifycode, String deviceid) {
		Intent intent = new Intent(context, SamchatSignupActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(COUNTRYCODE,countrycode);
		intent.putExtra(CELLPHONE,cellphone);
		intent.putExtra(VERIFYCODE,verifycode);
		intent.putExtra(DEVICEID,deviceid);
		context.startActivity(intent);
	}

	private void onParseIntent() {
		countrycode = getIntent().getStringExtra(COUNTRYCODE);
		cellphone = getIntent().getStringExtra(CELLPHONE);
		verifycode = getIntent().getStringExtra(VERIFYCODE);
		deviceid = getIntent().getStringExtra(DEVICEID);
	}

	@Override
	protected boolean displayHomeAsUpEnabled() {
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_signup_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();
		setupSignUpPanel();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private TextWatcher username_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			ready_username = (username_edittext.getText().length()>=3 && username_edittext.getText().toString().trim().length()>=3);
			updateDoneButton();
			if(ready_username){
				username = username_edittext.getText().toString().trim();
			}
		}
	};

	private void setupUsernameEditClick(){
		username_edittext.addTextChangedListener(username_textWatcher);
	}

	private void setupDoneClick(){
		done_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				signup();
			}
		});

		updateDoneButton();
	}

	private void updateDoneButton(){
		done_textview.setEnabled(ready_username & ready_password & ready_selected);
	}

	private void updateSelectImage(){
		if(ready_selected){
			selected_imageview.setImageResource(R.drawable.btn_loaction_pressed);
		}else{
			selected_imageview.setImageResource(R.drawable.btn_loaction_normal);
		}
	}

	private void setupSelectedClick(){
		updateSelectImage();
		selected_imageview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(ready_selected){
					ready_selected = false;
				}else{
					ready_selected = true;
				}
				updateSelectImage();
				updateDoneButton();
			}
		});
	}

	private TextWatcher password_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			ready_password = (password_edittext.getText().length()>=6 && password_edittext.getText().toString().trim().length()>=6);
			updateDoneButton();
			if(ready_password){
				password = password_edittext.getText().toString().trim();
			}
		}
	};

	private void setupPasswordEditClick(){
		password_edittext.addTextChangedListener(password_textWatcher);
		password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	private void updatePasswordVisibility(){
		if(!isPwdShown){
			password_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			Editable etable = password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isPwdShown = true;
		}else{
			password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			Editable etable = password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isPwdShown = false;
		}
	}

	private void setupHiddenClick(){
		hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updatePasswordVisibility();
			}
		});
	}
	
	private void setupSignUpPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		username_edittext = findView(R.id.username);
		password_edittext = findView(R.id.password);
		hidden_layout = findView(R.id.hidden_layout);
		selected_imageview = findView(R.id.selected);
		done_textview = findView(R.id.done);

		setupBackArrowClick();
		setupUsernameEditClick();
		setupPasswordEditClick();
		setupHiddenClick();
		setupSelectedClick();
		setupDoneClick();

	}

	private void saveLoginInfo(final String account, final String token) {
		Preferences.saveUserAccount(account);
		Preferences.saveUserToken(token);
	}

	private void login(final String account, final String token) {
        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), false, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (loginRequest != null) {
                    loginRequest.abort();
                    onLoginDone();
                }
            }
        }).setCanceledOnTouchOutside(false);

        /*SAMC_BEGIN(register message before login)*/
        SamDBManager.getInstance().registerObservers(false);
        SamDBManager.getInstance().registerObservers(true);
		  DemoCache.setTAccount(account);
		  /*SAMC_BEGIN(register message before login)*/
        loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(account, token));
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                LogUtil.i(TAG, "login success");

                onLoginDone();
                DemoCache.setAccount(account);
                saveLoginInfo(account, token);

                NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

                if (UserPreferences.getStatusConfig() == null) {
                    UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
                }
                NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());

                DataCacheManager.buildDataCacheAsync();
					/*SAMC_BEGIN(build samchat cache)*/
					DemoCache.setTAccount(account);
					SamService.getInstance().initDao(StringUtil.makeMd5(account));
					SamchatDataCacheManager.buildDataCache();
					
					/*SAMC_END(build samchat cache)*/

                // 进入主界面
                MainActivity.start(SamchatSignupActivity.this, null);
                finish();
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_SIGN_IN_ALREADY);
                sendbroadcast(intent);
            }

            @Override
            public void onFailed(int code) {
                onLoginDone();
                Toast.makeText(SamchatSignupActivity.this, getString(R.string.samchat_login_failed) + code, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_SIGN_UP_ALREADY);
                sendbroadcast(intent);
            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(SamchatSignupActivity.this, R.string.samchat_login_exception, Toast.LENGTH_LONG).show();
                onLoginDone();
                Intent intent = new Intent();
                intent.setAction(Constants.BROADCAST_SIGN_UP_ALREADY);
                sendbroadcast(intent);
            }
        });
    }


    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }
	
	private void onSignupDone(final Object obj){
		HttpCommClient hcc = (HttpCommClient)obj;
		final String account = ""+hcc.userinfo.getunique_id();
		final String token = hcc.token_id + deviceid;
		LogUtil.e("test","signup token:"+token);
		saveLoginInfo(account,token);

		login(account,token);
	}

	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void signup(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_signuping), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().signup(countrycode,cellphone,verifycode,password,username,deviceid,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							onSignupDone(obj);
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSignupActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSignupActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSignupActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSignupActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

		});
	}

}


