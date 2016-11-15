package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;

import com.netease.nim.demo.DemoCache;
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
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.android.samservice.info.ContactUser;
public class SamchatCreateSPStepTwoActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCreateSPStepTwoActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private EditText cellphone_edittext;
	private EditText email_edittext;
	private EditText address_edittext;
	private TextView skip_tv;
	private TextView next_tv;

	private ContactUser info;
	private String cellphone=null;
	private String email=null;
	private String address=null;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatCreateSPStepTwoActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_CREATE_SP_SUCCESS);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_CREATE_SP_SUCCESS)){
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

	public static void start(Context context,ContactUser sp) {
		Intent intent = new Intent(context, SamchatCreateSPStepTwoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("info", sp);
		intent.putExtras(bundle);
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
		setContentView(R.layout.samchat_createspsteptwo_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();

		setupPanel();
		registerBroadcastReceiver();

	}

	@Override
	protected void onDestroy() {
		unregisterBroadcastReceiver();
		super.onDestroy();
	}

	private void onParseIntent() {
		info = (ContactUser)getIntent().getSerializableExtra("info");
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		
		cellphone_edittext = findView(R.id.cellphone);
		email_edittext = findView(R.id.email);
		address_edittext = findView(R.id.address);

		skip_tv = findView(R.id.skip);
		next_tv = findView(R.id.next);

		setupBackArrowClick();

		setupCellphoneEditClick();
		setupEmailEditClick();
		setupAddressEditClick();

		setupNextClick();
       setupSkipClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupNextClick(){
		next_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				info.setphone_sp(cellphone);
				info.setemail_sp(email);
				info.setaddress_sp(address);
				SamchatCreateSPStepThreeActivity.start(SamchatCreateSPStepTwoActivity.this,info);
			}
		});
	}

	private void setupSkipClick(){
		skip_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cellphone = "";
				email = "";
				address = "";
				info.setphone_sp(cellphone);
				info.setemail_sp(email);
				info.setaddress_sp(address);
				SamchatCreateSPStepThreeActivity.start(SamchatCreateSPStepTwoActivity.this,info);

				cellphone_edittext.setText("");
				email_edittext.setText("");
				address_edittext.setText("");
			}
		});
	}

	private TextWatcher cellphone_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			cellphone = cellphone_edittext.getText().toString().trim();
		}
	};

	private void setupCellphoneEditClick(){
		cellphone_edittext.addTextChangedListener(cellphone_textWatcher);	
	}

	private TextWatcher email_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			email = email_edittext.getText().toString().trim();
		}
	};

	private void setupEmailEditClick(){
		email_edittext.addTextChangedListener(email_textWatcher);	
	}

	private TextWatcher address_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			address = address_edittext.getText().toString().trim();
		}
	};

	private void setupAddressEditClick(){
        address_edittext.addTextChangedListener(address_textWatcher);
	}

}



