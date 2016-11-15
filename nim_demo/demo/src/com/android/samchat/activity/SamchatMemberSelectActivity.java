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
import android.widget.ImageView;
import android.widget.ListView;

import com.android.samchat.SamchatGlobal;
import com.android.samchat.adapter.SelectMemberAdapter;
import com.android.samchat.adapter.SelectTeamAdapter;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samservice.info.Contact;
import com.android.samchat.R;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.team.model.Team;

import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SamchatMemberSelectActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatMemberSelectActivity.class.getSimpleName();
	public static final String RESULT_DATA="RESULT_DATA";

	private RelativeLayout titlebar_layout;
	private FrameLayout back_arrow_layout;
	private ImageView back_icon_iv;
	private TextView titlebar_name;
	private FrameLayout invite_layout;
	private TextView invite_tv;
	private ListView member_list_listview;

	private SamchatMemberSelectActivity.Option option;
	private List<String> selectMembers = null;
	private List<Contact> members = null;
	private boolean memberListLoaded = false;
	private SelectMemberAdapter adapter;

	private List<Team> teams = null;
	private boolean teamsListLoaded = false;
	private SelectTeamAdapter teamAdapter;

	private boolean ready=false;

	public enum MemberSelectType {
        BUDDY_CUSTOMER,
        BUDDY_SP,
        TEAM
    }

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
		if(option == null){
			finish();
			return;
		}

		findView();
		setupPanel();

		if(option.gettype() != MemberSelectType.TEAM){
			initMemberList();
			LoadMembers(false);
		}else{
			initTeamList();
			LoadTeams(false);
		}
	}

	private void initTeamList(){
		teams = new ArrayList<>();
		selectMembers = new ArrayList<>();
		teamAdapter = new SelectTeamAdapter(SamchatMemberSelectActivity.this,teams);
		member_list_listview.setAdapter(teamAdapter);
		member_list_listview.setItemsCanFocus(true);
		member_list_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Team team = (Team) parent.getAdapter().getItem(position);
				addSelectMember(team.getId());
				Intent intent = new Intent();
				intent.putStringArrayListExtra(RESULT_DATA, (ArrayList<String>) selectMembers);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

	private List<Team> loadedTeams;
	private void LoadTeams(boolean delay){
		if(teamsListLoaded){
			return;
		}
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(((UI)SamchatMemberSelectActivity.this).isDestroyedCompatible() || teamsListLoaded){
					return;
				}

				loadedTeams = new ArrayList<>();
				for(Team t: TeamDataCache.getInstance().getAllTeams()){
					if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
						if(!t.getCreator().equals(DemoCache.getAccount())){
							loadedTeams.add(t);
						}
					}else{
						if(t.getCreator().equals(DemoCache.getAccount())){
							loadedTeams.add(t);
						}
					}
				}
				teamsListLoaded = true;
				onTeamsLoaded();
			}
		}, delay ? 250 : 0);

	}

	private void onTeamsLoaded(){
		teams.clear();
		if(loadedTeams != null){
			teams.addAll(loadedTeams);
			loadedTeams = null;
		}
		refreshTeamList();
	}

	private void notifyDataSetChangedTeam() {
		teamAdapter.notifyDataSetChanged();
	}

	private void refreshTeamList(){
		notifyDataSetChangedTeam();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void onParseIntent() {
		option = (SamchatMemberSelectActivity.Option)getIntent().getSerializableExtra("option");
	}

	private void findView(){	
		titlebar_layout = findView(R.id.titlebar_layout);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name = findView(R.id.titlebar_name);
		back_icon_iv = findView(R.id.back_icon);
		invite_layout = findView(R.id.invite_layout);
		invite_tv = findView(R.id.invite);
		member_list_listview = findView(R.id.member_list);
	}

	private void setupPanel() {
		setupTitlebar();
		setupBackArrowClick();
		setupInviteClick();
	}

	private boolean isCustomerMode(){
		return (NimUIKit.getCallback().getCurrentMode() == ModeEnum.CUSTOMER_MODE.getValue());
	}

	private void setupTitlebar(){
		if(isCustomerMode()){
			titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
        	back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
        	back_icon_iv.setImageResource(R.drawable.samchat_arrow_left);
        	titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
			//invite_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
			//invite_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
		}else{
			titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
        	back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
        	back_icon_iv.setImageResource(R.drawable.samchat_arrow_left_sp);
        	titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
			invite_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
			invite_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
		}

		if(option.getmuti()){
			invite_layout.setVisibility(View.VISIBLE);
			invite_tv.setVisibility(View.VISIBLE);
		}else{
			invite_layout.setVisibility(View.GONE);
			invite_tv.setVisibility(View.GONE);
		}
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
				if(option.getmuti() && selectMembers.size() > option.getalreadySelectedAccounts().size()){
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
		adapter = new SelectMemberAdapter(SamchatMemberSelectActivity.this,members,selectMembers,option.getmuti());
		member_list_listview.setAdapter(adapter);
		member_list_listview.setItemsCanFocus(true);
		member_list_listview.setOnItemClickListener(option.getmuti() ? new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Contact member = (Contact) parent.getAdapter().getItem(position);
				if(inAlreadySelectedAccounts(member.getAccount())){
					return;
				}

				if(selectMembers.size() + option.getalreadySelectedAccounts().size() >= option.getmaxSelectNum()){
					Toast.makeText(SamchatMemberSelectActivity.this, getString(R.string.samchat_team_max_not_exceed)+" "+option.maxSelectNum, Toast.LENGTH_SHORT).show();
					return;
				}

				if(inSelectedMembers(member.getAccount())){
					removeSelectMember(member.getAccount());
				}else{
					addSelectMember(member.getAccount());
				}
				
				if(selectMembers.size() > option.getalreadySelectedAccounts().size() 
					&& (selectMembers.size()+option.getalreadySelectedAccounts().size())>=option.minSelectNum){
					ready = true;
				}else{
					ready = false;
				}
				updateInvite();
				refreshMemberList();
			}
		} : new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Contact member = (Contact) parent.getAdapter().getItem(position);
				addSelectMember(member.getAccount());
				Intent intent = new Intent();
				intent.putStringArrayListExtra(RESULT_DATA, (ArrayList<String>) selectMembers);
				setResult(Activity.RESULT_OK, intent);
				finish();
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
				if(((UI)SamchatMemberSelectActivity.this).isDestroyedCompatible() || memberListLoaded){
					return;
				}
				if(option.gettype() == MemberSelectType.BUDDY_CUSTOMER){
					loadedMembers = new ArrayList<Contact>(CustomerDataCache.getInstance().getMyCustomers());
				}else if(option.gettype() == MemberSelectType.BUDDY_SP){
					loadedMembers = new ArrayList<Contact>(ContactDataCache.getInstance().getMyContacts());
				}else{
					return;
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
		public boolean muti = false;
		public MemberSelectType type = MemberSelectType.BUDDY_CUSTOMER;

		public Option(int minSelectNum, int maxSelectNum, List<String> alreadySelectedAccounts, boolean muti, MemberSelectType type){
			this.minSelectNum = minSelectNum;
			this.maxSelectNum = maxSelectNum;
			if(alreadySelectedAccounts == null){
				this.alreadySelectedAccounts = new ArrayList<String>();
			}else{
				this.alreadySelectedAccounts = alreadySelectedAccounts;
			}
			this.muti = muti;
			this.type = type;
		}

		public void setmuti(boolean muti){
			this.muti = muti;
		}

		public boolean getmuti(){
			return muti;
		}

		public void settype(MemberSelectType type){
			this.type = type;
		}

		public MemberSelectType gettype(){
			return type;
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




