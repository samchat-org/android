package com.android.samchat.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.adapter.ContactUserAdapter;
import com.android.samchat.adapter.PhoneContactsAdapter;
import com.android.samchat.cache.ContactDataCache;
import com.android.samservice.info.PhoneNumber;
import com.android.samservice.type.TypeEnum;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PhoneContact;
import com.karics.library.zxing.android.CaptureActivity;
import com.android.samchat.R;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.android.samservice.HttpCommClient;
public class SamchatAddServiceProviderActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatAddServiceProviderActivity.class.getSimpleName();
	private static final int REQUEST_CODE_SCAN = 0x0000;

    private static final String[]PHONES_PROJECTION={ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.PHOTO_ID,ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    private static final int PHONES_CONTACT_ID_INDEX = 3;

	private FrameLayout back_arrow_layout;
	private FrameLayout scan_layout;
	private EditText key_edittext;
	private ListView phone_contacts_listview;
	private ListView search_result_listview;

	private String key;
	private List<PhoneContact> contacts;
	private PhoneContactsAdapter adapter;

	private List<ContactUser> search_result;
	private ContactUserAdapter contactUserAdapter;

	private boolean isSending = false;
	private boolean isSearching = false;
	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatAddServiceProviderActivity.class);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
			if (data != null) {
				String content = data.getStringExtra("codedContent");
				long unique_id = transferToUniqueID(content);
				if(unique_id >0){
					if(isSending || isSearching){
						return;
					}
					query_user_precise(unique_id);
				}else{
					Toast.makeText(SamchatAddServiceProviderActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_addserviceprovider_activity);

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

	private long transferToUniqueID(String content){
		long unique_id = -1;
		if(content.indexOf(NimConstants.QRCODE_PREFIX)!=0){
			return 0;
		}

		String content2 = content.substring(NimConstants.QRCODE_PREFIX.length());
		try{
			unique_id = Long.valueOf(content2);
		}catch(Exception e){
			e.printStackTrace();
			LogUtil.i(TAG,"warning: invalid qr code");
		}finally{
			return unique_id;
		}
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		scan_layout = findView(R.id.scan_layout);
		key_edittext = findView(R.id.key);
		phone_contacts_listview = findView(R.id.phone_contacts);
		search_result_listview = findView(R.id.search_result);

		setupBackArrowClick();
		setupKeyEditClick();
		setupScanClick();
		setupPhoneContactsListView();
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
			if(key.length() == 0){
				phone_contacts_listview.setVisibility(View.VISIBLE);
				search_result_listview.setVisibility(View.GONE);
			}
		}
	};

	private void setupKeyEditClick(){
		key_edittext.addTextChangedListener(key_textWatcher);	
		key_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override  
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
				if(actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH){
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) {  
						imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);  
					}
					query_user_by_key();
					return true;  
				}  
				return false;  
			}  
		});
	}

	private void setupScanClick(){
		scan_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CaptureActivity.startActivityForResult(SamchatAddServiceProviderActivity.this,REQUEST_CODE_SCAN, ModeEnum.CUSTOMER_MODE.getValue(),true);
			}
		});
	}
		
	private void setupPhoneContactsListView(){
		contacts = new ArrayList<>();
		adapter = new PhoneContactsAdapter(SamchatAddServiceProviderActivity.this, contacts);
		phone_contacts_listview.setAdapter(adapter);
       phone_contacts_listview.setItemsCanFocus(true);
       phone_contacts_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              if(isSending || isSearching){
					return;
				}
				PhoneContact contact = (PhoneContact) parent.getAdapter().getItem(position);
				query_user_precise(contact);
			}
		});

		LoadPhoneContacts();
	}

	private List<PhoneContact> loadedContacts;
	private void LoadPhoneContacts() {
		NimSingleThreadExecutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				loadedContacts = new ArrayList<>();
				ContentResolver resolver = getBaseContext().getContentResolver();
				Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);
      
				if (phoneCursor != null) {  
					while (phoneCursor.moveToNext()) {  
						String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
						if (TextUtils.isEmpty(phoneNumber))
							continue;  
						String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
						Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  
						Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);  
						Bitmap contactPhoto = null;
						/*if(photoid > 0 ) {  
							Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
							InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
							contactPhoto = BitmapFactory.decodeStream(input);
						}else {  
							contactPhoto = null;//BitmapFactory.decodeResource(getResources(), R.drawable.avatar_def);  
						}*/
						loadedContacts.add(new PhoneContact(contactName, phoneNumber, contactPhoto));
					}  
					phoneCursor.close();  
				}  

				getHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (((UI)SamchatAddServiceProviderActivity.this).isDestroyedCompatible()){
							return;
						}
						onContactsLoaded();
					}
				}, 0);
			}
		});
	}  

	private void onContactsLoaded(){
		contacts.clear();
		if(loadedContacts != null){
			contacts.addAll(loadedContacts);
			loadedContacts = null;
		}
		sortUsers(contacts);
		notifyDataSetChanged();
	}

	private void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}
	
	private void sortUsers(List<PhoneContact> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, comp);
	}

	private static Comparator<PhoneContact> comp = new Comparator<PhoneContact>() {
		@Override
		public int compare(PhoneContact lhs, PhoneContact rhs) {
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

/******************************************Data Flow Control***********************************************************/
	private void query_user_precise(long unique_id){
		isSending=true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID, null, unique_id, null, false, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					final HttpCommClient hcc = (HttpCommClient)obj;
					if(hcc.users.getcount() > 0){
						if(hcc.users.getusers().get(0).getusertype() != Constants.SAM_PROS){
							getHandler().postDelayed(new Runnable() {
								@Override
								public void run() {
									DialogMaker.dismissProgressDialog();
									Toast.makeText(SamchatAddServiceProviderActivity.this, R.string.samchat_no_sp, Toast.LENGTH_LONG).show();
									isSending = false;
								}
							}, 0);
						}else{
							getHandler().postDelayed(new Runnable() {
								@Override
								public void run() {
									DialogMaker.dismissProgressDialog();
									SamchatContactUserSPNameCardActivity.start(SamchatAddServiceProviderActivity.this,hcc.users.getusers().get(0));
									isSending = false;
								}
							}, 0);
						}
					}else{
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								Toast.makeText(SamchatAddServiceProviderActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
								isSending = false;
							}
						}, 0);
					}
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}

	private void showInviteDialog(final PhoneContact contact){
		EasyAlertDialogHelper.OnDialogActionListener listener = new EasyAlertDialogHelper.OnDialogActionListener() {
			@Override
			public void doCancelAction() {

			}

			@Override
			public void doOkAction() {
				LogUtil.i(TAG,"send invitation sms");
				sendInviteMsg(contact);
			}
		};

		final EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(SamchatAddServiceProviderActivity.this, getString(R.string.samchat_invite_title),
			getString(R.string.samchat_invite_desc), true, listener);
		dialog.show();
	}

	private void query_user_precise(final PhoneContact contact){
		isSending = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().query_user_precise(TypeEnum.CELLPHONE, contact.getphone(), 0, null, false, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					HttpCommClient hcc = (HttpCommClient)obj;
					if(hcc.users.getcount() > 0){
						final ContactUser user = hcc.users.getusers().get(0);
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								SamchatContactUserSPNameCardActivity.start(SamchatAddServiceProviderActivity.this, user);
								isSending = false;
							}
						}, 0);
					}else{
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								isSending = false;
								showInviteDialog(contact);
							}
						}, 0);
					}
				}

				@Override
				public void onFailed(final int code) {
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}

	private void sendInviteMsg(PhoneContact contact){
		PhoneNumber ph = new PhoneNumber("",contact.getphone());
		List<PhoneNumber> lists = new ArrayList<>();
		lists.add(ph);
		
		SamService.getInstance().send_invite_msg(lists, "", new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(SamchatAddServiceProviderActivity.this, R.string.samchat_invite_send, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}
		});
	}

	private void addServiceProvider(ContactUser user){
		SamService.getInstance().add_contact(Constants.ADD_INTO_CONTACT, user, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isSending = false;
							Toast.makeText(SamchatAddServiceProviderActivity.this, R.string.samchat_add_sp_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}

	private void setupSearchResultListView(){
		search_result = new ArrayList<>();
		contactUserAdapter = new ContactUserAdapter(SamchatAddServiceProviderActivity.this, search_result);
       contactUserAdapter.setFollowVisible(false);
		search_result_listview.setAdapter(contactUserAdapter);
       search_result_listview.setItemsCanFocus(true);
       search_result_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactUser user = (ContactUser) parent.getAdapter().getItem(position);
				SamchatContactUserSPNameCardActivity.start(SamchatAddServiceProviderActivity.this, user);
			}
		});
	}

	private void notifyDataContactUser() {
		contactUserAdapter.notifyDataSetChanged();
	}

	private void refreshContactUserList(){
		sortContactUser(search_result);
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

	private List<ContactUser> loadedContactUser;
	private void onContactUserSearched() {	
		search_result.clear();
		if (loadedContactUser != null) {
			search_result.addAll(loadedContactUser);
			loadedContactUser = null;
		}
		refreshContactUserList();
	}
		
	private void query_user_by_key(){
		if(isSending || isSearching || TextUtils.isEmpty(key)){
			return;
		}
		isSearching = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);
		SamService.getInstance().query_public(key,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL,
				null,null,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSearching = false;
							phone_contacts_listview.setVisibility(View.GONE);
							search_result_listview.setVisibility(View.VISIBLE);
							HttpCommClient hcc = (HttpCommClient)obj;
							loadedContactUser = hcc.users.getusers();
							onContactUserSearched();
						}
					});
				}

				@Override
				public void onFailed(int code) {
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					});
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatAddServiceProviderActivity.this,code);
                    runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddServiceProviderActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSearching = false;
						}
					});
				}
		} );
	}
}





