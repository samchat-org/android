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

import com.android.samchat.SamchatGlobal;
import com.android.samchat.adapter.SelectMemberAdapter;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.type.ModeEnum;
import com.android.samservice.info.Contact;
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
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.ImageView;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.android.samservice.info.ContactUser;
public class SamchatMemberSelectActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatMemberSelectActivity.class.getSimpleName();
	public static final String RESULT_DATA="RESULT_DATA";

	private FrameLayout back_arrow_layout;
	private FrameLayout right_button_layout;
	private ListView member_list_listview;

	private SamchatMemberSelectActivity.Option option;
	private List<String> selectMembers = null;
	private List<Contact> members = null;
	private boolean memberListLoaded = false;

	private SelectMemberAdapter adapter;

	public static void startActivityForResult(Context context,SamchatMemberSelectActivity.Option option, int requestCode) {
		Intent intent = new Intent(context, SamchatMemberSelectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("option", option);
		intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, requestCode);
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
		setContentView(R.layout.samchat_memberselect_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();

		findView();
		setupPanel();

		initMemberList();
		LoadMembers(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void onParseIntent() {
		option = (SamchatMemberSelectActivity.Option)getIntent().getSerializableExtra("option");
	}

	private void findView(){	
		back_arrow_layout = findView(R.id.back_arrow_layout);
		right_button_layout = findView(R.id.right_button_layout);
		member_list_listview = findView(R.id.member_list);
	}

	private void setupPanel() {
		setupBackArrowClick();
		setupOKClick();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupOKClick(){
		right_button_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(selectMembers.size() > option.getalreadySelectedAccounts().size()){
					Intent intent = new Intent();
					intent.putStringArrayListExtra(RESULT_DATA, (ArrayList<String>) selectMembers);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}
		});
	}

	private boolean inSelectedMembers(String account){
		for(String id:selectMembers){
			if(id.equals(account)){
				return true;
			}
		}
		return false;
	}

	private boolean inAlreadySelectedAccounts(String account){
		for(String id:option.getalreadySelectedAccounts()){
			if(id.equals(account)){
				return true;
			}
		}
		return false;
	}

	private void removeSelectMember(String member){
		int index = -1;
		for(int i = 0; i<selectMembers.size();i++){
			if(member.equals(selectMembers.get(i))){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			selectMembers.remove(index);
		}
	}

	private void addSelectMember(String member){
		int index = -1;
		for(int i = 0; i<selectMembers.size();i++){
			if(member.equals(selectMembers.get(i))){
				index = i;
				break;
			}
		}
		
		if(index == -1){
			selectMembers.add(member);
		}
	}

	private void initMemberList(){
		members = new ArrayList<>();
		selectMembers = new ArrayList<>();
		selectMembers.addAll(option.getalreadySelectedAccounts());
		adapter = new SelectMemberAdapter(SamchatMemberSelectActivity.this,members,selectMembers);
		member_list_listview.setAdapter(adapter);
		member_list_listview.setItemsCanFocus(true);
		member_list_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Contact member = (Contact) parent.getAdapter().getItem(position);
				if(inAlreadySelectedAccounts(member.getAccount())){
					return;
				}

				if(inSelectedMembers(member.getAccount())){
					removeSelectMember(member.getAccount());
					refreshMemberList();
				}else{
					addSelectMember(member.getAccount());
					refreshMemberList();
				}
			}
		});

		member_list_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
	}

	private List<Contact> loadedMembers;
	private void LoadMembers(boolean delay){
		if(memberListLoaded){
			return;
		}
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(memberListLoaded || ((UI)SamchatMemberSelectActivity.this).isDestroyedCompatible()){
					return;
				}
				if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
					loadedMembers = new ArrayList<Contact>(ContactDataCache.getInstance().getMyContacts());
				}else{
					loadedMembers = new ArrayList<Contact>(CustomerDataCache.getInstance().getMyCustomers());
				}
				memberListLoaded = true;
				onMembersLoaded();
			}
		}, delay ? 250 : 0);

	}

	private void onMembersLoaded(){
		members.clear();
		if(loadedMembers != null){
			members.addAll(loadedMembers);
			loadedMembers = null;
		}
		refreshMemberList();
	}

	private void notifyDataSetChangedMember() {
		adapter.notifyDataSetChanged();
	}

	private void refreshMemberList(){
		sortUsers(members);
		notifyDataSetChangedMember();
	}

	private void sortUsers(List<Contact> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, comp);
	}

	private static Comparator<Contact> comp = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
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

	

	public static class Option implements Serializable {
		public int minSelectNum = 1;
		public int maxSelectNum = 2000;
		public List<String> alreadySelectedAccounts = null;

		public Option(int minSelectNum, int maxSelectNum, List<String> alreadySelectedAccounts){
			this.minSelectNum = minSelectNum;
			this.maxSelectNum = maxSelectNum;
			if(alreadySelectedAccounts == null){
				this.alreadySelectedAccounts = new ArrayList<String>();
			}else{
				this.alreadySelectedAccounts = alreadySelectedAccounts;
			}
		}

		public void setminSelectNum(int minSelectNum){
			this.minSelectNum = minSelectNum;
		}

		public int getminSelectNum(){
			return minSelectNum;
		}

		public void setmaxSelectNum(int maxSelectNum){
			this.maxSelectNum = maxSelectNum;
		}

		public int getmaxSelectNum(){
			return maxSelectNum;
		}

		public void setalreadySelectedAccounts(List<String> alreadySelectedAccounts){
			this.alreadySelectedAccounts = alreadySelectedAccounts;
		}

		public List<String> getalreadySelectedAccounts(){
			return alreadySelectedAccounts;
		}

    }

}




