package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.hpl.sparta.Text;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
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
import android.graphics.Typeface;
import com.android.samservice.HttpCommClient;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;

public class SamchatLoginActivity extends Activity {
	private static final String TAG = SamchatLoginActivity.class.getSimpleName();
	private static final String KICK_OUT = "KICK_OUT";
	private final int BASIC_PERMISSION_REQUEST_CODE = 100;
	
	public static final int CONFIRM_ID_SELECT_COUNTRY_CODE = 200;

	private TextView countrycode_textview;
	private EditText logininput_edittext;
	private RelativeLayout hidden_layout;
	private ImageView hidden_iv;
	private EditText password_edittext;
	private TextView signin_textview;
	private TextView signup_textview;
	private TextView forgot_pwd_textview;

	private String countrycode = "USA";
	private String input;
	private String password;

	private boolean isPwdShown = false;
	private boolean input_ready=false;
	private boolean password_ready=false;

	private AbortableFuture<LoginInfo> loginRequest;

	private boolean isLogining = false;

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
		isBroadcastRegistered = true;
	}
		

	
	private void unregisterBroadcastReceiver(){
		if(isBroadcastRegistered){
			broadcastManager.unregisterReceiver(broadcastReceiver);
			isBroadcastRegistered = false;
		}
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_login_activity);

		
		/*clear all persist storage info*/
		Preferences.saveUserAlias("");
		Preferences.saveMode(ModeEnum.CUSTOMER_MODE.getValue());
		Preferences.clearSyncDate();

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
				Manifest.permission.READ_PHONE_STATE
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
		//Toast.makeText(this, getString(R.string.samchat_permission_refused_storage), Toast.LENGTH_SHORT).show();
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

	private void setupLoginPanel() {
		countrycode_textview = (TextView) findViewById(R.id.countrycode);
		logininput_edittext = (EditText) findViewById(R.id.logininput);
		hidden_layout = (RelativeLayout) findViewById(R.id.hidden_layout);
		hidden_iv = (ImageView) findViewById(R.id.hidden);
		password_edittext = (EditText) findViewById(R.id.password);
		signin_textview = (TextView) findViewById(R.id.signin);
		signup_textview = (TextView) findViewById(R.id.signup);
		forgot_pwd_textview = (TextView) findViewById(R.id.forgot_pwd);

		setupCountrycodeClick();
		setupLoginInputEditClick();
		setupHiddenClick();
		setupPasswordEditClick();
		setupSignInClick();
		setupSignUpClick();
		setupForgotPwdClick();
		
    }


/************************country code button setup***************************************/
	private void updateCountryCode(){
		countrycode_textview.setText("+" + countrycode);
	}

	private void setupCountrycodeClick(){
		countrycode_textview.setTypeface(Typeface.SANS_SERIF);
		countrycode_textview.setText(countrycode);
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatLoginActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
	}
	
/***********************************************************************************/

/************************login input setup***************************************/
	private TextWatcher logininput_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			input = logininput_edittext.getText().toString().trim();
			input_ready = input.length()>=Constants.MIN_USERNAME_LENGTH;
			updateSigninButton();
		}
	};
	
	private void setupLoginInputEditClick(){
		logininput_edittext.addTextChangedListener(logininput_textWatcher);
		logininput_edittext.setTypeface(Typeface.SANS_SERIF);
	}
	
/***********************************************************************************/

/************************password view setup********************************************/
	private TextWatcher password_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			password = password_edittext.getText().toString();
			password_ready = password.length()>=Constants.MIN_PASSWORD_LENGTH;
			updateSigninButton();
			password_edittext.setTypeface(Typeface.SANS_SERIF);
			if(password_ready){
				if(isPwdShown){
					hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_shown);
				}else{
					hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_filled2);
				}
			}else{
				hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
			}
		}
	};

	private void togglePasswordVisibility(){
		if(!isPwdShown){
			password_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			Editable etable = password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isPwdShown = true;
			if(password_ready){
				hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_shown);
			}else{
				hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
			}
		}else{
			password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			Editable etable = password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
			isPwdShown = false;
			if(password_ready){
				hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_filled2);
			}else{
				hidden_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
			}
		}
		password_edittext.setTypeface(Typeface.SANS_SERIF);
	}

	private void setupHiddenClick(){
		hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				togglePasswordVisibility();
			}
		});
	}

	private void setupPasswordEditClick(){
		password_edittext.addTextChangedListener(password_textWatcher);
		password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		password_edittext.setTypeface(Typeface.SANS_SERIF);
	}
/***********************************************************************************/

/************************signin view setup********************************************/
	private void updateSigninButton(){
		boolean enable = input_ready & password_ready;
		signin_textview.setEnabled(enable);
		if(enable){
			signin_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green_gradient);
		}else{
			signin_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_grey);
		}
	}

	private void setupSignInClick(){
		signin_textview.setTypeface(Typeface.SANS_SERIF);
		signin_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isLogining){
					return;
				}
				
				loginSamchat();
			}
		});
		updateSigninButton();
	}

/***********************************************************************************/

/************************signup view setup********************************************/
	private void setupSignUpClick(){
		signup_textview.setTypeface(Typeface.SANS_SERIF);
		signup_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatRegisterCodeRequestActivity.start(SamchatLoginActivity.this,Constants.FROM_SIGNUP);
			}
		});
	}

/***********************************************************************************/

/************************signup view setup********************************************/
	private void setupForgotPwdClick(){
		forgot_pwd_textview.setTypeface(Typeface.SANS_SERIF);
		forgot_pwd_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatRegisterCodeRequestActivity.start(SamchatLoginActivity.this,Constants.FROM_FORGETPWD);
			}
		});
	}

/***********************************************************************************/

/************************transaction implement********************************************/	
	private void loginSamchat() {
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_loginging), false, null).setCanceledOnTouchOutside(false);
		String cc="";
		if(!countrycode.equals("USA")){
			cc = countrycode;
		}else{
			cc = "1";
		}
		SamService.getInstance().signin(cc, input, password, UuidFactory.getInstance().getDeviceId(), 
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							login((HttpCommClient)obj);
						}
					});
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatLoginActivity.this,code);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatLoginActivity.this, null,
									error.reminder, getString(R.string.samchat_ok), true, null);
							isLogining = false;
						}
					});
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatLoginActivity.this,code);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatLoginActivity.this, null,
									error.reminder, getString(R.string.samchat_ok), true, null);
							isLogining = false;
						}
					});
				}

		});
	}

	private void login(HttpCommClient hcc) {
		final String account = ""+hcc.userinfo.getunique_id();
		final String token = hcc.token_id;
		final String final_token = token + UuidFactory.getInstance().getDeviceId();

		SamDBManager.getInstance().registerObservers(true);
		DemoCache.setAccount(account);
		loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(account, final_token));
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
				  UserPreferences.setRequestToggle(SamService.getInstance().get_current_user().getquestion_notify()==1);
					SamService.getInstance().initDao(StringUtil.makeMd5(account));
					SamchatDataCacheManager.buildDataCache();
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
					isLogining = false;
            }

            @Override
            public void onException(Throwable exception) {
            		onLoginDone();
                Toast.makeText(SamchatLoginActivity.this, R.string.samchat_login_exception, Toast.LENGTH_LONG).show();
					isLogining = false;
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

