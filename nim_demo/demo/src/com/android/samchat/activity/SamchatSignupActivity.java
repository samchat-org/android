package com.android.samchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.info.ContactUser;
import com.android.samservice.type.TypeEnum;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.main.activity.WelcomeActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.android.samchat.service.SamDBManager;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.text.InputType;
import android.text.Selection;
import com.android.samservice.HttpCommClient;
import android.support.v4.content.LocalBroadcastManager;
import android.graphics.Typeface;

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
	private TextView done_textview;
	private TextView terms_textview;
	private ImageView hidden_icon_imageview;
	private ImageView username_icon_imageview;
	private ImageView username_ok_imageview;
	private ImageView password_ok_imageview;
	private TextView Oops_textview;


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

	private boolean isSignuping = false;
	
	
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getHandler().removeCallbacksAndMessages(null);
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
			if((username_edittext.getText().toString().trim().length()>=Constants.MIN_USERNAME_LENGTH)){
				username = username_edittext.getText().toString().trim();
				username_icon_imageview.setImageResource(R.drawable.samchat_user_icon_ok);
				showOops(false);
				updateUsernameOKShow(false);
				ready_username = false;
				updateDoneButton();
				getHandler().removeCallbacksAndMessages(null);
				postCountdownCheckUsername();
			}else{
				username = username_edittext.getText().toString().trim();
				username_icon_imageview.setImageResource(R.drawable.samchat_user_icon);
				showOops(false);
				updateUsernameOKShow(false);
				ready_username = false;
				updateDoneButton();
				getHandler().removeCallbacksAndMessages(null);
			}
		}
	};

	private void setupUsernameEditClick(){
		username_edittext.addTextChangedListener(username_textWatcher);
		username_edittext.setTypeface(Typeface.SANS_SERIF);
	}

	private boolean isPasswordValid(){
		if(password.indexOf(" ") != -1){
			return false;
		}else{
			return true;
		}
	}
	private void setupDoneClick(){
		done_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSignuping){
					return;
				}
				
				if(!isPasswordValid()){
					Toast.makeText(SamchatSignupActivity.this, R.string.samchat_password_warning, Toast.LENGTH_SHORT).show();
					return;
				}

				isSignuping = true;
				signup();
			}
		});

		updateDoneButton();
	}

	private void updateDoneButton(){
		if(ready_username & ready_password & ready_selected){
			done_textview.setEnabled(true);
			done_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green);
		}else{
			done_textview.setEnabled(false);
			done_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green_disable);
		}
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
			ready_password = password_edittext.getText().length()>=Constants.MIN_PASSWORD_LENGTH;
			updateDoneButton();
			if(ready_password){
				password = password_edittext.getText().toString();
				hidden_icon_imageview.setImageResource(R.drawable.samchat_hidden_ok);
				password_ok_imageview.setVisibility(View.VISIBLE);
			}else{
				hidden_icon_imageview.setImageResource(R.drawable.samchat_hidden);
				password_ok_imageview.setVisibility(View.GONE);
			}
		}
	};

	private void setupPasswordEditClick(){
		password_edittext.addTextChangedListener(password_textWatcher);
		password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		password_edittext.setTypeface(Typeface.SANS_SERIF);
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
		password_edittext.setTypeface(Typeface.SANS_SERIF);
	}

	private void setupHiddenClick(){
		hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updatePasswordVisibility();
			}
		});
	}

	private void setupTermsClick(){
		terms_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatTermsActivity.start(SamchatSignupActivity.this);
			}
		});
	}
	
	private void setupSignUpPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		username_edittext = findView(R.id.username);
		password_edittext = findView(R.id.password);
		hidden_layout = findView(R.id.hidden_layout);
		done_textview = findView(R.id.done);
		terms_textview = findView(R.id.terms);
		hidden_icon_imageview	= findView(R.id.hidden_icon);
		username_icon_imageview = findView(R.id.username_icon);
		username_ok_imageview = findView(R.id.username_ok);
		password_ok_imageview = findView(R.id.password_ok);
		Oops_textview = findView(R.id.Oops);

		setupBackArrowClick();
		setupUsernameEditClick();
		setupPasswordEditClick();
		setupHiddenClick();
		setupDoneClick();
		setupTermsClick();
	}

	private void showOops(boolean show){
		if(show){
			Oops_textview.setVisibility(View.VISIBLE);
			username_ok_imageview.setImageResource(R.drawable.samchat_warning);
			username_ok_imageview.setVisibility(View.VISIBLE);
			username_icon_imageview.setImageResource(R.drawable.samchat_user_icon_warning);
			username_edittext.setTextColor(getResources().getColor(R.color.color_orange_f69b5a));
		}else{
			Oops_textview.setVisibility(View.GONE);
			username_ok_imageview.setImageResource(R.drawable.samchat_ok);
			username_edittext.setTextColor(getResources().getColor(R.color.black));
		}
	}

	private void updateUsernameOKShow(boolean show){
		username_ok_imageview.setVisibility(show ? View.VISIBLE:View.GONE);
	}

	

