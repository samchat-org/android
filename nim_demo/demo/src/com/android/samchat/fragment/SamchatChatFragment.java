package com.android.samchat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.cache.MsgSessionDataCache;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.SendIMMessageCache;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.recent.viewholder.CommonRecentViewHolder;
import com.netease.nim.uikit.recent.viewholder.RecentContactAdapter;
import com.netease.nim.uikit.recent.viewholder.RecentViewHolder;
import com.netease.nim.uikit.recent.viewholder.SamchatRecentContactAdapter;
import com.netease.nim.uikit.recent.viewholder.TeamRecentViewHolder;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nim.uikit.uinfo.UserInfoObservable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.team.TeamService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog.onSeparateItemClickListener;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.android.samchat.viewholder.SamchatCommonRecentViewHolder;
import com.android.samchat.R;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.android.samservice.Constants;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.android.samchat.SamchatGlobal;
import com.android.samchat.type.ModeEnum;
import com.netease.nim.uikit.common.util.log.LogUtil;
import java.util.Map;
import java.util.Iterator;
import com.android.samchat.viewholder.SamchatRecentViewHolder;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.netease.nim.demo.main.activity.MainActivity;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.info.MsgSession;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.android.samservice.SamService;
import com.android.samservice.info.Message;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.NIMCallback;
/**
 * Main Fragment in SamchatChatListFragment
 */
public class SamchatChatFragment extends TFragment{
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

	public class SamchatSPAdapterDelegate implements TAdapterDelegate {
		@Override
    	public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
			SessionTypeEnum type = items_sp.get(position).getSessionType();
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

	private static final String TAG="SamchatChatFragment";
	public static final long RECENT_TAG_STICKY = 1;
	public static final long RECENT_TAG_CUSTOMER_ROLE = 2;
	public static final long RECENT_TAG_SP_ROLE = 4;
	public static final long RECENT_TAG_STICKY_CUSTOMER_ROLE = 8;
	public static final long RECENT_TAG_STICKY_SP_ROLE = 16;

	// customer mode 
	// view
	private ListView listView_customer;
	// data
	private List<RecentContact> items_customer;
	private SamchatRecentContactAdapter adapter_customer;
	private RecentContactsCallback callback_customer;
	private boolean msgLoaded = false;

