package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
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

	public static final int EDIT_PROFILE_TYPE_CUSTOMER_PHONE=8;
	public static final int EDIT_PROFILE_TYPE_SP_PHONE=9;

	private static final int CONFIRM_ID_SELECT_COUNTRY_CODE=202;

	private RelativeLayout titlebar_layout;
	private FrameLayout back_arrow_layout;
	private ImageView back_icon;
	private TextView titlebar_name_tv;
	private TextView save_textview;
	
	private ClearableEditTextWithIcon edit_edittext;
	private TextView countrycode_textview;
	private EditText multi_edit_ev;
	private RelativeLayout edit_layout_single;
	private RelativeLayout edit_layout_multiple;
	private ImageView multi_delete_img;

	private String data;
	private String new_data;
	private int type;

	private String countrycode;
	private String new_countrycode;

	private boolean isSaving=false;

	public static void start(Context context, int type, String data) {
		Intent intent = new Intent(context, SamchatProfileEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("type", type);
		intent.putExtra("data", data);
		context.startActivity(intent);
	}

	public static void start(Context context, int type, String countrycode, String data) {
		Intent intent = new Intent(context, SamchatProfileEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("type", type);
		intent.putExtra("countrycode",countrycode);
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
		updateTitle();
		//setInputType();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_SELECT_COUNTRY_CODE && resultCode == RESULT_OK){
			new_countrycode = data.getStringExtra(Constants.CONFIRM_COUNTRYCODE);
			updateCountryCode(new_countrycode);
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}

	private void parseIntent() {
		type = getIntent().getIntExtra("type",EDIT_PROFILE_TYPE_UNKNOW);
		if(type == EDIT_PROFILE_TYPE_UNKNOW){
			finish();
		}else if(type == EDIT_PROFILE_TYPE_CUSTOMER_PHONE || type == EDIT_PROFILE_TYPE_SP_PHONE){
			countrycode = getIntent().getStringExtra("countrycode");
			data = getIntent().getStringExtra("data");
		}else{
			data = getIntent().getStringExtra("data");
		}
    }

	private boolean isCustomerMode(){
		if(type == EDIT_PROFILE_TYPE_CUSTOMER_EMAIL
			|| type == EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS
			|| type == EDIT_PROFILE_TYPE_CUSTOMER_PHONE){
			return true;
		}else{
			return false;
		}
	}

	private void setTitleCustomerMode(){
		titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
		back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
		back_icon.setImageResource(R.drawable.samchat_arrow_left);
		titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_dark_blue));
	}

	private void setTitleSPMode(){
		titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
		back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
		back_icon.setImageResource(R.drawable.samchat_arrow_left_sp);
		titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
	}
	
	private void updateTitle(){
		if(isCustomerMode()){
			setTitleCustomerMode();
		}else{
			setTitleSPMode();
		}
		
		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				titlebar_name_tv.setText(R.string.samchat_edit_email);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:
				titlebar_name_tv.setText(R.string.samchat_edit_address);
				break;
			case EDIT_PROFILE_TYPE_SP_EMAIL:
				titlebar_name_tv.setText(R.string.samchat_edit_email);
				break;
			case EDIT_PROFILE_TYPE_SP_ADDRESS:
				titlebar_name_tv.setText(R.string.samchat_edit_address);
				break;
			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:
				titlebar_name_tv.setText(R.string.samchat_edit_company);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:
				titlebar_name_tv.setText(R.string.samchat_edit_category);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:
				titlebar_name_tv.setText(R.string.samchat_edit_description);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_PHONE:
				titlebar_name_tv.setText(R.string.samchat_edit_phone_number);
				break;
			case EDIT_PROFILE_TYPE_SP_PHONE:
				titlebar_name_tv.setText(R.string.samchat_edit_phone_number);
				break;
		}
	}

	private void setInputType(){
		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				edit_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:
				//titlebar_name_tv.setText(R.string.samchat_edit_address);
				break;
			case EDIT_PROFILE_TYPE_SP_EMAIL:
				edit_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				break;
			case EDIT_PROFILE_TYPE_SP_ADDRESS:
				//titlebar_name_tv.setText(R.string.samchat_edit_address);
				break;
			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:
				//titlebar_name_tv.setText(R.string.samchat_edit_company);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:
				//titlebar_name_tv.setText(R.string.samchat_edit_category);
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:
				//titlebar_name_tv.setText(R.string.samchat_edit_description);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_PHONE:
				edit_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
				break;
			case EDIT_PROFILE_TYPE_SP_PHONE:
				edit_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
				break;
		}
	}

	private void updateCountryCode(String c){
		if(TextUtils.isEmpty(c)){
			countrycode_textview.setText("+");
		}else{
			countrycode_textview.setText("+"+c);
		}
	}

	private void setupPanel() {
		titlebar_layout = findView(R.id.titlebar_layout);
		back_icon = findView(R.id.back_icon);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		save_textview= findView(R.id.save);
		edit_edittext= findView(R.id.edit);
		countrycode_textview = findView(R.id.countrycode);
		titlebar_name_tv = findView(R.id.titlebar_name);
		edit_layout_single = findView(R.id.edit_layout_single);
		edit_layout_multiple = findView(R.id.edit_layout_multiple);
		multi_edit_ev = findView(R.id.multi_edit);
		multi_delete_img = findView(R.id.multi_delete_img);

		if(type == EDIT_PROFILE_TYPE_CUSTOMER_PHONE || type == EDIT_PROFILE_TYPE_SP_PHONE){
			countrycode_textview.setVisibility(View.VISIBLE);
			updateCountryCode(countrycode);
			edit_layout_single.setVisibility(View.VISIBLE);
			edit_layout_multiple.setVisibility(View.GONE);
		}else if(type == EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION){
			countrycode_textview.setVisibility(View.GONE);
			edit_layout_single.setVisibility(View.GONE);
			edit_layout_multiple.setVisibility(View.VISIBLE);
		}else{
			countrycode_textview.setVisibility(View.GONE);
			edit_layout_single.setVisibility(View.VISIBLE);
			edit_layout_multiple.setVisibility(View.GONE);
		}

		setupBackArrowClick();
		setupSaveClick();
		setupCountryCodeClick();
		edit_edittext.setDeleteImage(R.drawable.nim_grey_delete_icon);
		edit_edittext.setText(data);
       Editable etext = edit_edittext.getText();
		Selection.setSelection(etext, etext.length());
		edit_edittext.setAfterTextChangedListener(new ClearableEditTextWithIcon.afterTextChangedListener(){
			@Override
			public void afterTextChangedCallback(Editable s){
				updateSaveButton(s);
			}
		});

		multi_edit_ev.setText(data);
       Editable etext2 = multi_edit_ev.getText();
		Selection.setSelection(etext2, etext2.length());
		multi_edit_ev.addTextChangedListener(service_description_textWatcher);

		multi_delete_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				multi_edit_ev.setText("");
			}
		});
	}

	private TextWatcher service_description_textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			updateSaveButton(s);
		}
	};

	private void updateSaveButton(Editable s){
		if(type == EDIT_PROFILE_TYPE_CUSTOMER_PHONE || type == EDIT_PROFILE_TYPE_SP_PHONE){
			if( s.length()>=Constants.MIN_MPHONE_NUMBER_LENGTH
				&& countrycode_textview.getText().toString().trim().length()>=2){
				save_textview.setEnabled(true);
				save_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
			}else{
				save_textview.setEnabled(false);
				save_textview.setBackgroundResource(R.drawable.samchat_button_green_inactive);
			}
		}else{
			if(s.length()>0){
				save_textview.setEnabled(true);
				save_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
			}else{
				save_textview.setEnabled(false);
				save_textview.setBackgroundResource(R.drawable.samchat_button_green_inactive);
			}
		}
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(back_arrow_layout.getWindowToken(), 0);
				finish();
			}
		});
	}

	private boolean stringEquals(String s1, String s2){
		if(s1 == null && s2 == null){
			return true;
		}else if(s1 == null && s2 != null){
			return false;
		}else if(s1 != null && s2 == null){
			return false;
		}else{
			return s1.equals(s2);
		}
	}

	private void setupSaveClick(){
		save_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSaving){
					return;
				}

				if(type == EDIT_PROFILE_TYPE_SP_PHONE || type == EDIT_PROFILE_TYPE_CUSTOMER_PHONE){
					if(edit_edittext.getText().toString().trim().equals(data) && stringEquals(countrycode,new_countrycode)){
						return;
					}

					if(edit_edittext.getText().toString().trim().length() < Constants.MIN_MPHONE_NUMBER_LENGTH
						|| edit_edittext.getText().toString().trim().length() > Constants.MAX_MPHONE_NUMBER_LENGTH){
						Toast.makeText(SamchatProfileEditActivity.this, getString(R.string.samchat_phone_illeage), Toast.LENGTH_SHORT).show();
						return;
					}
				}else if(type == EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION){
					if(multi_edit_ev.getText().toString().trim().equals(data)){
						return;
					}
				}else{
					if(edit_edittext.getText().toString().trim().equals(data)){
						return;
					}
				}
				
				isSaving = true;
				save();
			}
		});
	}

	private void setupCountryCodeClick(){
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatProfileEditActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
	}

/*************************Data Flow Control***************************************************************/
	private void save(){
		if(type == EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION){
			new_data = multi_edit_ev.getText().toString().trim();
		}else{
			new_data = edit_edittext.getText().toString().trim();
		}
		
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
			case EDIT_PROFILE_TYPE_CUSTOMER_PHONE:
				user.setcountrycode(new_countrycode == null ? countrycode:new_countrycode);
				user.setcellphone(new_data);
				break;
			case EDIT_PROFILE_TYPE_SP_PHONE:
				user.setcountrycode_sp(new_countrycode == null ? countrycode:new_countrycode);
				user.setphone_sp(new_data);
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

