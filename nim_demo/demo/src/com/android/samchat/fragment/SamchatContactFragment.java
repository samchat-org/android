package com.android.samchat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.service.ErrorString;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.android.samchat.R;
import android.widget.LinearLayout;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;

import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samservice.info.Contact;
import com.android.samchat.adapter.ContactAdapter;
import com.android.samchat.callback.ContactCallback;
import com.android.samchat.cache.ContactDataCache;
import com.android.samchat.cache.CustomerDataCache;
import android.widget.RelativeLayout;
import com.android.samchat.ui.SideBar;
import com.android.samchat.ui.SideBar.OnSideBarTouchingLetterChangedListener;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;

/**
 * Main Fragment in SamchatContactListFragment
 */
public class SamchatContactFragment extends TFragment {
	private static final String TAG="SamchatContactFragment";
	/*customer mode*/
	//view
	private LinearLayout customer_contact_layout;
	private ListView customer_contact_list;
	private RelativeLayout customer_main;
	private TextView customer_dialog;
	private SideBar customer_sidebar;
	
	//data
	private List<Contact> contactList;
	private ContactAdapter contactAdapter;
	private boolean contactListLoaded = false;
	private ContactCallback contact_callback;
	
	/*sp mode*/
	//view
	private LinearLayout sp_contact_layout;
	private ListView sp_contact_list;
	private RelativeLayout sp_main;
	private TextView sp_dialog;
	private SideBar sp_sidebar;
	//data
	private List<Contact> customerList;
	private ContactAdapter customerAdapter;
	private boolean customerListLoaded = false;
	private ContactCallback customer_callback;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_CONTACTLIST_UPDATE);
		filter.addAction(Constants.BROADCAST_CUSTOMERLIST_UPDATE);
		filter.addAction(Constants.BROADCAST_USER_INFO_UPDATE);
		filter.addAction(Constants.BROADCAST_CHAT_BLOCK_MUTE_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
						customer_contact_layout.setVisibility(View.VISIBLE);
						sp_contact_layout.setVisibility(View.GONE);
					}else{
						customer_contact_layout.setVisibility(View.GONE);
						sp_contact_layout.setVisibility(View.VISIBLE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_CONTACTLIST_UPDATE)){
					loadedContacts = new ArrayList<Contact>(ContactDataCache.getInstance().getMyContacts());
					onContactsLoaded();
				}else if(intent.getAction().equals(Constants.BROADCAST_CUSTOMERLIST_UPDATE)){
					loadedCustomers= new ArrayList<Contact>(CustomerDataCache.getInstance().getMyCustomers());
					onCustomersLoaded();
				}else if(intent.getAction().equals(Constants.BROADCAST_USER_INFO_UPDATE)){
					ContactUser updateUser = (ContactUser)intent.getSerializableExtra("user");
					userInfoUpdate(updateUser);
					//refreshContactList();
					//refreshCustomerList();
				}else if(intent.getAction().equals(Constants.BROADCAST_CHAT_BLOCK_MUTE_UPDATE)){
					refreshContactList();
					refreshCustomerList();
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

	public SamchatContactFragment(){
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.samchat_contact_fragment_layout, container, false);
	}

    @Override
    public void onDestroyView(){
        unregisterBroadcastReceiver();
        super.onDestroyView();
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findViews();
		initContactList();
		LoadContacts(true);

		initCustomerList();
		LoadCustomers(true);

		registerBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void findViews() {
		//customer mode views
		customer_contact_layout= (LinearLayout) findView(R.id.customer_contact_layout);
		customer_contact_list = (ListView) findView(R.id.customer_contact_list);
		customer_main = (RelativeLayout) findView(R.id.customer_main);
		customer_dialog = (TextView) findView(R.id.customer_dialog);
		customer_sidebar = (SideBar) findView(R.id.customer_sidebar);
		customer_sidebar.setTextView(customer_dialog);
		customer_sidebar.setOnTouchingLetterChangedListener(new OnSideBarTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// TODO Auto-generated method stub
				int position = contactAdapter.getPositionForSelection(s.charAt(0));

				if (position != -1) {
					customer_contact_list.setSelection(position);
				}
			}
		});
		//sp mode views
		sp_contact_layout= (LinearLayout) findView(R.id.sp_contact_layout);
		sp_contact_list = (ListView) findView(R.id.sp_contact_list);
		sp_main = (RelativeLayout) findView(R.id.sp_main);
		sp_dialog = (TextView) findView(R.id.sp_dialog);
		sp_sidebar = (SideBar) findView(R.id.sp_sidebar);
		sp_sidebar.setTextView(sp_dialog);
		sp_sidebar.setOnTouchingLetterChangedListener(new OnSideBarTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// TODO Auto-generated method stub
				int position = customerAdapter.getPositionForSelection(s.charAt(0));

				if (position != -1) {
					sp_contact_list.setSelection(position);
				}
			}
		});

		if(SamchatGlobal.getmode()== ModeEnum.CUSTOMER_MODE){
			customer_contact_layout.setVisibility(View.VISIBLE);
			sp_contact_layout.setVisibility(View.GONE);
		}else{
			customer_contact_layout.setVisibility(View.GONE);
			sp_contact_layout.setVisibility(View.VISIBLE);
		}
    }

	private void initContactList(){
		contactList = new ArrayList<Contact>();
		contactAdapter = new ContactAdapter(getActivity(),contactList,0);
		customer_contact_list.setAdapter(contactAdapter);
		customer_contact_list.setItemsCanFocus(true);
		customer_contact_list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (contact_callback != null) {
					Contact ui = (Contact) parent.getAdapter().getItem(position);
					if(ui != null){
						contact_callback.onItemClick(ui);
						LogUtil.i(TAG,"start chatting activity for ui:"+ui.getunique_id());
					}
				}
			}
		});

		customer_contact_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Contact ui = (Contact) parent.getAdapter().getItem(position);
				if(ui != null){
					showLongClickMenuCustomerContactList(ui);
				}
				return true;
			}
		});
	}

	private void showLongClickMenuCustomerContactList(final Contact ui) {
		CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
		String title = getString(R.string.samchat_delete);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				deleteContact(ui,Constants.REMOVE_OUT_CONTACT);
			}
		});
		alertDialog.show();
	}

	private List<Contact> loadedContacts;
	private void LoadContacts(boolean delay){
		if(contactListLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(contactListLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}

				loadedContacts = new ArrayList<Contact>(ContactDataCache.getInstance().getMyContacts());
				
				contactListLoaded = true;
				if(isAdded()){
					onContactsLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onContactsLoaded(){
		contactList.clear();
		if(loadedContacts != null){
			contactList.addAll(loadedContacts);
			loadedContacts = null;
		}

		refreshContactList();
	}

	private void notifyDataSetChangedContact() {
		contactAdapter.notifyDataSetChanged();
	}

	private void refreshContactList(){
		sortUsers(contactList);
		notifyDataSetChangedContact();
	}

	public void setContactCallback(ContactCallback callback) {
		contact_callback = callback;
	}

	private void userInfoUpdate(ContactUser updateUser){
		int updateContactIndex = -1;
		boolean contactNameUpdate = false;
		int updateCustomerIndex = -1;
		boolean customerNameUpdate = false;
		
		for(int i=0; i<contactList.size(); i++){
			if(updateUser.getunique_id() == contactList.get(i).getunique_id()){
				if(!updateUser.getusername().equals(contactList.get(i).getusername())){
					contactList.get(i).setusername(updateUser.getusername());
					contactNameUpdate = true;
				}
				contactList.get(i).setavatar(updateUser.getavatar());
				contactList.get(i).setservice_category(updateUser.getservice_category());
				contactList.get(i).setlastupdate(updateUser.getlastupdate());
				updateContactIndex = i;
				break;
			}
		}

		for(int i=0; i<customerList.size(); i++){
			if(updateUser.getunique_id() == customerList.get(i).getunique_id()){
				if(!updateUser.getusername().equals(customerList.get(i).getusername())){
					customerList.get(i).setusername(updateUser.getusername());
					customerNameUpdate = true;
				}
				customerList.get(i).setavatar(updateUser.getavatar());
				customerList.get(i).setservice_category(updateUser.getservice_category());
				customerList.get(i).setlastupdate(updateUser.getlastupdate());
				updateCustomerIndex = i;
				break;
			}
		}

		if(updateContactIndex != -1){
			if(contactNameUpdate){
				refreshContactList();
			}else{
				Object tag = ListViewUtil.getViewHolderByIndex(customer_contact_list, updateContactIndex);
				ContactAdapter.ViewHolder viewHolder = (ContactAdapter.ViewHolder) tag;
				if(viewHolder != null){
					ContactAdapter.ViewHolder.refreshItem(viewHolder,contactList.get(updateContactIndex),0);
				}
			}
		}

		if(updateCustomerIndex != -1){
			if(customerNameUpdate){
				refreshCustomerList();
			}else{
				Object tag = ListViewUtil.getViewHolderByIndex(sp_contact_list, updateCustomerIndex);
				ContactAdapter.ViewHolder viewHolder = (ContactAdapter.ViewHolder) tag;
				if(viewHolder != null){
					ContactAdapter.ViewHolder.refreshItem(viewHolder,customerList.get(updateCustomerIndex),1);
				}
			}
		}
	}



/*******************************Service Provider View*************************************************/
	private void initCustomerList(){
		customerList = new ArrayList<Contact>();
		customerAdapter = new ContactAdapter(getActivity(),customerList,1);
		sp_contact_list.setAdapter(customerAdapter);
		sp_contact_list.setItemsCanFocus(true);
		sp_contact_list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (customer_callback != null) {
					Contact ui = (Contact) parent.getAdapter().getItem(position);
					if(ui != null){
						customer_callback.onItemClick(ui);
					}
				}
			}
		});

		sp_contact_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Contact ui = (Contact) parent.getAdapter().getItem(position);
				if(ui != null){
					showLongClickMenuSpContactList(ui);
				}
				return true;
			}
		});
		
	}

	private void showLongClickMenuSpContactList(final Contact ui) {
		CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
		String title = getString(R.string.samchat_delete);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				deleteContact(ui,Constants.REMOVE_OUT_CUSTOMER);
			}
		});
		alertDialog.show();
	}

	private List<Contact> loadedCustomers;
	private void LoadCustomers(boolean delay){
		if(customerListLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(customerListLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}

				loadedCustomers = new ArrayList<Contact>(CustomerDataCache.getInstance().getMyCustomers());
				
				customerListLoaded = true;
				if(isAdded()){
					onCustomersLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onCustomersLoaded(){
		customerList.clear();
		if(loadedCustomers != null){
			customerList.addAll(loadedCustomers);
			loadedCustomers = null;
		}

		refreshCustomerList();
	}

	private void notifyDataSetChangedCustomer() {
		customerAdapter.notifyDataSetChanged();
	}

	private void refreshCustomerList(){
		sortUsers(customerList);
		notifyDataSetChangedCustomer();
	}

	public void setCustomerCallback(ContactCallback callback) {
		customer_callback = callback;
	}


	private void sortUsers(List<Contact> list) {
		if (list.size() == 0) {
			return;
		}
		for(Contact user: list){
			user.updateFPinYin();
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



/*************************************Service Provide Mode***************************/
	private void deleteContact(Contact ui, int type){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ui.getunique_id());
		if(user == null){
			return;
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().remove_contact(type, user ,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(getActivity(),code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(getActivity(), null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(getActivity(),code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(getActivity(), null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}
		} );

	}
}



