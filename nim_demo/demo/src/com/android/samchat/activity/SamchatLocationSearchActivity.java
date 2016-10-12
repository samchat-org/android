package com.android.samchat.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.android.samchat.adapter.PlaceInfoAdapter;
import com.android.samchat.adapter.SimpleListAdapter;
import com.android.samchat.common.SCell;
import com.android.samchat.factory.LocationFactory;
import com.android.samservice.info.QuestionInfo;
import com.android.samservice.info.PlacesInfo;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import com.android.samservice.HttpCommClient;

import java.util.ArrayList;
import java.util.List;

public class SamchatLocationSearchActivity extends Activity {
	private static final String TAG = SamchatLocationSearchActivity.class.getSimpleName();
	private static final int MSG_COUNT_DOWN = 100;
	private static final int TIME_COUNT_DOWN = 1000;

	public static final String PLACE_INFO = "PLACE_INFO";
	
	private FrameLayout back_arrow_layout;
	private EditText location_edittext;
	private TextView send_textview;
	private ListView address_listview;

	private List<PlacesInfo> addresses;
	private PlaceInfoAdapter addressAdapter;
	
	private String locationInput = null;
	private String pre_locationInput=null;

	private boolean ready_send = false;
	
	public static void startActivityForResult(Activity activity,int requestCode) {
		Intent intent = new Intent(activity, SamchatLocationSearchActivity.class);
        activity.startActivityForResult(intent, requestCode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_location_search_activity);
		setupPanel();
		initAddressList();
		handler = new LocationHandler();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
	}

	private void setupPanel() {
		back_arrow_layout = (FrameLayout)findViewById(R.id.back_arrow_layout);
		send_textview = (TextView) findViewById(R.id.send);
		location_edittext = (EditText) findViewById(R.id.location);
		address_listview = (ListView) findViewById(R.id.address);

		setupBackArrowClick();
		setupSendClick();
		setupLocationEditClick();
	}

	private void initAddressList(){
		addresses = new ArrayList<>();
		addresses.add(new PlacesInfo(getString(R.string.samchat_current_location),null));
		addressAdapter = new PlaceInfoAdapter(SamchatLocationSearchActivity.this,addresses);
		address_listview.setAdapter(addressAdapter);
		address_listview.setItemsCanFocus(true);
		address_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PlacesInfo info = (PlacesInfo)parent.getAdapter().getItem(position);
				location_edittext.setText(info.description);
			}
		});

		address_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
	}

	private List<PlacesInfo> searchedAddress;
	private void onAddressLoaded(){
		addresses.clear();
		addresses.add(new PlacesInfo(getString(R.string.samchat_current_location),null));
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
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED, null);
				finish();
			}
		});
	}

	private void updateSend(){
		if(ready_send){
			send_textview.setEnabled(true);
			send_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green);
		}else{
			send_textview.setEnabled(false);
			send_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green_disable);
		}
	}

	private void setupSendClick(){
		updateSend();
		send_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				PlacesInfo info = findAddress();
				if(info == null){
					info = new PlacesInfo(locationInput,null);
				}
				Intent data = new Intent();
				data.putExtra(PLACE_INFO,info);
				setResult(RESULT_OK, data);
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
			pre_locationInput = locationInput;
			locationInput = location_edittext.getText().toString().trim();
			if(!TextUtils.isEmpty(locationInput)){
				ready_send = true;
				updateSend();
			}else{
				ready_send = false;
				updateSend();
			}
			
			LogUtil.i(TAG,"pre_locationInput:"+pre_locationInput+" locationInput:"+locationInput);
			if(!stringEquals(pre_locationInput,locationInput) 
				&& !locationInput.equals(getString(R.string.samchat_current_location))
				&& findAddress() == null){
				cancelQueryCountDown();
				startQueryCountDown();
			}else if(locationInput.equals(getString(R.string.samchat_current_location))){
				cancelQueryCountDown();
			}else if(findAddress() != null){
				cancelQueryCountDown();
			}
		}
	};

	private void setupLocationEditClick(){
		location_edittext.addTextChangedListener(location_textWatcher);
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

/******************************************Data Flow Control***********************************************************/
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
}




