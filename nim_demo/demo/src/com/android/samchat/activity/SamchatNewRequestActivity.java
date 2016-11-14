package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.adapter.SimpleListAdapter;
import com.android.samchat.common.SCell;
import com.android.samchat.factory.LocationFactory;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.info.QuestionInfo;
import com.android.samservice.info.PlacesInfo;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.R;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.Toast;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import com.android.samservice.HttpCommClient;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;

import java.util.ArrayList;
import java.util.List;

public class SamchatNewRequestActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatNewRequestActivity.class.getSimpleName();
	private final int BASIC_PERMISSION_REQUEST_CODE = 100;
	private final int CONFIRM_ID_LOCATION_FOUND=101;
	
	private FrameLayout back_arrow_layout;
	private TextView send_textview;
	private EditText question_edittext;
	private EditText location_edittext;
	private LinearLayout history_request_layout;
	private ListView request_listview;

	private List<String> history_request;
	private SimpleListAdapter requestAdapter;

	private String question = null;
	private String locationInput = null;
	private String pre_locationInput=null;

	private boolean ready_send = false;

	private boolean isSending = false;

	private PlacesInfo infoFound=null;
	
	public static void startActivityForResult(Activity activity, TFragment fragment, int requestCode) {
		Intent intent = new Intent(activity, SamchatNewRequestActivity.class);
        fragment.startActivityForResult(intent, requestCode);
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
		setContentView(R.layout.samchat_newrequest_activity);

		requestBasicPermission();

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();
		initHistoryRequestList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationFactory.getInstance().stopLocationMonitor();
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		send_textview = findView(R.id.send);
		question_edittext = findView(R.id.question);
		location_edittext = findView(R.id.location);
		history_request_layout = findView(R.id.history_request);
		request_listview = findView(R.id.request);
	
		setupBackArrowClick();
		setupSendClick();
		setupQuestionEditClick();
		setupLocationEditClick();
	}

	private void requestBasicPermission() {
        MPermission.with(SamchatNewRequestActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
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
		LocationFactory.getInstance().startLocationMonitor();
	}

	@OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_location), Toast.LENGTH_SHORT).show();
	}

	@OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionNeverAskAgainFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_location), Toast.LENGTH_SHORT).show();
	}
	

	private void initHistoryRequestList(){
		history_request= new ArrayList<>();

		requestAdapter = new SimpleListAdapter(SamchatNewRequestActivity.this,history_request);
		request_listview.setAdapter(requestAdapter);
		request_listview.setItemsCanFocus(true);
		request_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String request = (String)parent.getAdapter().getItem(position);
				question_edittext.setText(request);
			}
		});

		request_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});

		loadHistoryRequest(true);
		
	}

	private List<String> loadedHistoryRequest;
	private void loadHistoryRequest(final boolean delay){
		SamDBManager.getInstance().asyncQuerySendQuestion(20, new NIMCallback(){
			@Override
			public void onResult(final Object obj1, Object obj2, int code) {
				getHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						List<SendQuestion> sqs = (List<SendQuestion>)obj1;
						if(sqs != null && sqs.size()>0){
							loadedHistoryRequest = new ArrayList<String>();
							for(SendQuestion sq: sqs){
								loadedHistoryRequest.add(sq.getquestion());
							}
							onHistoryRequestLoaded();
						}
					}
				},delay?250:0);
			}
		});
	}
	
	private void onHistoryRequestLoaded() {
		history_request.clear();
		if (loadedHistoryRequest != null) {
			history_request.addAll(loadedHistoryRequest);
			loadedHistoryRequest = null;
		}
		refreshHistoryRequest();
	}
	
    private void refreshHistoryRequest() {
        notifyDataSetChangedHistoryRequest();
    }

	private void notifyDataSetChangedHistoryRequest() {
		requestAdapter.notifyDataSetChanged();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void updateSend(){
		if(ready_send){
			send_textview.setEnabled(true);
			send_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
		}else{
			send_textview.setEnabled(false);
			send_textview.setBackgroundResource(R.drawable.samchat_button_green_inactive);
		}
	}

	private void setupSendClick(){
		updateSend();
		send_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSending){
					return;
				}

				isSending = true;
				sendQuestion();
			}
		});
	}

	private TextWatcher question_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			Selection.setSelection(s, s.length());
			question = question_edittext.getText().toString().trim();
			if(question.length() > 0){
				ready_send = true;
			}else{
				ready_send = false;
			}
			updateSend();
		}
	};

	private void setupQuestionEditClick(){
		question_edittext.addTextChangedListener(question_textWatcher);
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

	private TextWatcher location_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			Selection.setSelection(s, s.length());
			locationInput = location_edittext.getText().toString().trim();
		}
	};

	private void setupLocationEditClick(){
		location_edittext.addTextChangedListener(location_textWatcher);
		location_edittext.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {  
			@Override  
			public void onFocusChange(View v, boolean hasFocus) {  
				if(hasFocus) {
					SamchatLocationSearchActivity.startActivityForResult(SamchatNewRequestActivity.this, CONFIRM_ID_LOCATION_FOUND);
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CONFIRM_ID_LOCATION_FOUND && resultCode == RESULT_OK){
			infoFound = (PlacesInfo) data.getSerializableExtra(SamchatLocationSearchActivity.PLACE_INFO);
			if(infoFound != null){
				location_edittext.setText(infoFound.getdescription());
			}
		}

		super.onActivityResult( requestCode,  resultCode,  data);
	}

/******************************************Data Flow Control***********************************************************/
	private void sendQuestion(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_sending), false, null).setCanceledOnTouchOutside(false);
		String placeId = null;
		String location = null;
		double latitude =  Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		double longitude = Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL;
		SCell cell = null;
		if(TextUtils.isEmpty(locationInput) || locationInput.equals(getString(R.string.samchat_current_location))){
			location = null;
			placeId = null;
			Location loc = LocationFactory.getInstance().getCurrentBestLocation();
			cell = LocationFactory.getInstance().getCurrentCellInfo();
			if(loc!=null){
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
			}
		}else{
			location = locationInput;
			if(infoFound != null){
				placeId = infoFound.place_id;
			}
		}
		LogUtil.i(TAG,"latitude:"+latitude+" longitude:"+longitude+" placeId:"+placeId +" cell:"+cell);
		if(cell != null){
			LogUtil.i(TAG,"mcc:"+cell.mcc+" mnc:"+cell.mnc+" lac:"+cell.lac +" cid:"+cell.cid);
		}
		SamService.getInstance().send_question(question,latitude,longitude,placeId,location,cell,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							QuestionInfo qinfo = ((HttpCommClient)obj).qinfo;
							SamchatRequestDetailsActivity.start(SamchatNewRequestActivity.this, qinfo);
							SendQuestion sq = new SendQuestion(qinfo.question_id,qinfo.question, qinfo.datetime,qinfo.address);
							Intent data = new Intent();
							Bundle bundle = new Bundle();
							bundle.putSerializable("send_question", sq);
							data.putExtras(bundle);
							SamchatNewRequestActivity.this.setResult(RESULT_OK, data);
							finish();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatNewRequestActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatNewRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending= false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatNewRequestActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatNewRequestActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		} );
	}
}



