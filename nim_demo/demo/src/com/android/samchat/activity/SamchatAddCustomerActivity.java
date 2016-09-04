package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
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
import com.android.samchat.adapter.PhoneContactsAdapter;
import com.android.samchat.cache.CustomerDataCache;
import com.android.samchat.cache.FollowDataCache;
import com.android.samservice.PhoneNumber;
import com.android.samservice.QuestionInfo;
import com.android.samservice.TypeEnum;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PhoneContact;
import com.android.samservice.info.SendQuestion;
import com.karics.library.zxing.android.CaptureActivity;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.HttpCommClient;
public class SamchatAddCustomerActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatAddCustomerActivity.class.getSimpleName();
	private static final int REQUEST_CODE_SCAN = 0x0000;

    private static final String[]PHONES_PROJECTION={ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.PHOTO_ID,ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    private static final int PHONES_CONTACT_ID_INDEX = 3;

	private FrameLayout back_arrow_layout;
	private ImageView scan_imageview;
	private EditText key_edittext;
	private ListView phone_contacts_listview;

	private String key;
	private List<PhoneContact> contacts;
	private PhoneContactsAdapter adapter;

	private boolean isSending = false;
	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatAddCustomerActivity.class);
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
					query_user_precise(unique_id);
				}else{
					Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
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
		try{
			unique_id = Long.valueOf(content);
		}catch(Exception e){
			LogUtil.i(TAG,"warning: invalid qr code");
		}finally{
			return unique_id;
		}
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		scan_imageview = findView(R.id.scan);
		key_edittext = findView(R.id.key);
		phone_contacts_listview = findView(R.id.phone_contacts);

		setupBackArrowClick();
		setupKeyEditClick();
		setupScanClick();
		setupPhoneContactsListView();
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
		}
	};

	private void setupKeyEditClick(){
		key_edittext.addTextChangedListener(key_textWatcher);	
	}

	private void setupScanClick(){
		scan_imageview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CaptureActivity.startActivityForResult(SamchatAddCustomerActivity.this,REQUEST_CODE_SCAN);
			}
		});
	}
		
	private void setupPhoneContactsListView(){
		contacts = new ArrayList<>();
		getPhoneContacts();
		sortUsers(contacts);
		adapter = new PhoneContactsAdapter(SamchatAddCustomerActivity.this, contacts);
		phone_contacts_listview.setAdapter(adapter);
       phone_contacts_listview.setItemsCanFocus(true);
       phone_contacts_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              if(isSending){
					return;
				}
				PhoneContact contact = (PhoneContact) parent.getAdapter().getItem(position);
				query_user_precise(contact);
			}
		});
	}

	private void getPhoneContacts() {  
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
        
				if(photoid > 0 ) {  
					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
					InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				}else {  
					contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_def);  
				}  
          
				contacts.add(new PhoneContact(contactName, phoneNumber, contactPhoto));
			}  

			phoneCursor.close();  
			
		}  
		
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
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		}).setCanceledOnTouchOutside(false);

		SamService.getInstance().query_user_precise(TypeEnum.UNIQUE_ID, null, unique_id, null, false, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					HttpCommClient hcc = (HttpCommClient)obj;
					if(hcc.users.getcount() > 0){
						if(CustomerDataCache.getInstance().getCustomerByUniqueID(hcc.users.getusers().get(0).getunique_id()) != null){
							DialogMaker.dismissProgressDialog();
							getHandler().postDelayed(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_customer_already_add, Toast.LENGTH_LONG).show();
									isSending = false;
								}
							}, 0);
						}else{
							addCustomer(hcc.users.getusers().get(0));
						}
					}else{
						DialogMaker.dismissProgressDialog();
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
								isSending = false;
							}
						}, 0);
					}
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}


	private void query_user_precise(final PhoneContact contact){
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
						if(CustomerDataCache.getInstance().getCustomerByUniqueID(hcc.users.getusers().get(0).getunique_id()) != null){
							DialogMaker.dismissProgressDialog();
							getHandler().postDelayed(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_customer_already_add, Toast.LENGTH_LONG).show();
									isSending = false;
								}
							}, 0);
						}else{
							addCustomer(hcc.users.getusers().get(0));
						}
					}else{
						sendInviteMsg(contact);
					}
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
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
							isSending = false;
							Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_invite_send, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}

	private void addCustomer(ContactUser user){
		SamService.getInstance().add_contact(Constants.ADD_INTO_CUSTOMER, user, new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					DialogMaker.dismissProgressDialog();
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isSending = false;
							Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_add_customer_succeed, Toast.LENGTH_LONG).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(final int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					DialogMaker.dismissProgressDialog();
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
							isSending = false;
						}
					}, 0);
				}
		});
	}
}