/******************************Data Flow Control*************************************************************/
	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void saveLoginInfo(final String account, final String token) {
		Preferences.saveUserAccount(account);
		Preferences.saveUserToken(token);
	}

	private void login(final String account, final String token) {
        DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_loginging), false, new DialogInterface.OnCancelListener() {
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
		  DemoCache.setAccount(account);
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
					finish();
            }

            @Override
            public void onException(Throwable exception) {
					onLoginDone();
                Toast.makeText(SamchatSignupActivity.this, R.string.samchat_login_exception, Toast.LENGTH_LONG).show();
					finish();
            }
        });
    }


    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }
	
	private void onSignupDone(final Object obj){
		HttpCommClient hcc = (HttpCommClient)obj;
		String account = ""+hcc.userinfo.getunique_id();
		String token = hcc.token_id;
		String final_token = hcc.token_id + deviceid;
		saveLoginInfo(account,token);

		login(account,final_token);
		//autoLogin(account, final_token);
	}

	public LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token+ UuidFactory.getInstance().getDeviceId());
        } else {
            return null;
        }
    }

	private void autoLogin( String account,  String token){
		DemoCache.setAccount(account);
		NIMClient.init(DemoCache.getContext(), new LoginInfo(account, token), DemoCache.getApp().getOptions());
		
		NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
		if (UserPreferences.getStatusConfig() == null) {
			UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
		}
		NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());
		if(!TextUtils.isEmpty(DemoCache.getAccount())){
			SamService.getInstance().initDao(StringUtil.makeMd5(DemoCache.getAccount()));
			DataCacheManager.buildDataCacheAsync();
			SamchatDataCacheManager.buildDataCache(); // build data cache on auto login
			if(SamService.getInstance().get_current_user() == null || SamService.getInstance().get_current_token() == null){
				ContactUser cuser = SamchatUserInfoCache.getInstance().getUserByUniqueID(Long.valueOf(account));
				SamService.getInstance().set_current_user(cuser);
				SamService.getInstance().store_current_token(token);
			}
			SamDBManager.getInstance().registerObservers(true);
		}
		MainActivity.start(SamchatSignupActivity.this, null);
		finish();
		Intent intent = new Intent();
		intent.setAction(Constants.BROADCAST_SIGN_IN_ALREADY);
		sendbroadcast(intent);
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
							isSignuping = false;
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
							isSignuping = false;
						}
					}, 0);
				}

		});
	}


	private void postCountdownCheckUsername(){
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				checkUsername(username);
			}
		}, 2000);
	}

	private void checkUsername(final String checkname){
		SamService.getInstance().query_user_without_token(TypeEnum.USERNAME, null,  null, checkname, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
                        public void run(){
                            if(!checkname.equals(username)){
                                return;
                            }

                            HttpCommClient hcc = (HttpCommClient)obj;
                            if(hcc.users!=null && hcc.users.getcount()>0){
                                //oops, username has been reigistered
                                showOops(true);
                                ready_username = false;
                                updateDoneButton();
                            }else{
                                showOops(false);
                                ready_username = true;
                                updateUsernameOKShow(true);
                                updateDoneButton();
                            }
                        }

					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					getHandler().postDelayed(new Runnable(){
                        public void run(){
						    if(checkname.equals(username)){
						    	showOops(false);
						    	updateUsernameOKShow(true);
							    ready_username = true;
							    updateDoneButton();
						    }
					    }
                    }, 0);
				}

				@Override
				public void onError(int code) {
                    getHandler().postDelayed(new Runnable(){
                        public void run(){
                            if(checkname.equals(username)){
                                showOops(false);
                                updateUsernameOKShow(true);
                                ready_username = true;
                                updateDoneButton();
                            }
                        }
                    }, 0);
                }
		});
	}

}


