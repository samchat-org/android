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
import com.android.samchat.R;
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
import android.graphics.Typeface;

public class SamchatUpdatePasswordActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatUpdatePasswordActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private EditText old_password_edittext;
	private RelativeLayout old_hidden_layout;
	private EditText new_password_edittext;
	private RelativeLayout new_hidden_layout;

	private TextView done_textview;

	private boolean ready_old_password=false;
	private boolean ready_new_password=false;

	private boolean isOldPwdShown=false;
	private boolean isNewPwdShown=false;

	private String old_password;
	private String new_password;

	private boolean isUpdating = false;
	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatUpdatePasswordActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
		setContentView(R.layout.samchat_updatepassword_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		old_password_edittext = findView(R.id.old_password);
		old_hidden_layout = findView(R.id.new_hidden_layout);
		new_password_edittext = findView(R.id.new_password);
		new_hidden_layout = findView(R.id.new_hidden_layout);
		
		done_textview = findView(R.id.done);

		setupBackArrowClick();
		
		setupOldPasswordEditClick();
		setupOldHiddenClick();
		
		setupNewPasswordEditClick();
		setupNewHiddenClick();

		setupDoneClick();

	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private boolean isPasswordValid(){
		if(new_password.indexOf(" ") != -1){
			return false;
		}else{
			return true;
		}
	}
	private void setupDoneClick(){
		done_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isUpdating){
					return;
				}
				
				if(!isPasswordValid()){
					Toast.makeText(SamchatUpdatePasswordActivity.this, R.string.samchat_password_warning, Toast.LENGTH_SHORT).show();
					return;
				}
				isUpdating = true;
				updatePassword();
			}
		});

		updateDoneButton();
	}

	private void updateDoneButton(){
		done_textview.setEnabled(ready_old_password & ready_new_password);
	}


	private TextWatcher old_password_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			ready_old_password = old_password_edittext.getText().length()>=Constants.MIN_PASSWORD_LENGTH;
			updateDoneButton();
			if(ready_old_password){
				old_password = old_password_edittext.getText().toString();
			}
		}
	};

	private void setupOldPasswordEditClick(){
		old_password_edittext.addTextChangedListener(old_password_textWatcher);
		old_password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		old_password_edittext.setTypeface(Typeface.SANS_SERIF);
	}

	private void updateOldPasswordVisibility(){
		if(!isOldPwdShown){
			old_password_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			Editable etable = old_password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isOldPwdShown = true;
		}else{
			old_password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			Editable etable = old_password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isOldPwdShown = false;
		}
	}

	private void setupOldHiddenClick(){
		old_hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updateOldPasswordVisibility();
			}
		});
	}

	private TextWatcher new_password_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			ready_new_password = new_password_edittext.getText().length()>=Constants.MIN_PASSWORD_LENGTH;
			updateDoneButton();
			if(ready_new_password){
				new_password = new_password_edittext.getText().toString();
			}
		}
	};

	private void setupNewPasswordEditClick(){
		new_password_edittext.addTextChangedListener(new_password_textWatcher);
		new_password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		new_password_edittext.setTypeface(Typeface.SANS_SERIF);
	}

	private void updateNewPasswordVisibility(){
		if(!isNewPwdShown){
			new_password_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			Editable etable = new_password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isNewPwdShown = true;
		}else{
			new_password_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			Editable etable = new_password_edittext.getText();  
			Selection.setSelection(etable, etable.length());
			isNewPwdShown = false;
		}
	}

	private void setupNewHiddenClick(){
		new_hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updateNewPasswordVisibility();
			}
		});
	}
	
	

/******************************Data Flow Control*************************************************************/
	private void updatePassword(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_updating), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().update_password(old_password,new_password,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(SamchatUpdatePasswordActivity.this, R.string.samchat_reset_succeed, Toast.LENGTH_SHORT).show();
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatUpdatePasswordActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatUpdatePasswordActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isUpdating = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatUpdatePasswordActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatUpdatePasswordActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isUpdating = false;
						}
					}, 0);
				}

		});
	}

}



