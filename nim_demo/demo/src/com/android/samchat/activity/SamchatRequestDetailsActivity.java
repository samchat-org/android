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
import com.netease.nim.uikit.NimConstants;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nim.uikit.recent.viewholder.TeamRecentViewHolder;
import com.android.samchat.viewholder.SamchatCommonRecentViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.android.samservice.QuestionInfo;
import android.widget.ListView;
import java.util.List;
import java.util.ArrayList;
import com.android.samchat.adapter.SamchatRecentContactAdapter;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.MsgService;
import java.util.Map;
import com.android.samchat.type.ModeEnum;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import com.android.samservice.info.SendQuestion;
import java.util.Collections;
import java.util.Comparator;
import com.android.samservice.info.MsgSession;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.android.samchat.viewholder.SamchatRecentViewHolder;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nim.uikit.recent.viewholder.RecentViewHolder;


public class SamchatRequestDetailsActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRequestDetailsActivity.class.getSimpleName();

	public class SamchatCustomerAdapterDelegate implements TAdapterDelegate {
		@Override
    	public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
			SessionTypeEnum type = items_customer.get(position).getSessionType();
			if (type == SessionTypeEnum.Team) {
				return TeamRecentViewHolder.class;
			} else {
				return SamchatCommonRecentViewHolder.class;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean enabled(int position) {
			return true;
		}
	};

	private FrameLayout back_arrow_layout;
	private HeadImageView avatar_headimageview;
	private TextView question_textview;
	private TextView location_textview;

	private QuestionInfo info;

	// customer mode 
	// view
	private ListView listView_customer;
	// data
	private List<RecentContact> items_customer;
	private SamchatRecentContactAdapter adapter_customer;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_CUSTOMER_ITEMS_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_CUSTOMER_ITEMS_UPDATE)){
					requestMessagesCustomer();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}


	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}
	
	public static void start(Context context,QuestionInfo info) {
		Intent intent = new Intent(context, SamchatRequestDetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("info", info);
		intent.putExtras(bundle);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_requestdetails_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();

		setupPanel();

		initMessageListCustomer();

		registerBroadcastReceiver();
		registerObservers(true);

		requestMessagesCustomer();

	}

	@Override
    public void onResume() {
		super.onResume();
		SamDBManager.getInstance().asyncClearSendQuestionUnreadCount(info.getquestion_id());
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		registerObservers(false);
		unregisterBroadcastReceiver();
		SamDBManager.getInstance().asyncClearSendQuestionUnreadCount(info.getquestion_id());
	}

	private void onParseIntent() {
		info = (QuestionInfo)getIntent().getSerializableExtra("info");
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		avatar_headimageview = findView(R.id.avatar);
		question_textview = findView(R.id.question);
		location_textview = findView(R.id.location);
		listView_customer = findView(R.id.listView_customer);

		avatar_headimageview.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount());
		question_textview.setText(info.getquestion());
		location_textview.setText(info.getaddress());

		setupBackArrowClick();

	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	RecentContactsCallback callback_customer = new RecentContactsCallback() {
            @Override
            public void onRecentContactsLoaded() {
                
            }

            @Override
            public void onUnreadCountChange(int unreadCount) {
                
            }

            @Override
            public void onItemClick(RecentContact recent) {
                switch (recent.getSessionType()) {
                    case P2P:
                        SessionHelper.startP2PSession(SamchatRequestDetailsActivity.this, recent.getContactId());
                        break;
                    case Team:
                        SessionHelper.startTeamSession(SamchatRequestDetailsActivity.this, recent.getContactId());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public String getDigestOfAttachment(MsgAttachment attachment) {
                 if (attachment instanceof GuessAttachment) {
                    GuessAttachment guess = (GuessAttachment) attachment;
                    return guess.getValue().getDesc();
                } else if (attachment instanceof RTSAttachment) {
                    return "["+getString(R.string.samchat_RTS)+"]";
                } else if (attachment instanceof StickerAttachment) {
                    return "["+getString(R.string.samchat_sticker)+"]";
                } else if (attachment instanceof SnapChatAttachment) {
                    return "["+getString(R.string.samchat_snapchat)+"]";
                }

                return null;
            }

            @Override
            public String getDigestOfTipMsg(RecentContact recent) {
                String msgId = recent.getRecentMessageId();
                List<String> uuids = new ArrayList<>(1);
                uuids.add(msgId);
                List<IMMessage> msgs = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                if (msgs != null && !msgs.isEmpty()) {
                    IMMessage msg = msgs.get(0);
                    Map<String, Object> content = msg.getRemoteExtension();
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get("content");
                    }
                }

                return null;
            }
        };

	private void initMessageListCustomer() {
		//init customer list view
		items_customer = new ArrayList<>();
		adapter_customer = new SamchatRecentContactAdapter(this, items_customer , new SamchatCustomerAdapterDelegate());
		adapter_customer.setmode(ModeEnum.CUSTOMER_MODE.ordinal());
		listView_customer.setAdapter(adapter_customer);
		listView_customer.setItemsCanFocus(true);
		listView_customer.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (callback_customer != null) {
                    RecentContact recent = (RecentContact) parent.getAdapter().getItem(position);
                    callback_customer.onItemClick(recent);
                }
            }
        });

		listView_customer.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				adapter_customer.onMutable(scrollState == SCROLL_STATE_FLING);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

	private List<String> parseSp_ids(String ids){
		List<String> sq_ids = new ArrayList<String>();
		if(ids == null){
			return sq_ids;
		}

		String []  array = ids.split(":");
		for(String id:array){
			sq_ids.add(id);
		}

		return sq_ids;
	}

	private boolean isMine(List<String> sq_ids , String rcid){
		for(String id: sq_ids){
			if(id.equals(rcid)){
				return true;
			}
		}
		return false;
	}

    private void addTag(RecentContact recent, long tag) {
        tag = recent.getTag() | tag;
        recent.setTag(tag);
    }

    private void removeTag(RecentContact recent, long tag) {
        tag = recent.getTag() & ~tag;
        recent.setTag(tag);
    }

    private boolean isTagSet(RecentContact recent, long tag) {
        return (recent.getTag() & tag) == tag;
    }

	private List<RecentContact> loadedRecentsCustomer;

	private void requestMessagesCustomer() {
		SendQuestion sq = SamService.getInstance().getDao().query_SendQuestion_db_by_question_id(info.getquestion_id());
		final List<String> sp_ids = parseSp_ids(sq.getsp_ids());
		if(sp_ids == null || sp_ids.size() == 0){
			return;
		}
		
		NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
					@Override
					public void onResult(int code, List<RecentContact> recents, Throwable exception) {
						if (code != ResponseCode.RES_SUCCESS || recents == null ) {
							return;
						}
						loadedRecentsCustomer = new ArrayList<RecentContact>();
						for(RecentContact rc:recents){
							if(rc.getSessionType() == SessionTypeEnum.P2P && isTagSet(rc, NimConstants.RECENT_TAG_CUSTOMER_ROLE) && isMine(sp_ids,rc.getContactId())){
									loadedRecentsCustomer.add(rc);
							}
						}

						onRecentContactsCustomerLoaded();
					}
				});
	}

	private void onRecentContactsCustomerLoaded() {
		items_customer.clear();
		if (loadedRecentsCustomer != null) {
			items_customer.addAll(loadedRecentsCustomer);
			loadedRecentsCustomer = null;
		}
		refreshCustomerMessages(true);
	}
	
	private void refreshCustomerMessages(boolean unreadChanged) {
		sortRecentContactsCustomer(items_customer);
		notifyDataSetChangedCustomer();
	}

	private void notifyDataSetChangedCustomer() {
		adapter_customer.notifyDataSetChanged();
	}

	private void sortRecentContactsCustomer(List<RecentContact> list) {
        if (list.size() == 0) {
            return;
        }
        Collections.sort(list, comp_customer);
    }

	private static Comparator<RecentContact> comp_customer = new Comparator<RecentContact>() {
		@Override
		public int compare(RecentContact o1, RecentContact o2) {
			long time1 = 0;
			long time2 = 0;
			if(o1.getSessionType() == SessionTypeEnum.P2P){
				MsgSession s1 = SamService.getInstance().getDao().query_MsgSession_db(o1.getContactId(),ModeEnum.CUSTOMER_MODE.ordinal());
				if(s1!=null){
					time1 = s1.getrecent_msg_time();
				}
			}else{
				time1 = o1.getTime();
			}

			if(o2.getSessionType() == SessionTypeEnum.P2P){
				MsgSession s2 = SamService.getInstance().getDao().query_MsgSession_db(o2.getContactId(),ModeEnum.CUSTOMER_MODE.ordinal());
				if(s2!=null){
					time2 = s2.getrecent_msg_time();
				}
			}else{
				time2 = o2.getTime();
			}

			long time = time1 - time2;
			return time == 0 ? 0 : (time > 0 ? -1 : 1);
		}
	};


	private int findRecentContactIndex(List<RecentContact> recents, IMMessage msg){
		int index = -1;
		for(int i=0; i< recents.size();i++){
			if(msg.getSessionId().equals(recents.get(i).getContactId())){
				index = i;
				break;
			}
		}

		return index;
	}

	protected void refreshViewHolderCustomerByIndex(final int index) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Object tag = ListViewUtil.getViewHolderByIndex(listView_customer, index);
				if (tag instanceof SamchatRecentViewHolder) {
					LogUtil.e("test","getViewHolderByIndex:"+tag);
					SamchatRecentViewHolder viewHolder = (SamchatRecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}else if(tag instanceof RecentViewHolder) {
					RecentViewHolder viewHolder = (RecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}
			}
		});
	}

	

	Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
		@Override
		public void onEvent(IMMessage message){
			if(message.	getDirect() == MsgDirectionEnum.Out){
				Map<String, Object> content = message.getRemoteExtension();
				if(content == null){
					return;
				}
				
				if((Integer)content.get(Constants.MSG_FROM) == Constants.FROM_CUSTOMER){
					int index = findRecentContactIndex(items_customer,message);
					if(index != -1){
						items_customer.get(index).setMsgStatus(message.getStatus());
						refreshViewHolderCustomerByIndex(index);
					}
				}
			}
		}
	};

	private void registerObservers(boolean register) {
		MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
		service.observeMsgStatus(statusObserver, register);
	}

}




