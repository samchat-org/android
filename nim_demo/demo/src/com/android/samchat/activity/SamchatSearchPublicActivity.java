package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.adapter.ContactAdapter;
import com.android.samchat.adapter.ContactUserAdapter;
import com.android.samservice.QuestionInfo;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.SendQuestion;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.HttpCommClient;
public class SamchatSearchPublicActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatSearchPublicActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView search_textview;
	private EditText key_edittext;
	private EditText location_edittext;
	private ListView searchResultList_listview;

	private String key = null;
	private String location = null;

	private boolean ready_search = false;

	private boolean isSearching = false;

	private List<ContactUser> items_search_result;
	private ContactUserAdapter adapter;
	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatSearchPublicActivity.class);
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
		setContentView(R.layout.samchat_searchpublic_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		search_textview = findView(R.id.search);
		key_edittext = findView(R.id.key);
		location_edittext = findView(R.id.location);
		searchResultList_listview = findView(R.id.searchResultList);

		setupBackArrowClick();
		setupSearchClick();
		setupKeyEditClick();
		setupLocationEditClick();
		setupSearchResultListView();
		
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void updateSearch(){
		boolean enable = ready_search;
		search_textview.setEnabled(enable);
	}

	private void setupSearchClick(){
		updateSearch();
		search_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSearching){
					return;
				}

				isSearching = true;
				searchPublic();
			}
		});
	}

	private TextWatcher key_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			key = key_edittext.getText().toString().trim();
			if(key.length() > 0){
				ready_search= true;
			}else{
				ready_search = false;
			}
			updateSearch();
		}
	};

	private void setupKeyEditClick(){
		key_edittext.addTextChangedListener(key_textWatcher);	
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
			location = location_edittext.getText().toString().trim();
		}
	};

	private void setupLocationEditClick(){
		location_edittext.addTextChangedListener(location_textWatcher);	
	}

	private void setupSearchResultListView(){
		items_search_result = new ArrayList<>();
		adapter = new ContactUserAdapter(SamchatSearchPublicActivity.this, items_search_result);
		searchResultList_listview.setAdapter(adapter);
        searchResultList_listview.setItemsCanFocus(true);
        searchResultList_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactUser user = (ContactUser) parent.getAdapter().getItem(position);
				SamchatContactUserSPNameCardActivity.start(SamchatSearchPublicActivity.this, user);
			}
		});
	}

	private void notifyDataContactUser() {
		adapter.notifyDataSetChanged();
	}

	private void refreshContactUserList(){
		sortContactUser(items_search_result);
		notifyDataContactUser();
	}

	private void sortContactUser(List<ContactUser> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, usercomp);
	}

	private static Comparator<ContactUser> usercomp = new Comparator<ContactUser>() {
		@Override
		public int compare(ContactUser o1, ContactUser o2) {
			return TextComparator.compare(o1.getusername(),o2.getusername());
		}
	};

/******************************************Data Flow Control***********************************************************/
	private List<ContactUser> loadedContactUser;
	private void onContactUserSearched() {	
		items_search_result.clear();
		if (loadedContactUser != null) {
			items_search_result.addAll(loadedContactUser);
			loadedContactUser = null;
		}
		refreshContactUserList();
	}
	
	private void searchPublic(){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_searching), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().query_public(key,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,
				null,location,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSearching = false;
							HttpCommClient hcc = (HttpCommClient)obj;
							loadedContactUser = hcc.users.getusers();
							onContactUserSearched();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					}, 0);
				}

		} );

	}

}




