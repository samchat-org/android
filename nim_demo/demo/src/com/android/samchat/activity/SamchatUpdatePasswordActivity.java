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
import android.widget.ImageView;
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

public class SamchatUpdatePasswordActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatUpdatePasswordActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	
	private RelativeLayout old_hidden_layout;
	private ImageView old_hidden_icon_iv;
	private EditText old_password_ev;
	private ImageView old_password_ok_iv;
	
	private RelativeLayout new_hidden_layout;
	private ImageView new_hidden_icon_iv;
	private EditText new_password_ev;
	private ImageView new_password_ok_iv;

	private TextView done_tv;

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
		old_hidden_layout = findView(R.id.old_hidden_layout);
		old_hidden_icon_iv = findView(R.id.old_hidden_icon);
		old_password_ev = findView(R.id.old_password);
		old_password_ok_iv = findView(R.id.old_password_ok);
	
		new_hidden_layout = findView(R.id.new_hidden_layout);
		new_hidden_icon_iv = findView(R.id.new_hidden_icon);
		new_password_ev = findView(R.id.new_password);
		new_password_ok_iv = findView(R.id.new_password_ok);

		done_tv = findView(R.id.done);

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
		done_tv.setOnClickListener(new OnClickListener() {
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
		if(ready_old_password & ready_new_password){
			done_tv.setEnabled(true);
			done_tv.setBackgroundResource(R.drawable.samchat_button_green_active);
		}else{
			done_tv.setEnabled(false);
			done_tv.setBackgroundResource(R.drawable.samchat_button_green_inactive);
		}
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
			ready_old_password = old_password_ev.getText().length()>=Constants.MIN_PASSWORD_LENGTH;
			updateOldPasswordOK();
			updateOldPasswordHiddenIcon();
			updateDoneButton();
			if(ready_old_password){
				old_password = old_password_ev.getText().toString();
			}
		}
	};

	private void updateOldPasswordHiddenIcon(){
		if(ready_old_password){
			if(isOldPwdShown){
				old_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_shown);
			}else{
				old_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_filled);
			}
		}else{
			old_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
		}
	}

	private void updateOldPasswordOK(){
		old_password_ok_iv.setVisibility(ready_old_password ? View.VISIBLE:View.GONE);
	}

	private void setupOldPasswordEditClick(){
		old_password_ev.addTextChangedListener(old_password_textWatcher);
		old_password_ev.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		old_password_ev.setTypeface(Typeface.SANS_SERIF);
	}

	private void updateOldPasswordVisibility(){
		if(!isOldPwdShown){
			old_password_ev.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			old_password_ev.setTypeface(Typeface.SANS_SERIF);
			Editable etable = old_password_ev.getText();  
			Selection.setSelection(etable, etable.length());
			isOldPwdShown = true;
			updateOldPasswordHiddenIcon();
		}else{
			old_password_ev.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); 
			old_password_ev.setTypeface(Typeface.SANS_SERIF);
			Editable etable = old_password_ev.getText();  
			Selection.setSelection(etable, etable.length());
			isOldPwdShown = false;
			updateOldPasswordHiddenIcon();
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
			ready_new_password = new_password_ev.getText().length()>=Constants.MIN_PASSWORD_LENGTH;
			updateNewPasswordOK();
			updateNewPasswordHiddenIcon();
			updateDoneButton();
			if(ready_new_password){
				new_password = new_password_ev.getText().toString();
			}
		}
	};

	private void setupNewPasswordEditClick(){
		new_password_ev.addTextChangedListener(new_password_textWatcher);
		new_password_ev.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		new_password_ev.setTypeface(Typeface.SANS_SERIF);
	}

	private void updateNewPasswordOK(){
		new_password_ok_iv.setVisibility(ready_new_password ? View.VISIBLE:View.GONE);
	}

	private void updateNewPasswordHiddenIcon(){
		if(ready_new_password){
			if(isNewPwdShown){
				new_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_shown);
			}else{
				new_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_filled);
			}
		}else{
			new_hidden_icon_iv.setImageResource(R.drawable.samchat_ic_showpw_hint);
		}
	}

	private void updateNewPasswordVisibility(){
		if(!isNewPwdShown){
			new_password_ev.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);  
			new_password_ev.setTypeface(Typeface.SANS_SERIF);
			Editable etable = new_password_ev.getText();
			Selection.setSelection(etable, etable.length());
			isNewPwdShown = true;
			updateNewPasswordHiddenIcon();
		}else{
			new_password_ev.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			new_password_ev.setTypeface(Typeface.SANS_SERIF);
			Editable etable = new_password_ev.getText();
			Selection.setSelection(etable, etable.length());
			isNewPwdShown = false;
			updateNewPasswordHiddenIcon();
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



