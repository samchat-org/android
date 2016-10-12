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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.adapter.ContactAdapter;
import com.android.samchat.adapter.ContactUserAdapter;
import com.android.samchat.cache.FollowDataCache;
import com.android.samservice.type.TypeEnum;
import com.android.samservice.info.ContactUser;
import com.karics.library.zxing.android.CaptureActivity;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.model.ToolBarOptions;
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
	private ListView searchResultList_listview;

	private String key = null;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_searchpublic_activity);
		setupPanel();
	}

	@Override
    public void onResume() {
		super.onResume();
		refreshContactUserList();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = (FrameLayout)findViewById(R.id.back_arrow_layout);
		search_textview = (TextView) findViewById(R.id.search);
		key_edittext = (EditText) findViewById(R.id.key);
		searchResultList_listview = (ListView) findViewById(R.id.searchResultList);

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
			search_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green);
		}else{
			search_textview.setEnabled(false);
			search_textview.setBackgroundResource(R.drawable.samchat_text_radius_border_green_disable);
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
							onContactUserSearched();
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




