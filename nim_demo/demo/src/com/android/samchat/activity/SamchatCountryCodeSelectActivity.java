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
import android.widget.RelativeLayout;
import com.android.samservice.Constants;
import android.app.Activity;
import android.widget.ListView;
import com.android.samchat.common.CountryInfo;
import java.util.List;
import com.android.samchat.adapter.CountryCodeAdapter;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

public class SamchatCountryCodeSelectActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCountryCodeSelectActivity.class.getSimpleName();
	private static final String [][] countryInfoarray={
		{"Afghanistan","93"},
		{"Albania","355"},
		{"Algeria","213"},
		{"Andorra","376"},
		{"Bahamas","193"},
		{"Bahrian","293"},
		{"China","86"},
		{"USA","1"}
	};
	

	private FrameLayout back_arrow_layout;
	private TextView search_textview;
	private ListView countrylist;

	private List<CountryInfo> items;
	private CountryCodeAdapter adapter;

	private void createCountryInfoList(){
		for(int i=0; i<countryInfoarray.length;i++){
			items.add(new CountryInfo(countryInfoarray[i][0],countryInfoarray[i][1]));
		}
	}

	private void sortCountry(List<CountryInfo> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, comp);
	}

	private static Comparator<CountryInfo> comp = new Comparator<CountryInfo>() {
		@Override
		public int compare(CountryInfo lhs, CountryInfo rhs) {
			int lhs_ascii = lhs.getFPinYin().toUpperCase().charAt(0);
			int rhs_ascii = rhs.getFPinYin().toUpperCase().charAt(0);

			if (lhs_ascii < 65 || lhs_ascii > 90)
				return 1;
			else if (rhs_ascii < 65 || rhs_ascii > 90)
				return -1;
			else
				return lhs.getPinYin().compareTo(rhs.getPinYin());
		}
	};

	public static void startActivityForResult(Activity activity, int requestCode) {
		Intent intent = new Intent(activity, SamchatCountryCodeSelectActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}

	private void initCountryList(){
		items = new ArrayList<CountryInfo>();
		createCountryInfoList();
		sortCountry(items);
		adapter = new CountryCodeAdapter(SamchatCountryCodeSelectActivity.this,items);
		countrylist.setAdapter(adapter);
		countrylist.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position == 0){
					return;
				}
				
				Intent data = new Intent();
				if(position == 1){
					data.putExtra(Constants.CONFIRM_COUNTRYCODE,"86");
				}else if(position == 2){
					data.putExtra(Constants.CONFIRM_COUNTRYCODE, "1");
				}else{
					data.putExtra(Constants.CONFIRM_COUNTRYCODE, adapter.getItem(position).code);
				}

				setResult(RESULT_OK, data);
				finish();
			}
		});
		
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
		setContentView(R.layout.samchat_countrycodeselect_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupCountryCodeSelectPanel();
	}

	@Override
	public void onBackPressed(){
		setResult(RESULT_CANCELED);
		finish();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	private void setupSearchClick(){
		search_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}
	
	private void setupCountryCodeSelectPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		search_textview = findView(R.id.search);
		countrylist = findView(R.id.list);

		setupBackArrowClick();
		setupSearchClick();
		initCountryList();

	}

}


