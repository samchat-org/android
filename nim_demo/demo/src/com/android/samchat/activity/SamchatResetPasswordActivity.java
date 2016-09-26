package com.android.samchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.widget.RelativeLayout;
import android.text.InputType;
import android.text.Selection;
import android.graphics.Typeface;

public class SamchatResetPasswordActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatResetPasswordActivity.class.getSimpleName();
	private static final String COUNTRYCODE="countrycode";
	private static final String CELLPHONE="cellphone";
	private static final String VERIFYCODE="verifycode";
	private static final String DEVICEID="deviceid";

	private FrameLayout back_arrow_layout;
	private EditText password_edittext;
	private RelativeLayout hidden_layout;
	private TextView done_textview;

	private boolean ready_password=false;

	private boolean isPwdShown=false;

	private String password;
	private String countrycode;
	private String cellphone;
	private String verifycode;
	private String deviceid;

	private boolean isReseting = false;
	
	public static void start(Context context,String countrycode, String cellphone, String verifycode, String deviceid) {
		Intent intent = new Intent(context, SamchatResetPasswordActivity.class);
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
		setContentView(R.layout.samchat_resetpassword_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();
		setupPanel();
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
				if(isReseting){
					return;
				}
				
				if(!isPasswordValid()){
					Toast.makeText(SamchatResetPasswordActivity.this, R.string.samchat_password_warning, Toast.LENGTH_SHORT).show();
					return;
				}
				isReseting = true;
				resetpassword();
			}
		});

		updateDoneButton();
	}

	private void updateDoneButton(){
		done_textview.setEnabled( ready_password );
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
	}

	private void setupHiddenClick(){
		hidden_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updatePasswordVisibility();
			}
		});
	}
	
	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		password_edittext = findView(R.id.password);
		hidden_layout = findView(R.id.hidden_layout);
		done_textview = findView(R.id.done);

		setupBackArrowClick();
		setupPasswordEditClick();
		setupHiddenClick();
		setupDoneClick();

	}

/******************************Data Flow Control*************************************************************/

	private void resetpassword(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_reseting), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().findpwd_update( countrycode,  cellphone,  verifycode, password,  deviceid,
			new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(SamchatResetPasswordActivity.this, R.string.samchat_reset_succeed, Toast.LENGTH_SHORT).show();
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatResetPasswordActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatResetPasswordActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isReseting = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatResetPasswordActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatResetPasswordActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isReseting = false;
						}
					}, 0);
				}

		});
	}

}



