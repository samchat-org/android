package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.R;
import com.android.samchat.adapter.PlaceInfoAdapter;
import com.android.samservice.HttpCommClient;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PlacesInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;

import java.util.ArrayList;
import java.util.List;

public class SamchatProfileEditActivity extends UI implements OnKeyListener{
	private static final String TAG = SamchatProfileEditActivity.class.getSimpleName();
	private static final int MSG_COUNT_DOWN = 100;
	private static final int TIME_COUNT_DOWN = 1000;

	public static final int EDIT_PROFILE_TYPE_UNKNOW=0;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_EMAIL=1;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS=2;
	public static final int EDIT_PROFILE_TYPE_SP_EMAIL=3;
	public static final int EDIT_PROFILE_TYPE_SP_ADDRESS=4;

	public static final int EDIT_PROFILE_TYPE_SP_COMPANY_NAME=5;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY=6;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION=7;
	public static final int EDIT_PROFILE_TYPE_SP_PHONE=8;

	public static final int EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID=9;

	private static final int CONFIRM_ID_SELECT_COUNTRY_CODE=202;

	private RelativeLayout titlebar_layout;
	private FrameLayout back_arrow_layout;
	private ImageView back_icon;
	private TextView titlebar_name_tv;
	private TextView sp_save_tv;
	private FrameLayout sp_save_layout;
	private TextView customer_save_tv;
	
	private ClearableEditTextWithIcon edit_edittext;
	private TextView countrycode_textview;
	private EditText multi_edit_ev;
	private RelativeLayout edit_layout_single;
	private RelativeLayout edit_layout_multiple;
	private ListView address_lv;

	private TextView illustration_tv;

	private String data;
	private String new_data;
	private int type;

	private PlacesInfo place_info=null;

	private String countrycode;
	private String new_countrycode;

	private boolean isSaving=false;

	private List<PlacesInfo> addresses;
	private PlaceInfoAdapter addressAdapter;
	
