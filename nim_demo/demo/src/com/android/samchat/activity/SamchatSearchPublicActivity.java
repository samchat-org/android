package com.android.samchat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.adapter.ContactUserAdapter;
import com.android.samchat.cache.FollowDataCache;
import com.android.samservice.type.TypeEnum;
import com.android.samservice.info.ContactUser;
import com.android.samchat.R;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshListView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.android.samservice.HttpCommClient;
public class SamchatSearchPublicActivity extends Activity {
	private static final String TAG = SamchatSearchPublicActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView search_textview;
	private EditText key_edittext;
	private PullToRefreshListView searchResultList_listview;

	private String key = null;
	private String searchKey=null;

	private boolean ready_search = false;

	private boolean isSearching = false;

	private List<ContactUser> items_search_result;
	private ContactUserAdapter adapter;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;
	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(SamchatSearchPublicActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_FOLLOWEDSP_UPDATE)){
					refreshContactUserList();
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
	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatSearchPublicActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_searchpublic_activity);
		setupPanel();
		registerBroadcastReceiver();
	}

	@Override
    public void onResume() {
		super.onResume();
		//refreshContactUserList();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterBroadcastReceiver();
	}

	private void setupPanel() {
		back_arrow_layout = (FrameLayout)findViewById(R.id.back_arrow_layout);
		search_textview = (TextView) findViewById(R.id.search);
		key_edittext = (EditText) findViewById(R.id.key);
		searchResultList_listview = (PullToRefreshListView) findViewById(R.id.searchResultList);

		setupBackArrowClick();
		setupSearchClick();
		setupKeyEditClick();
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
		if(ready_search){
			search_textview.setEnabled(true);
			search_textview.setBackgroundResource(R.drawable.samchat_button_green_active);
		}else{
			search_textview.setEnabled(false);
			search_textview.setBackgroundResource(R.drawable.samchat_button_green_inactive);
		}
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
				searchKey = key;
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

	private void setupSearchResultListView(){
		items_search_result = new ArrayList<>();
		adapter = new ContactUserAdapter(SamchatSearchPublicActivity.this, items_search_result);
		searchResultList_listview.setAdapter(adapter);
       searchResultList_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactUser user = (ContactUser) parent.getAdapter().getItem(position);
				SamchatContactUserSPNameCardActivity.start(SamchatSearchPublicActivity.this, user);
			}
		});

		searchResultList_listview.setMode(PullToRefreshBase.Mode.DISABLED);
		searchResultList_listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
				LogUtil.i(TAG,"loading more result by pull up");
				if(!isSearching){
					isSearching = true;
					searchPublicByPullUp();
				}
			}
		});
	}

	private void notifyDataContactUser() {
		adapter.notifyDataSetChanged();
	}

	private void refreshContactUserList(){
		//sortContactUser(items_search_result);
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
		SamService.getInstance().query_public(0,searchKey,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,
				null,null,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSearching = false;
							HttpCommClient hcc = (HttpCommClient)obj;
							loadedContactUser = hcc.users.getusers();
							int size = (loadedContactUser == null?0:loadedContactUser.size());
							onContactUserSearched();
							if(size < Constants.MAX_SEARCH_PUBLIC_EACH_TIME){
								searchResultList_listview.setMode(PullToRefreshBase.Mode.DISABLED);
							}else{
								searchResultList_listview.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
							}
						}
					});
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);

                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					});
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);
                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					});
				}
		} );

	}

	private void searchPublicByPullUp(){
		SamService.getInstance().query_public(items_search_result.size(),searchKey,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,
				null,null,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							isSearching = false;
							HttpCommClient hcc = (HttpCommClient)obj;
							loadedContactUser = hcc.users.getusers();
							if(loadedContactUser == null || loadedContactUser.size() == 0){
								searchResultList_listview.onRefreshComplete();
								searchResultList_listview.setMode(PullToRefreshBase.Mode.DISABLED);
							}else if(loadedContactUser.size() < Constants.MAX_SEARCH_PUBLIC_EACH_TIME){
								items_search_result.addAll(loadedContactUser);
								refreshContactUserList();
								searchResultList_listview.onRefreshComplete();
								searchResultList_listview.setMode(PullToRefreshBase.Mode.DISABLED);
							}else{
								items_search_result.addAll(loadedContactUser);
								refreshContactUserList();
								searchResultList_listview.onRefreshComplete();
							}
						}
					});
				}

				@Override
				public void onFailed(int code) {
			     runOnUiThread(new Runnable() {
						@Override
						public void run() {
							isSearching = false;
							searchResultList_listview.onRefreshComplete();
						}
					});
				}

				@Override
				public void onError(int code) {
                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							isSearching = false;
							searchResultList_listview.onRefreshComplete();
						}
					});
				}
		} );

	}

	private void query_user_precise(long unique_id){
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID, null, unique_id, null, false, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					HttpCommClient hcc = (HttpCommClient)obj;
					DialogMaker.dismissProgressDialog();
					if(hcc.users.getcount() > 0){
						if(FollowDataCache.getInstance().getFollowSPByUniqueID(hcc.users.getusers().get(0).getunique_id()) != null){
                            runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(SamchatSearchPublicActivity.this, R.string.samchat_sp_already_followed, Toast.LENGTH_LONG).show();
								}
							});
						}else{
							SamchatContactUserSPNameCardActivity.start(SamchatSearchPublicActivity.this, hcc.users.getusers().get(0));
						}
					}else{
                        runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(SamchatSearchPublicActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
							}
						});
					}
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);

                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					});
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatSearchPublicActivity.this,code);

                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatSearchPublicActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					});
				}
		});
	}

}




