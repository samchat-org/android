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
import android.text.Selection;
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
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
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
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.info.ContactUser;

public class SamchatProfileEditActivity extends UI implements OnKeyListener{
	private static final String TAG = SamchatProfileCustomerActivity.class.getSimpleName();

    public static final int EDIT_PROFILE_TYPE_UNKNOW=0;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_EMAIL=1;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS=2;
	public static final int EDIT_PROFILE_TYPE_SP_EMAIL=3;
	public static final int EDIT_PROFILE_TYPE_SP_ADDRESS=4;

	public static final int EDIT_PROFILE_TYPE_SP_COMPANY_NAME=5;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY=6;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION=7;

	private FrameLayout back_arrow_layout;
	private TextView save_textview;
	private ClearableEditTextWithIcon edit_edittext;

	private String data;
	private String new_data;
	private int type;

	private boolean isSaving=false;

    public static void start(Context context, int type, String data) {
		Intent intent = new Intent(context, SamchatProfileEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
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
		setContentView(R.layout.samchat_profileedit_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		parseIntent();
		setupPanel();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void parseIntent() {
        type = getIntent().getIntExtra("type",EDIT_PROFILE_TYPE_UNKNOW);
        data = getIntent().getStringExtra("data");
        if(type == EDIT_PROFILE_TYPE_UNKNOW){
            finish();
        }
    }

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		save_textview= findView(R.id.save);
		edit_edittext= findView(R.id.edit);

		edit_edittext.setDeleteImage(R.drawable.nim_grey_delete_icon);
		edit_edittext.setText(data);
        Editable etext = edit_edittext.getText();
		Selection.setSelection(etext, etext.length());

		setupBackArrowClick();
		setupSaveClick();
		
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupSaveClick(){
		save_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSaving){
					return;
				}

				if(edit_edittext.getText().toString().trim().equals(data)){
					return;
				}

				isSaving = true;
				new_data = edit_edittext.getText().toString().trim();
				save();
			}
		});
	}

/*************************Data Flow Control***************************************************************/
	private void save(){
		ContactUser user = new ContactUser(SamService.getInstance().get_current_user());

		user.setcountrycode(null);
		user.setcellphone(null);
		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				user.setemail(new_data);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:
				user.setaddress(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_EMAIL:
				user.setemail_sp(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_ADDRESS:
				user.setaddress_sp(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:
				user.setcompany_name(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:
				user.setservice_category(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:
				user.setservice_description(new_data);
				break;
		}

		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().edit_profile(user,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					final ErrorString error = new ErrorString(SamchatProfileEditActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileEditActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSaving = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatProfileEditActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatProfileEditActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSaving = false;
						}
					}, 0);
				}

		} );
	}
}