	private String locationInput = null;
	private String pre_locationInput=null;

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
		if(handler != null)
			handler.removeCallbacksAndMessages(null);
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
		}else{
			data = getIntent().getStringExtra("data");
		}
    }

	private boolean isCustomerMode(){
		if(type == EDIT_PROFILE_TYPE_CUSTOMER_EMAIL
			|| type == EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS
			|| type == EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID){
			return true;
		}else{
			return false;
		}
	}

	private boolean isAddressSelect(){
		if(type == EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS
			|| type == EDIT_PROFILE_TYPE_SP_ADDRESS){
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
			case EDIT_PROFILE_TYPE_SP_PHONE:
				titlebar_name_tv.setText(R.string.samchat_edit_phone_number);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID:
				titlebar_name_tv.setText(R.string.samchat_edit_samchat_id);
				break;
		}
	}

	private void setInputType(){
		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				edit_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:

				break;
			case EDIT_PROFILE_TYPE_SP_EMAIL:
				edit_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				break;
			case EDIT_PROFILE_TYPE_SP_ADDRESS:

				break;
			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:

				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:

				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:

				break;
			case EDIT_PROFILE_TYPE_SP_PHONE:
				edit_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID:
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

	private void setIllustrationText(){
		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				illustration_tv.setText(getString(R.string.samchat_illustration_email));
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:
				illustration_tv.setText(getString(R.string.samchat_illustration_address));
				break;
			case EDIT_PROFILE_TYPE_SP_EMAIL:
				edit_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				illustration_tv.setText(getString(R.string.samchat_illustration_email));
				break;
			case EDIT_PROFILE_TYPE_SP_ADDRESS:
				illustration_tv.setText(getString(R.string.samchat_illustration_address));
				break;
			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:
				illustration_tv.setText(getString(R.string.samchat_illustration_company));
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:
				illustration_tv.setText(getString(R.string.samchat_illustration_service_category));
				break;
			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:
				illustration_tv.setText(getString(R.string.samchat_illustration_service_description));
				break;
			case EDIT_PROFILE_TYPE_SP_PHONE:
				edit_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
				illustration_tv.setText(getString(R.string.samchat_illustration_phone));
				break;
			case EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID:
				illustration_tv.setText(getString(R.string.samchat_illustration_samchat_id));
				break;
		}
	}

	private void setupPanel() {
		titlebar_layout = findView(R.id.titlebar_layout);
		back_icon = findView(R.id.back_icon);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		edit_edittext= findView(R.id.edit);
		countrycode_textview = findView(R.id.countrycode);
		titlebar_name_tv = findView(R.id.titlebar_name);
		edit_layout_single = findView(R.id.edit_layout_single);
		edit_layout_multiple = findView(R.id.edit_layout_multiple);
		multi_edit_ev = findView(R.id.multi_edit);
		address_lv = findView(R.id.address);
		sp_save_tv= findView(R.id.sp_save);
		sp_save_layout = findView(R.id.sp_save_layout);
		customer_save_tv = findView(R.id.customer_save);
		illustration_tv = findView(R.id.illustration);

		if(type == EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION){
			countrycode_textview.setVisibility(View.GONE);
			edit_layout_single.setVisibility(View.GONE);
			edit_layout_multiple.setVisibility(View.VISIBLE);
		}else if(type == EDIT_PROFILE_TYPE_SP_PHONE){
			countrycode_textview.setVisibility(View.GONE);
			edit_layout_single.setVisibility(View.VISIBLE);
			edit_layout_multiple.setVisibility(View.GONE);
			edit_edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_MPHONE_NUMBER_LENGTH+4)});
		}else{
			countrycode_textview.setVisibility(View.GONE);
			edit_layout_single.setVisibility(View.VISIBLE);
			edit_layout_multiple.setVisibility(View.GONE);
		}

		if(isAddressSelect()){
			initAddressList();
			handler = new LocationHandler();
			address_lv.setVisibility(View.VISIBLE);
		}else{
			address_lv.setVisibility(View.GONE);
		}

		setIllustrationText();

		setupBackArrowClick();
		setupSpSaveClick();
		setupCustomerSaveClick();
		setupCountryCodeClick();
		
		edit_edittext.setDeleteImage(R.drawable.nim_grey_delete_icon);
		edit_edittext.setText(data);
       Editable etext = edit_edittext.getText();
		Selection.setSelection(etext, etext.length());
		edit_edittext.setAfterTextChangedListener(new ClearableEditTextWithIcon.afterTextChangedListener(){
			@Override
			public void afterTextChangedCallback(Editable s){
				if(isAddressSelect()){
					place_info = null;
					Selection.setSelection(s, s.length());
					pre_locationInput = locationInput;
					locationInput = edit_edittext.getText().toString().trim();
					if(!stringEquals(pre_locationInput,locationInput) 
						&& !locationInput.equals(getString(R.string.samchat_current_location))
						&& findAddress()==null
						&& !TextUtils.isEmpty(locationInput)){
						cancelQueryCountDown();
						startQueryCountDown();
					}else{
						cancelQueryCountDown();
					}
					new_data = locationInput;
					updateSaveButton(s);
				}else{
					new_data = edit_edittext.getText().toString().trim();
					updateSaveButton(s);
				}
			}
		});

		multi_edit_ev.setText(data);
       Editable etext2 = multi_edit_ev.getText();
		Selection.setSelection(etext2, etext2.length());
		multi_edit_ev.addTextChangedListener(service_description_textWatcher);
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
			new_data = multi_edit_ev.getText().toString().trim();
			updateSaveButton(s);
		}
	};

	private void updateSaveButtonBackground(boolean clickable){
		if(isCustomerMode()){
			customer_save_tv.setVisibility(View.VISIBLE);
			sp_save_layout.setVisibility(View.GONE);
			customer_save_tv.setBackgroundResource(clickable?R.drawable.samchat_button_green_active:R.drawable.samchat_button_green_inactive);
		}else{
			customer_save_tv.setVisibility(View.GONE);
			sp_save_layout.setVisibility(View.VISIBLE);
			if(clickable){
				sp_save_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
				sp_save_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
			}else{
				sp_save_tv.setTextColor(getResources().getColor(R.color.samchat_color_grey));
				sp_save_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp_inactive);
			}
		}	
	}

	private void updateSaveButton(Editable s){
		if(type == EDIT_PROFILE_TYPE_SP_COMPANY_NAME ||
			type == EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY||
			type == EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION){
			if(s.length()>0){
				sp_save_layout.setEnabled(true);
				updateSaveButtonBackground(true);
			}else{
				sp_save_layout.setEnabled(false);
				updateSaveButtonBackground(false);
			}
		}else if(type == EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID){
			if(s.length()>0){
				customer_save_tv.setEnabled(true);
				updateSaveButtonBackground(true);
			}else{
				customer_save_tv.setEnabled(false);
				updateSaveButtonBackground(false);
			}
		}else{
			if(isCustomerMode()){
				customer_save_tv.setEnabled(true);
			}else{
				sp_save_layout.setEnabled(true);
			}
			updateSaveButtonBackground(true);
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

	private void setupSpSaveClick(){
		sp_save_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSaving){
					return;
				}

				isSaving = true;
				save();
			}
		});
		sp_save_layout.setEnabled(false);
		updateSaveButtonBackground(false);
	}

	private void setupCustomerSaveClick(){
		customer_save_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSaving){
					return;
				}

				if(isAddressSelect()){
					place_info = findAddress();
					if(place_info != null){
						new_data = place_info.description;
					}
				}
				
				isSaving = true;
				save();
			}
		});
		customer_save_tv.setEnabled(false);
		updateSaveButtonBackground(false);
	}

	private void setupCountryCodeClick(){
		countrycode_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCountryCodeSelectActivity.startActivityForResult(SamchatProfileEditActivity.this, CONFIRM_ID_SELECT_COUNTRY_CODE);
			}
		});
	}

	private void initAddressList(){
		addresses = new ArrayList<>();
		addressAdapter = new PlaceInfoAdapter(SamchatProfileEditActivity.this,addresses,false);
		address_lv.setAdapter(addressAdapter);
		address_lv.setItemsCanFocus(true);
		address_lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				place_info = (PlacesInfo)parent.getAdapter().getItem(position);
				edit_edittext.setText(place_info.description);
			}
		});

		address_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
	}

	private List<PlacesInfo> searchedAddress;
	private void onAddressLoaded(){
		addresses.clear();
		if(searchedAddress!=null){
			addresses.addAll(searchedAddress);
			searchedAddress = null;
		}

		refreshAddressList();
	}

	private void notifyDataSetChangedAddress() {
		addressAdapter.notifyDataSetChanged();
	}

	private void refreshAddressList(){
		notifyDataSetChangedAddress();
	}

	private PlacesInfo findAddress(){
		for(PlacesInfo info : addresses){
			if(!TextUtils.isEmpty(locationInput) && locationInput.equals(info.description)){
				LogUtil.i(TAG,"find Adress :" + info);
				return info;
			}
		}
		LogUtil.i(TAG,"find Adress :" + null);
		return null;
	}

	private LocationHandler handler;

	private class LocationHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_COUNT_DOWN:
					getPlacesInfo(locationInput);	
					break;
				}
		}
	}

	private void cancelQueryCountDown() {
		handler.removeMessages(MSG_COUNT_DOWN);
	}

	private void startQueryCountDown() {
		Message msg = handler.obtainMessage(MSG_COUNT_DOWN);
		handler.sendMessageDelayed(msg, TIME_COUNT_DOWN);
	}

/*************************Data Flow Control***************************************************************/
	private void getPlacesInfo(String key){
		if(key == null){
			return;
		}

		SamService.getInstance().get_places_info(key, new SMCallBack(){
			@Override
			public void onSuccess(final Object obj, final int WarningCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    		if(isDestroyedCompatible()){
								return;
							}

                        HttpCommClient hcc = (HttpCommClient) obj;
                        if (hcc.placesinfos.getkey().equals(locationInput)) {
                            searchedAddress = hcc.placesinfos.getinfo();
                            onAddressLoaded();
                        }
                    }
                });
			}
			@Override
			public void onFailed(int code) {}
			@Override
			public void onError(int code) {}
		 });
	}

	private void save(){
		if(type == EDIT_PROFILE_TYPE_CUSTOMER_SAMCHAT_ID){
			create_samchat_id();
		}else{
			edit_profile();
		}
	}


	private void edit_profile(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		ContactUser user = new ContactUser(SamService.getInstance().get_current_user());
		SamService.getInstance().edit_profile( type, user, new_data, place_info, new SMCallBack(){
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

	private void create_samchat_id(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().create_samchat_id( new_data , new SMCallBack(){
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

