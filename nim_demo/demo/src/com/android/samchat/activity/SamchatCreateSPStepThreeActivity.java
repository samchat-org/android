package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import com.android.samchat.R;
import com.android.samchat.service.ErrorString;
import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.model.ToolBarOptions;

public class SamchatCreateSPStepThreeActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCreateSPStepThreeActivity.class.getSimpleName();

	private EditText service_description_ev;
	private TextView done_tv;

	private boolean ready_service_description=false;
	private String service_description=null;

	private ContactUser info;
	private boolean isCreating = false;
	
	public static void start(Context context,ContactUser sp) {
		Intent intent = new Intent(context, SamchatCreateSPStepThreeActivity.class);
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
		setContentView(R.layout.samchat_createspstepthree_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();
		setupPanel();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void onParseIntent() {
		info = (ContactUser)getIntent().getSerializableExtra("info");
	}

	private void setupPanel() {
		service_description_ev = findView(R.id.service_description);
		done_tv = findView(R.id.done);

		setupServiceDescriptionEditClick();
		setupDoneClick();
	}
	
	private void setupDoneClick(){
		done_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isCreating){
					return;
				}
				info.setservice_description(service_description);
				isCreating = true;
				createSPAccount();
			}
		});
		updateDone();
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
			service_description = service_description_ev.getText().toString().trim();
			if(service_description.length() > 0){
				ready_service_description= true;
			}else{
				ready_service_description = false;
			}
			updateDone();
		}
	};

	private void setupServiceDescriptionEditClick(){
		service_description_ev.addTextChangedListener(service_description_textWatcher);	
	}

	private void updateDone(){
		boolean enable = ready_service_description;
		done_tv.setEnabled(enable);
	}

/************************************date flow control************************************/
	private void sendbroadcast(Intent intent){
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	private void createSPAccount(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_create_sp), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().create_sam_pros(info,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
                            SamchatCreateSPStepFourActivity.start(SamchatCreateSPStepThreeActivity.this);
							Intent intent = new Intent();
                			intent.setAction(Constants.BROADCAST_CREATE_SP_SUCCESS);
                			sendbroadcast(intent);
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatCreateSPStepThreeActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatCreateSPStepThreeActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isCreating = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatCreateSPStepThreeActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatCreateSPStepThreeActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isCreating = false;
						}
					}, 0);
				}

		} );

	}


}