	// sp mode
	// view
	private ListView listView_sp;
	// data
	private List<RecentContact> items_sp;
	private SamchatRecentContactAdapter adapter_sp;
	private RecentContactsCallback callback_sp;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_USER_INFO_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
						listView_customer.setVisibility(View.VISIBLE);
						listView_sp.setVisibility(View.GONE);
					}else{
						listView_customer.setVisibility(View.GONE);
						listView_sp.setVisibility(View.VISIBLE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_USER_INFO_UPDATE)){
					refreshCustomerMessages(false);
					refreshSPMessages(false);
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}


	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	private void sendBroadcastForCustomerItemsUpdata(){
		Intent intent = new Intent();
		intent.setAction(Constants.BROADCAST_CUSTOMER_ITEMS_UPDATE);
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findViews();
		initMessageListCustomer();
		initMessageListSP();

		requestMessages(true);

		registerObservers(true);

		registerBroadcastReceiver();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.samchat_chat_fragment_layout, container, false);
    }

    

    @Override
    public void onDestroy() {
        super.onDestroy();
       //registerObservers(false);
    }

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		registerObservers(false);
        unregisterBroadcastReceiver();
	}

	private void findViews() {
		listView_customer = findView(R.id.listView_customer);
		listView_sp = findView(R.id.listView_sp);
		if(SamchatGlobal.getmode() == ModeEnum.CUSTOMER_MODE){
			listView_customer.setVisibility(View.VISIBLE);
			listView_sp.setVisibility(View.GONE);
		}else{
			listView_customer.setVisibility(View.GONE);
			listView_sp.setVisibility(View.VISIBLE);
		}
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

	private void sortRecentContactsCustomer(List<RecentContact> list) {
        if (list.size() == 0) {
            return;
        }
        Collections.sort(list, comp_customer);
    }

	private static Comparator<RecentContact> comp_customer = new Comparator<RecentContact>() {
		@Override
		public int compare(RecentContact o1, RecentContact o2) {
			long sticky = (o1.getTag() & RECENT_TAG_STICKY_CUSTOMER_ROLE) - (o2.getTag() & RECENT_TAG_STICKY_CUSTOMER_ROLE);
			if (sticky != 0) {
				return sticky > 0 ? -1 : 1;
			} else {
				long time1 = 0;
				long time2 = 0;
				if(o1.getSessionType() == SessionTypeEnum.P2P){
					MsgSession s1 = MsgSessionDataCache.getInstance().getMsgSession(o1.getContactId(), ModeEnum.CUSTOMER_MODE.ordinal());
					if(s1!=null){
						time1 = s1.getrecent_msg_time();
					}
				}else{
					time1 = o1.getTime();
				}

				if(o2.getSessionType() == SessionTypeEnum.P2P){
					MsgSession s2 = MsgSessionDataCache.getInstance().getMsgSession(o2.getContactId(), ModeEnum.CUSTOMER_MODE.ordinal());
					if(s2!=null){
						time2 = s2.getrecent_msg_time();
					}
				}else{
					time2 = o2.getTime();
				}

				long time = time1 - time2;
				return time == 0 ? 0 : (time > 0 ? -1 : 1);
        	}
		}
	};

	

	

	private RecentContact findRecentContact(List<RecentContact> recents, IMMessage msg){
		for(RecentContact rc : recents){
			if(msg.getSessionId().equals(rc.getContactId())){
				return rc;
			}
		}
		return null;
	}

	private RecentContact findRecentContact(List<RecentContact> recents, String sessionId){
		for(RecentContact rc : recents){
			if(rc.getSessionType()==SessionTypeEnum.P2P && sessionId.equals(rc.getContactId())){
				return rc;
			}
		}
		return null;
	}

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

	private RecentContact findTeamRecentContact(List<RecentContact> recents, String teamId){
		for(RecentContact rc : recents){
			if(rc.getSessionType()==SessionTypeEnum.Team && teamId.equals(rc.getContactId())){
				return rc;
			}
		}
		return null;
	}
	
	private void notifyDataSetChangedCustomer() {
		adapter_customer.notifyDataSetChanged();
	}

	private void initMessageListCustomer() {
		//init customer list view
		items_customer = new ArrayList<>();
		adapter_customer = new SamchatRecentContactAdapter(getActivity(), items_customer , new SamchatCustomerAdapterDelegate());
		adapter_customer.setmode(ModeEnum.CUSTOMER_MODE.ordinal());
		adapter_customer.setCallback(callback_customer);
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
		listView_customer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < listView_customer.getHeaderViewsCount()) {
					return false;
				}
				showLongClickMenuCustomer((RecentContact) parent.getAdapter().getItem(position));
				return true;
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

    private void showLongClickMenuCustomer(final RecentContact recent) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
        //alertDialog.setTitle(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));
        String title = getString(R.string.delete_chatting);
        alertDialog.addItem(title, new onSeparateItemClickListener() {
            @Override
            public void onClick() {
                if(recent.getSessionType() == SessionTypeEnum.P2P){
                    if(isTagSet( recent, RECENT_TAG_SP_ROLE)){
                        removeTag(recent,RECENT_TAG_CUSTOMER_ROLE);
                        NIMClient.getService(MsgService.class).updateRecent(recent);
                        SamDBManager.getInstance().syncDeleteMessageSession(recent.getContactId(), ModeEnum.CUSTOMER_MODE.ordinal());
                    }else{
                        SamDBManager.getInstance().syncDeleteMessageSession(recent.getContactId(), ModeEnum.CUSTOMER_MODE.ordinal());
                        NIMClient.getService(MsgService.class).clearChattingHistory(recent.getContactId(), recent.getSessionType());
                        NIMClient.getService(MsgService.class).deleteRecentContact2(recent.	getContactId(),recent.getSessionType());
                    }
                }else{
                    NIMClient.getService(MsgService.class).clearChattingHistory(recent.getContactId(), recent.getSessionType());
                    NIMClient.getService(MsgService.class).deleteRecentContact2(recent.	getContactId(),recent.getSessionType());
                }
            }
        });

        title = (isTagSet(recent, RECENT_TAG_STICKY_CUSTOMER_ROLE) ? getString(R.string.clear_sticky_on_top) : getString(R.string.sticky_on_top));
        alertDialog.addItem(title, new onSeparateItemClickListener() {
            @Override
            public void onClick() {
                if (isTagSet(recent, RECENT_TAG_STICKY_CUSTOMER_ROLE)) {
                    removeTag(recent, RECENT_TAG_STICKY_CUSTOMER_ROLE);
                } else {
                    addTag(recent, RECENT_TAG_STICKY_CUSTOMER_ROLE);
                }
                NIMClient.getService(MsgService.class).updateRecent(recent);

                refreshCustomerMessages(false);
            }
        });
        alertDialog.show();
    }

 

	private List<RecentContact> loadedRecentsCustomer;
	private List<RecentContact> loadedRecentsSP;

	private boolean handleRecentContactForCustomer(List<RecentContact> list, RecentContact rc){
		boolean update =false;
		if(rc.getSessionType() == SessionTypeEnum.P2P){
			if(isTagSet(rc, RECENT_TAG_CUSTOMER_ROLE) ){
				list.add(rc);
			}else{
				MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(rc.getContactId(),ModeEnum.CUSTOMER_MODE.ordinal());
				if(session != null){
					addTag(rc,RECENT_TAG_CUSTOMER_ROLE);
					update = true;
					list.add(rc);
				}
			}
		}else if(rc.getSessionType() == SessionTypeEnum.Team){
			if(isTagSet(rc, RECENT_TAG_CUSTOMER_ROLE) ){
				list.add(rc);
			}else if(!isTagSet(rc,RECENT_TAG_SP_ROLE)){
				Team team = TeamDataCache.getInstance().getTeamById(rc.getContactId());
				String myAccount = DemoCache.getAccount();
				if(team != null && !myAccount.equals(team.getCreator())){
					addTag(rc,RECENT_TAG_CUSTOMER_ROLE);
					update = true;
					list.add(rc);
				}
			}
		}
		return update;
	}

	private boolean handleRecentContactForSP(List<RecentContact> list, RecentContact rc){
		boolean update = false;
		if(rc.getSessionType() == SessionTypeEnum.P2P && !rc.getContactId().equals(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT)){
			if(isTagSet(rc, RECENT_TAG_SP_ROLE)){
				list.add(rc);
			}else{
				MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(rc.getContactId(), ModeEnum.SP_MODE.ordinal());
				if(session != null){
					addTag(rc,RECENT_TAG_SP_ROLE);
					update = true;
					list.add(rc);
				}
			}
		}else if(rc.getSessionType() == SessionTypeEnum.Team){
			if(isTagSet(rc, RECENT_TAG_SP_ROLE) ){
				list.add(rc);
			}else if(!isTagSet(rc,RECENT_TAG_CUSTOMER_ROLE)){
				Team team = TeamDataCache.getInstance().getTeamById(rc.getContactId());
				String myAccount = DemoCache.getAccount();
				if(team != null && myAccount.equals(team.getCreator())){
					addTag(rc,RECENT_TAG_SP_ROLE);
					update = true;
					list.add(rc);
				}
			}
		}

		return update;
	}

	private void requestMessages(boolean delay) {
		if (msgLoaded) {
			return;
		}
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (msgLoaded) {
					return;
				}
				NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
					@Override
					public void onResult(int code, List<RecentContact> recents, Throwable exception) {
						if (code != ResponseCode.RES_SUCCESS || recents == null ) {
							return;
						}
						
						loadedRecentsCustomer = new ArrayList<RecentContact>();
						loadedRecentsSP = new ArrayList<RecentContact>();
						
						for(RecentContact rc:recents){
							boolean updateByCustomer = handleRecentContactForCustomer(loadedRecentsCustomer,rc);
							boolean updateBySP = handleRecentContactForSP(loadedRecentsSP, rc);
							if(updateByCustomer || updateBySP){
								NIMClient.getService(MsgService.class).updateRecent(rc);
							}
						}

						msgLoaded = true;
						if (isAdded()) {
							onRecentContactsCustomerLoaded();
							onRecentContactsSPLoaded();
						}
					}
				});
			}
		}, delay ? 250 : 0);
	}

	private void onRecentContactsCustomerLoaded() {
		if(!msgLoaded){
			return;
		}
		items_customer.clear();
		if (loadedRecentsCustomer != null) {
			items_customer.addAll(loadedRecentsCustomer);
			loadedRecentsCustomer = null;
		}
		refreshCustomerMessages(true);

		if (callback_customer != null) {
			callback_customer.onRecentContactsLoaded();
		}	
	}
	
    private void refreshCustomerMessages(boolean unreadChanged) {
        sortRecentContactsCustomer(items_customer);
        notifyDataSetChangedCustomer();

        if (unreadChanged) {
            SamDBManager.getInstance().asyncReadTotalUnreadCount(ModeEnum.CUSTOMER_MODE.ordinal(), new NIMCallback() {
                @Override
                public void onResult(Object obj1, Object obj2, int code) {
                    final int p2p_unread = (Integer) obj1;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int other_unread = 0;
                            for (RecentContact r : items_customer) {
                                if (r.getSessionType() != SessionTypeEnum.P2P) {
                                    other_unread += r.getUnreadCount();
                                }
                            }

                            callback_customer.onUnreadCountChange(other_unread + p2p_unread);
                        }
                    });
                }
            });
        }

        sendBroadcastForCustomerItemsUpdata();
    }

	protected void refreshViewHolderCustomerByIndex(final int index) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Object tag = ListViewUtil.getViewHolderByIndex(listView_customer, index);
				if (tag instanceof SamchatRecentViewHolder) {
					SamchatRecentViewHolder viewHolder = (SamchatRecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}else if(tag instanceof RecentViewHolder) {
					RecentViewHolder viewHolder = (RecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}
			}
		});
	}



	private void updateCustomerItems(RecentContact rc){
		int index = -1;
		for(int i = 0; i<items_customer.size();i++){
			if(rc.getContactId().equals(items_customer.get(i).getContactId())
				&& rc.getSessionType() == (items_customer.get(i).getSessionType()) ){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			items_customer.remove(index);
		}

		items_customer.add(rc);
	}



    public void setCallbackCustomer(RecentContactsCallback callback) {
        callback_customer = callback;
    }


/**************************service provider function*********************************************************/

	public void setCallbackSP(RecentContactsCallback callback) {
        callback_sp = callback;
    }


	private void initMessageListSP() {
		//init sp list view
		items_sp = new ArrayList<>();
		adapter_sp = new SamchatRecentContactAdapter(getActivity(), items_sp , new SamchatSPAdapterDelegate());
		adapter_sp.setmode(ModeEnum.SP_MODE.ordinal());
		adapter_sp.setCallback(callback_sp);
		listView_sp.setAdapter(adapter_sp);
		listView_sp.setItemsCanFocus(true);
		listView_sp.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (callback_sp != null) {
                    RecentContact recent = (RecentContact) parent.getAdapter().getItem(position);
                    callback_sp.onItemClick(recent);
                }
            }
        });
		listView_sp.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < listView_sp.getHeaderViewsCount()) {
					return false;
				}
				showLongClickMenuSP((RecentContact) parent.getAdapter().getItem(position));
				return true;
			}
		});
		listView_sp.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				adapter_sp.onMutable(scrollState == SCROLL_STATE_FLING);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

    private void showLongClickMenuSP(final RecentContact recent) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
        //alertDialog.setTitle(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));
        String title = getString(R.string.delete_chatting);
         alertDialog.addItem(title, new onSeparateItemClickListener() {
            @Override
            public void onClick() {
                if(recent.getSessionType() == SessionTypeEnum.P2P){
                    if(isTagSet( recent, RECENT_TAG_CUSTOMER_ROLE)){
                        removeTag(recent,RECENT_TAG_SP_ROLE);
                        NIMClient.getService(MsgService.class).updateRecent(recent);
                        SamDBManager.getInstance().asyncDeleteMessageSession(recent.getContactId(), ModeEnum.SP_MODE.ordinal());
                        items_sp.remove(recent);
                        refreshSPMessages(true);
                    }else{
                        NIMClient.getService(MsgService.class).clearChattingHistory(recent.getContactId(), recent.getSessionType());
                        NIMClient.getService(MsgService.class).deleteRecentContact(recent);
                    }
                }else{
                    NIMClient.getService(MsgService.class).clearChattingHistory(recent.getContactId(), recent.getSessionType());
                    NIMClient.getService(MsgService.class).deleteRecentContact(recent);
                }
                items_sp.remove(recent);
                refreshSPMessages(true);
            }
        });

        title = (isTagSet(recent, RECENT_TAG_STICKY_SP_ROLE) ? getString(R.string.clear_sticky_on_top) : getString(R.string.sticky_on_top));
        alertDialog.addItem(title, new onSeparateItemClickListener() {
            @Override
            public void onClick() {
                if (isTagSet(recent, RECENT_TAG_STICKY_SP_ROLE)) {
                    removeTag(recent, RECENT_TAG_STICKY_SP_ROLE);
                } else {
                    addTag(recent, RECENT_TAG_STICKY_SP_ROLE);
                }
                NIMClient.getService(MsgService.class).updateRecent(recent);

                refreshSPMessages(false);
            }
        });
        alertDialog.show();
    }

	private void onRecentContactsSPLoaded() {
		if(!msgLoaded){
			return;
		}
		
		items_sp.clear();
		if (loadedRecentsSP != null) {
			items_sp.addAll(loadedRecentsSP);
			loadedRecentsSP = null;
		}
		refreshSPMessages(true);

		if (callback_sp != null) {
			callback_sp.onRecentContactsLoaded();
		}	

	}
	
    private void refreshSPMessages(boolean unreadChanged) {
        sortRecentContactsSP(items_sp);
        notifyDataSetChangedSP();

		 if (unreadChanged) {
             SamDBManager.getInstance().asyncReadTotalUnreadCount(ModeEnum.SP_MODE.ordinal(),new NIMCallback(){
                 @Override
                 public void onResult(Object obj1, Object obj2, int code) {
                     final int p2p_unread = (Integer)obj1;
                     getActivity().runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             int other_unread = 0;
                             for (RecentContact r : items_sp) {
                                 if(r.getSessionType() != SessionTypeEnum.P2P){
                                     other_unread += r.getUnreadCount();
                                 }
                             }

                             callback_sp.onUnreadCountChange(other_unread + p2p_unread);
                         }
                     });
                 }
             });
         }
    }

	private void notifyDataSetChangedSP() {
		adapter_sp.notifyDataSetChanged();
	}

	protected void refreshViewHolderSPByIndex(final int index) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Object tag = ListViewUtil.getViewHolderByIndex(listView_sp, index);
				if (tag instanceof SamchatRecentViewHolder) {
					SamchatRecentViewHolder viewHolder = (SamchatRecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}else if(tag instanceof RecentViewHolder) {
					RecentViewHolder viewHolder = (RecentViewHolder) tag;
					viewHolder.refreshCurrentItem();
				}
			}
		});
	}



	private void updateSPItems(RecentContact rc){
		int index = -1;
		for(int i = 0; i<items_sp.size();i++){
			if(rc.getContactId().equals(items_sp.get(i).getContactId())
				&& rc.getSessionType() == (items_sp.get(i).getSessionType()) ){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			items_sp.remove(index);
		}

		items_sp.add(rc);
	}


	private void sortRecentContactsSP(List<RecentContact> list) {
        if (list.size() == 0) {
            return;
        }
        Collections.sort(list, comp_sp);
    }

	private static Comparator<RecentContact> comp_sp = new Comparator<RecentContact>() {
		@Override
		public int compare(RecentContact o1, RecentContact o2) {
			long sticky = (o1.getTag() & RECENT_TAG_STICKY_SP_ROLE) - (o2.getTag() & RECENT_TAG_STICKY_SP_ROLE);
			if (sticky != 0) {
				return sticky > 0 ? -1 : 1;
			} else {
				long time1 = 0;
				long time2 = 0;
				if(o1.getSessionType() == SessionTypeEnum.P2P){
					MsgSession s1 = MsgSessionDataCache.getInstance().getMsgSession(o1.getContactId(), ModeEnum.SP_MODE.ordinal());
					if(s1!=null){
						time1 = s1.getrecent_msg_time();
					}
				}else{
					time1 = o1.getTime();
				}

				if(o2.getSessionType() == SessionTypeEnum.P2P){
					MsgSession s2 = MsgSessionDataCache.getInstance().getMsgSession(o2.getContactId(), ModeEnum.SP_MODE.ordinal());
					if(s2!=null){
						time2 = s2.getrecent_msg_time();
					}
				}else{
					time2 = o2.getTime();
				}

				long time = time1 - time2;
				return time == 0 ? 0 : (time > 0 ? -1 : 1);
        	}
		}
	};

/***********************************Observers*****************************************************/
	private void registerObservers(boolean register) {
		MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
		service.observeRecentContact(messageObserver, register);
		service.observeMsgStatus(statusObserver, register);
		service.observeRecentContactDeleted(deleteObserver, register);
		registerTeamUpdateObserver(register);

		SamDBManager.getInstance().registerMsgSessionObserver(msgSessionChangedObserver,  register);

		//registerTeamMemberUpdateObserver(register);
	}

	Observer<RecentContact> deleteObserver = new Observer<RecentContact>() {
	@Override
		public void onEvent(RecentContact recentContact) {
			LogUtil.i(TAG,"deleteObserver:"+recentContact.getContactId());
			boolean refresh_customer = false;
			boolean refresh_sp = false;
			if (recentContact != null) {
				for (RecentContact item : items_customer) {
					if (TextUtils.equals(item.getContactId(), recentContact.getContactId())
							&& item.getSessionType() == recentContact.getSessionType()) {
						items_customer.remove(item);
						refresh_customer = true;
						break;
					}
				}

				for (RecentContact item : items_sp) {
					if (TextUtils.equals(item.getContactId(), recentContact.getContactId())
							&& item.getSessionType() == recentContact.getSessionType()) {
						items_sp.remove(item);
						refresh_sp = true;
						break;
					}
				}
			} else {
				items_customer.clear();
				items_sp.clear();
				refresh_customer = true;
				refresh_sp = true;
			}

			if(refresh_customer)
				refreshCustomerMessages(true);
			if(refresh_sp)
				refreshSPMessages(true);
		}
	};

	SamchatObserver < MsgSession > msgSessionChangedObserver = new SamchatObserver < MsgSession >(){
		@Override
		public void onEvent(MsgSession session){
			LogUtil.i(TAG,"msgSessionChangedObserver:"+session.getsession_id());
			List<RecentContact> recents = NIMClient.getService(MsgService.class).queryRecentContactsBlock();	
			final RecentContact rc = findRecentContact(recents,session.getsession_id());
			if(rc == null){
				LogUtil.e(TAG,"find recent contact:" +session.getsession_id()+" not found");
				return;
			}

			if(session.getmode() == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
				if(!isTagSet(rc, RECENT_TAG_CUSTOMER_ROLE)){
					addTag(rc,RECENT_TAG_CUSTOMER_ROLE);
					NIMClient.getService(MsgService.class).updateRecent(rc);
				}

				getActivity().runOnUiThread(new Runnable() {
            		@Override
            		public void run() {
                		updateCustomerItems(rc);
						refreshCustomerMessages(true);
            		}
        		});
			}else{
				if(!isTagSet(rc, RECENT_TAG_SP_ROLE)){
					addTag(rc,RECENT_TAG_SP_ROLE);
					NIMClient.getService(MsgService.class).updateRecent(rc);
				}

				getActivity().runOnUiThread(new Runnable() {
            		@Override
            		public void run() {
                		updateSPItems(rc);
						refreshSPMessages(true);
            		}
        		});
			}
		}
	};

	Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
		@Override
		public void onEvent(IMMessage message){
			if(message.	getDirect() == MsgDirectionEnum.Out){
				Map<String, Object> content = message.getRemoteExtension();
				if(content == null){
					int index = findRecentContactIndex(items_customer,message);
					if(index != -1){
						items_customer.get(index).setMsgStatus(message.getStatus());
						refreshViewHolderCustomerByIndex(index);
						return;
					}
					index = findRecentContactIndex(items_sp,message);
					if(index != -1){
						items_sp.get(index).setMsgStatus(message.getStatus());
						refreshViewHolderSPByIndex(index);
						return;
					}
					return;
				}
				
				if((Integer)content.get(Constants.MSG_FROM) == Constants.FROM_CUSTOMER){
					int index = findRecentContactIndex(items_customer,message);
					if(index != -1){
						if(message.getSessionType() == SessionTypeEnum.P2P){
							MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(message.getSessionId(), ModeEnum.CUSTOMER_MODE.ordinal());
							if(session != null){
								session.setrecent_msg_status(message.getStatus().getValue());
								if(message.getStatus() == MsgStatusEnum.success){
									SamDBManager.getInstance().asyncUpdateMessageSessionDBRecentMessageStatus(message.getSessionId(), ModeEnum.CUSTOMER_MODE.ordinal(),message.getStatus().getValue());
								}
							}
							SendIMMessageCache.getInstance().remove(message.getUuid());
							sendBroadcastForCustomerItemsUpdata();
						}else{
							items_customer.get(index).setMsgStatus(message.getStatus());
						}
						refreshViewHolderCustomerByIndex(index);
					}
				}else if((Integer)content.get(Constants.MSG_FROM) == Constants.FROM_SP){
					int index = findRecentContactIndex(items_sp,message);
					if(index != -1){
						if(message.getSessionType() == SessionTypeEnum.P2P){
							MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(message.getSessionId(), ModeEnum.SP_MODE.ordinal());
							if(session != null){
								session.setrecent_msg_status(message.getStatus().getValue());
								if(message.getStatus() == MsgStatusEnum.success){
									SamDBManager.getInstance().asyncUpdateMessageSessionDBRecentMessageStatus(message.getSessionId(), ModeEnum.SP_MODE.ordinal(),message.getStatus().getValue());
								}
							}
							SendIMMessageCache.getInstance().remove(message.getUuid());
						}else{
							items_sp.get(index).setMsgStatus(message.getStatus());
						}
						refreshViewHolderSPByIndex(index);
					}
				}
			}
		}
	};

	Observer<List<RecentContact>> messageObserver = new Observer<List<RecentContact>>() {
		@Override
		public void onEvent(List<RecentContact> messages) {
			boolean refreshCustomer = false;
			boolean refreshSP = false;

			for (RecentContact msg : messages){
				LogUtil.i(TAG,"messageObserver:"+msg.getContactId());
				if(msg.getSessionType() == SessionTypeEnum.P2P && !msg.getContactId().equals(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT)){
					if(isTagSet(msg, RECENT_TAG_CUSTOMER_ROLE)){
						updateCustomerItems(msg);
						refreshCustomer = true;
					}else{
						MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(msg.getContactId(), ModeEnum.CUSTOMER_MODE.ordinal());
						if(session != null){
							addTag(msg,RECENT_TAG_CUSTOMER_ROLE);
							NIMClient.getService(MsgService.class).updateRecent(msg);
							updateCustomerItems(msg);
							refreshCustomer = true;
						}
					}

					if(isTagSet(msg, RECENT_TAG_SP_ROLE)){
						updateSPItems(msg);
						refreshSP = true;
					}else{
						MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(msg.getContactId(), ModeEnum.SP_MODE.ordinal());
						if(session != null){
							addTag(msg,RECENT_TAG_SP_ROLE);
							NIMClient.getService(MsgService.class).updateRecent(msg);
							updateSPItems(msg);
							refreshSP = true;
						}
					}
				}else if(msg.getSessionType() == SessionTypeEnum.Team){
					if(isTagSet(msg, RECENT_TAG_CUSTOMER_ROLE) ){
						updateCustomerItems(msg);
						refreshCustomer = true;
					}else if(isTagSet(msg, RECENT_TAG_SP_ROLE)){
						updateSPItems(msg);
						refreshSP = true;
					}else{
						Team team = TeamDataCache.getInstance().getTeamById(msg.getContactId());
						String myAccount = DemoCache.getAccount();
						if(team != null){
							if(myAccount.equals(team.getCreator())){
								addTag(msg,RECENT_TAG_SP_ROLE);
								NIMClient.getService(MsgService.class).updateRecent(msg);
								updateSPItems(msg);
								refreshSP = true;
							}else{
								addTag(msg,RECENT_TAG_CUSTOMER_ROLE);
								NIMClient.getService(MsgService.class).updateRecent(msg);
								updateCustomerItems(msg);
								refreshCustomer = true;
							}
						}
					}
				}
			}

			if(refreshCustomer){
				refreshCustomerMessages(true);
			}

			if(refreshSP){
				refreshSPMessages(true);
			}

		}
	};

	private void registerTeamUpdateObserver(boolean register) {
		if (register) {
			TeamDataCache.getInstance().registerTeamDataChangedObserver(teamDataChangedObserver);
		} else {
			TeamDataCache.getInstance().unregisterTeamDataChangedObserver(teamDataChangedObserver);
		}
	}

	TeamDataCache.TeamDataChangedObserver teamDataChangedObserver = new TeamDataCache.TeamDataChangedObserver() {
		@Override
		public void onUpdateTeams(List<Team> teams) {
			boolean refreshCustomer = false;
			boolean refreshSP = false;
			for(Team team:teams){
				RecentContact rc = findTeamRecentContact(items_customer, team.getId());
				if(rc != null){
					refreshCustomer = true;
					break;
				}
			}

			for(Team team:teams){
				RecentContact rc = findTeamRecentContact(items_sp, team.getId());
				if(rc != null){
					refreshSP = true;
					break;
				}
			}

			if(refreshCustomer){
				notifyDataSetChangedCustomer();
			}
			if(refreshSP){
				notifyDataSetChangedSP();
			}
			
		}

		@Override
		public void onRemoveTeam(Team team) {

		}
	};

}

