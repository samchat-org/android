package com.android.samchat.activity;

import android.Manifest;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.samchat.adapter.PhoneContactsAdapter;
import com.android.samservice.callback.SMCallBack;
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
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.android.samchat.service.ErrorString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.android.samservice.HttpCommClient;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;

public class SamchatAddCustomerActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatAddCustomerActivity.class.getSimpleName();
	private final int BASIC_PERMISSION_REQUEST_CODE = 100;
	private final int REQUEST_CODE_SCAN = 101;

    private static final String[]PHONES_PROJECTION={ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.PHOTO_ID,ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    private static final int PHONES_CONTACT_ID_INDEX = 3;

	private FrameLayout back_arrow_layout;
	private FrameLayout scan_layout;
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
					if(isSending){
						return;
					}
					query_user_precise(unique_id);
				}else{
					Toast.makeText(SamchatAddCustomerActivity.this, R.string.samchat_qr_code_invalid, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private void requestBasicPermission() {
        MPermission.with(SamchatAddCustomerActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.READ_CONTACTS
                )
                .request();
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionSuccess(){
		LoadPhoneContacts();
	}

	@OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_read_contact), Toast.LENGTH_SHORT).show();
	}

	@OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionNeverAskAgainFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_read_contact), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_addcustomer_activity);

		requestBasicPermission();

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
		phone_contacts_listview = findView(R.id.phone_contacts);

		setupBackArrowClick();
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

	private void setupScanClick(){
		scan_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CaptureActivity.startActivityForResult(SamchatAddCustomerActivity.this,REQUEST_CODE_SCAN, ModeEnum.SP_MODE.getValue(),true);
			}
		});
	}

	private void setupPhoneContactsListView(){
		contacts = new ArrayList<>();
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

	private List<PhoneContact> loadedContacts;
	private void LoadPhoneContacts() {
		NimSingleThreadExecutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				Cursor phoneCursor = null;
				try{
					loadedContacts = new ArrayList<>();
					ContentResolver resolver = getBaseContext().getContentResolver();
					phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);
      
					if (phoneCursor != null) {  
						while (phoneCursor.moveToNext()) {  
							String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
							if (TextUtils.isEmpty(phoneNumber))
								continue;  
							String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
							Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  
							Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);  
						
							loadedContacts.add(new PhoneContact(contactName, phoneNumber, null));
						}  
						phoneCursor.close();  
					}  

					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							if (((UI)SamchatAddCustomerActivity.this).isDestroyedCompatible()){
								return;
							}
							onContactsLoaded();
						}
					}, 0);
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					try{
						if(phoneCursor != null){
							phoneCursor.close();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
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
		isSending = true;
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
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
								isSending = false;
								SamchatContactUserNameCardActivity.start(SamchatAddCustomerActivity.this, hcc.users.getusers().get(0),false);
							}
						}, 0);
					}else{
						getHandler().postDelayed(new Runnable() {
							@Override
							public void run() {
								DialogMaker.dismissProgressDialog();
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

		final EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(SamchatAddCustomerActivity.this, getString(R.string.samchat_invite_title),
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
								SamchatContactUserNameCardActivity.start(SamchatAddCustomerActivity.this, user,false);
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
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							if(code == Constants.RET_USER_NOT_EXISTED){
								showInviteDialog(contact);
							}else{
								EasyAlertDialogHelper.showOneButtonDiolag(SamchatAddCustomerActivity.this, null,
                    				error.reminder, getString(R.string.samchat_ok), true, null);
							}
							isSending = false;
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(SamchatAddCustomerActivity.this,code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
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






