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
import android.widget.EditText;
import android.widget.ImageView;
import android.text.InputType;
import android.text.Selection;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;

public class SamchatLoginActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatLoginActivity.class.getSimpleName();
	private static final String KICK_OUT = "KICK_OUT";
	private final int BASIC_PERMISSION_REQUEST_CODE = 110;
	
	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 0x100;

	private TextView countrycode_textview;
	private EditText logininput_edittext;
	private ImageView hidden_imageview;
	private EditText password_edittext;
	private TextView signin_textview;
	private TextView signup_textview;
	private TextView forgot_pwd_textview;

	private String countrycode = "1";

	private boolean isPwdShown = false;

	private AbortableFuture<LoginInfo> loginRequest;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatLoginActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SIGN_IN_ALREADY);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SIGN_IN_ALREADY)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}
   
    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, SamchatLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
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
		setContentView(R.layout.samchat_login_activity);

		requestBasicPermission();

		onParseIntent();
		setupLoginPanel();

		registerBroadcastReceiver();	
	}

	@Override
	protected void onDestroy() {
        super.onDestroy();
		unregisterBroadcastReceiver();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_SELECT_COUNTRY_CODE && resultCode == RESULT_OK){
			countrycode = data.getStringExtra(Constants.CONFIRM_COUNTRYCODE);
			updateCountryCode();
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}


    private void requestBasicPermission() {
        MPermission.with(SamchatLoginActivity.this)
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
        Toast.makeText(this, getString(R.string.permision_apply_succeed), Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed(){
        Toast.makeText(this, getString(R.string.permision_apply_failed), Toast.LENGTH_SHORT).show();
    }

    private void onParseIntent() {
        if (getIntent().getBooleanExtra(KICK_OUT, false)) {
            int type = NIMClient.getService(AuthService.class).getKickedClientType();
            String client;
            switch (type) {
                case ClientType.Web:
                    client = getString(R.string.samchat_cient_type_web);
                    break;
                case ClientType.Windows:
                    client = getString(R.string.samchat_cient_type_pc);
                    break;
                case ClientType.REST:
                    client = getString(R.string.samchat_cient_type_server);
                    break;
                default:
                    client = getString(R.string.samchat_cient_type_mobile);
                    break;
            }
            EasyAlertDialogHelper.showOneButtonDiolag(SamchatLoginActivity.this, getString(R.string.kickout_notify),
                    String.format(getString(R.string.kickout_content), client), getString(R.string.ok), true, null);
        }
    }

	private void updateCountryCode(){
		countrycode_textview.setText("+" + countrycode);
	}

	private void setupCountrycodeClick(){
		updateCountryCode();
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatLoginActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
	}

	private void updateSigninButton(boolean enable){
		signin_textview.setEnabled(enable);
	}
	
	private TextWatcher logininput_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			boolean isEnable = logininput_edittext.getText().length()>0 && password_edittext.getText().length()>0 ;
			updateSigninButton(isEnable);
		}
	};
	
	private void setupLoginInputClick(){
		logininput_edittext.addTextChangedListener(logininput_textWatcher);
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
		hidden_imageview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updatePasswordVisibility();
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
			boolean isEnable = password_edittext.getText().length()>0 && password_edittext.getText().length()>0 ;
			updateSigninButton(isEnable);
		}
	};
	
	private void setupPasswordEditClick(){
		password_edittext.addTextChangedListener(password_textWatcher);
		password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	private void setupSignInClick(){
		signin_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				login();
			}
		});
		updateSigninButton(false);
	}

	private void setupSignUpClick(){
		signup_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatRegisterCodeRequestActivity.start(SamchatLoginActivity.this);
			}
		});
	}

	private void setupForgotPwdClick(){
		forgot_pwd_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				
			}
		});
	}

	private void setupLoginPanel() {
		countrycode_textview = findView(R.id.countrycode);
		logininput_edittext = findView(R.id.logininput);
		hidden_imageview = findView(R.id.hidden);
		password_edittext = findView(R.id.password);
		signin_textview = findView(R.id.signin);
		signup_textview = findView(R.id.signup);
		forgot_pwd_textview = findView(R.id.forgot_pwd);

		setupCountrycodeClick();
		setupLoginInputClick();
		setupHiddenClick();
		setupPasswordEditClick();
		setupSignInClick();
		setupSignUpClick();
		setupForgotPwdClick();
		
    }

    private void login() {
        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (loginRequest != null) {
                    loginRequest.abort();
                    onLoginDone();
                }
            }
        }).setCanceledOnTouchOutside(false);

        final String account = logininput_edittext.getEditableText().toString().toLowerCase();
        final String token = tokenFromPassword(password_edittext.getEditableText().toString());

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

                // 初始化消息提醒
                NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

                // 初始化免打扰
                if (UserPreferences.getStatusConfig() == null) {
                    UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
                }
                NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());

                // 构建缓存
                DataCacheManager.buildDataCacheAsync();
					/*SAMC_BEGIN(build samchat cache)*/
					DemoCache.setTAccount(account);
					SamService.getInstance().initDao(StringUtil.makeMd5(account));
					SamchatDataCacheManager.buildDataCache();
					
					/*SAMC_END(build samchat cache)*/

                // 进入主界面
                MainActivity.start(SamchatLoginActivity.this, null);
                finish();
            }

            @Override
            public void onFailed(int code) {
                onLoginDone();
                if (code == 302 || code == 404) {
                    Toast.makeText(SamchatLoginActivity.this, R.string.samchat_input_error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SamchatLoginActivity.this, getString(R.string.samchat_login_failed) + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(SamchatLoginActivity.this, R.string.samchat_login_exception, Toast.LENGTH_LONG).show();
                onLoginDone();
            }
        });
    }


    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void saveLoginInfo(final String account, final String token) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
    }

   private String tokenFromPassword(String password) {
        String appKey = readAppKey(this);
        boolean isDemo = "45c6af3c98409b18a84451215d0bdd6e".equals(appKey)
                || "fe416640c8e8a72734219e1847ad2547".equals(appKey);

        return isDemo ? MD5.getStringMD5(password) : password;
    }

    private static String readAppKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

