package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.samchat.SamchatGlobal;
import com.android.samchat.adapter.SelectMemberAdapter;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samservice.info.Contact;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.model.ToolBarOptions;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SamchatMemberSelectActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatMemberSelectActivity.class.getSimpleName();
	public static final String RESULT_DATA="RESULT_DATA";

	private FrameLayout back_arrow_layout;
	private FrameLayout invite_layout;
	private TextView invite_tv;
	private ListView member_list_listview;

	private SamchatMemberSelectActivity.Option option;
	private List<String> selectMembers = null;
	private List<Contact> members = null;
	private boolean memberListLoaded = false;

	private SelectMemberAdapter adapter;

	private boolean ready=false;

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
		invite_layout = findView(R.id.invite_layout);
		invite_tv = findView(R.id.invite);
		member_list_listview = findView(R.id.member_list);
	}

	private void setupPanel() {
		setupBackArrowClick();
		setupInviteClick();
	}

	private void updateInvite(){
		if(ready){
			invite_layout.setEnabled(true);
			invite_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
			invite_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
		}else{
			invite_layout.setEnabled(false);
			invite_tv.setTextColor(getResources().getColor(R.color.samchat_color_grey));
			invite_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp_inactive);
		}
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupInviteClick(){
		invite_layout.setOnClickListener(new OnClickListener() {
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
		updateInvite();
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
				}else{
					addSelectMember(member.getAccount());
				}
				
				if(selectMembers.size() > option.getalreadySelectedAccounts().size()){
					ready = true;
				}else{
					ready = false;
				}
				updateInvite();
				refreshMemberList();
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




