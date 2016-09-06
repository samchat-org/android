package com.android.samchat.fragment;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.activity.SamchatSearchPublicActivity;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.HttpCommClient;
import com.android.samservice.SMCallBack;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.Message;
import com.android.samservice.info.MsgSession;
import com.android.samservice.info.RcvdAdvSession;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.netease.nim.demo.R;
import android.widget.LinearLayout;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.session.actions.ImageAction;
import com.netease.nim.uikit.session.actions.LocationAction;
import com.netease.nim.uikit.session.actions.VideoAction;
import com.netease.nim.uikit.session.module.Container;
import com.netease.nim.uikit.session.module.ModuleProxy;
import com.netease.nim.uikit.session.module.input.SamchatAdvertisementInputPanel;
import com.netease.nim.uikit.session.module.list.SamchatAdvertisementMessageListPanel;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import android.view.View.OnClickListener;

/**
 * Main Fragment in SamchatPublicListFragment
 */
public class SamchatPublicFragment extends TFragment implements ModuleProxy {
	/*customer mode*/
	//view
	private LinearLayout customer_public_layout;
	private TextView search_textview;
	private ListView customer_public_list;
	
	//data
	private List<FollowedSamPros> followedSPs;
	private FollowedSPAdapter fspAdapter;
	private boolean followedSPLoaded = false;
	private CustomerPublicCallback cpcallback;
	
	/*sp mode*/
	//view
	private LinearLayout sp_public_layout;
	private View rootView;
	private SessionCustomization customization;
	protected String sessionId;
	protected SessionTypeEnum sessionType;
	protected SamchatAdvertisementInputPanel inputPanel;
	protected SamchatAdvertisementMessageListPanel messageListPanel;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void switchMode(ModeEnum to){
		if(to == ModeEnum.SP_MODE){
			customer_public_layout.setVisibility(View.GONE);
			sp_public_layout.setVisibility(View.VISIBLE);
		}else{
			customer_public_layout.setVisibility(View.VISIBLE);
			sp_public_layout.setVisibility(View.GONE);
		}
	}

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
						switchMode(ModeEnum.CUSTOMER_MODE);
					}else{
						switchMode(ModeEnum.SP_MODE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_FOLLOWEDSP_UPDATE)){
					loadedFollowSPs= FollowDataCache.getInstance().getMyFollowSPsList();
					onFollowedSPsLoaded();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	public SamchatPublicFragment(){
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView =  inflater.inflate(R.layout.samchat_public_fragment_layout, container, false);
		return rootView;
	}

	@Override
	public void onDestroyView(){
		unregisterBroadcastReceiver();
		registerObservers(false);
		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findViews();
		setupSearchClick();
		initFollowedSPList();
		LoadFollowedSPs(true);

		spLayoutInit();
		
		registerBroadcastReceiver();
		registerObservers(true);
	}

	@Override
    public void onPause() {
        super.onPause();
        inputPanel.onPause();
        messageListPanel.onPause();
    }

	@Override
    public void onResume() {
        super.onResume();
        messageListPanel.onResume();
        getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL); //play audio by ringtone
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		messageListPanel.onDestroy();
	}
	
	private void findViews() {
		//customer mode views
		customer_public_layout = (LinearLayout) findView(R.id.customer_public_layout);
		search_textview = (TextView) findView(R.id.search);
		customer_public_list = (ListView) findView(R.id.customer_public_list);
		//sp mode views
		sp_public_layout = (LinearLayout) findView(R.id.sp_public_layout);

		if(SamchatGlobal.getmode()== ModeEnum.CUSTOMER_MODE){
			customer_public_layout.setVisibility(View.VISIBLE);
			sp_public_layout.setVisibility(View.GONE);
		}else{
			customer_public_layout.setVisibility(View.GONE);
			sp_public_layout.setVisibility(View.VISIBLE);
		}
    }

	private void setupSearchClick(){
		LogUtil.e("test","setupSearchClick");
		search_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LogUtil.e("test","click called...");
				SamchatSearchPublicActivity.start(getActivity());
			}
		});
	}

	private void initFollowedSPList(){
		followedSPs = new ArrayList<FollowedSamPros>();
		fspAdapter = new FollowedSPAdapter(getActivity(),followedSPs);
		customer_public_list.setAdapter(fspAdapter);
		customer_public_list.setItemsCanFocus(true);
		customer_public_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (cpcallback != null) {
					FollowedSamPros fsp = (FollowedSamPros) parent.getAdapter().getItem(position);
					if(fsp != null){
						cpcallback.onItemClick(fsp);
					}
				}
			}
		});

		customer_public_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
		
	}

	private List<FollowedSamPros> loadedFollowSPs;
	private void LoadFollowedSPs(boolean delay){
		if(followedSPLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(followedSPLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}
				
				loadedFollowSPs= SamService.getInstance().getDao().query_FollowList_db_All();
				followedSPLoaded = true;
				if(isAdded()){
					onFollowedSPsLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onFollowedSPsLoaded(){
		followedSPs.clear();
		if(loadedFollowSPs!=null){
			followedSPs.addAll(loadedFollowSPs);
			loadedFollowSPs = null;
		}

		refreshFollowedSPsList();
	}

	private void notifyDataSetChangedFSP() {
		fspAdapter.notifyDataSetChanged();
	}

	private void refreshFollowedSPsList(){
		sortFollowedSPs(followedSPs);
		notifyDataSetChangedFSP();
	}

	public void setCpCallback(CustomerPublicCallback callback) {
		cpcallback = callback;
	}

	private void sortFollowedSPs(List<FollowedSamPros> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, fspcomp);
	}

	private static Comparator<FollowedSamPros> fspcomp = new Comparator<FollowedSamPros>() {
		@Override
		public int compare(FollowedSamPros o1, FollowedSamPros o2) {
			return TextComparator.compare(o1.getusername(),o2.getusername());
		}
	};


/***********************************Observers*****************************************************/
	private void registerObservers(boolean register) {
		SamDBManager.getInstance().registerRcvdAdvSessionObserver(rcvdAdvSessionChangedObserver,  register);
	}

	SamchatObserver< RcvdAdvSession > rcvdAdvSessionChangedObserver = new SamchatObserver <RcvdAdvSession>(){
		@Override
		public void onEvent(RcvdAdvSession session){
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					refreshFollowedSPsList();
				}
			}, 0);
		}
	};


/*************************************Service Provide Mode***************************/
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        actions.add(new ImageAction());
        actions.add(new VideoAction());
        actions.add(new LocationAction());

        if (customization != null && customization.actions != null) {
            actions.addAll(customization.actions);
        }
        return actions;
    }

	public boolean onBackPressed() {
        if (inputPanel.collapse(true)) {
            return true;
        }

        if (messageListPanel.onBackPressed()) {
            return true;
        }
        return false;
    }

	public void refreshMessageList() {
		messageListPanel.refreshMessageList();
	}

	private void spLayoutInit(){
		sessionId = NimConstants.SESSION_ACCOUNT_ADVERTISEMENT;
		sessionType = SessionTypeEnum.P2P;
		customization = SessionHelper.getAdvCustomization();
		Container container = new Container(getActivity(), sessionId, ModeEnum.SP_MODE.ordinal(),sessionType, this);
		messageListPanel = new SamchatAdvertisementMessageListPanel(container, rootView, false, false);
		messageListPanel.setOnResendAdvertisementListener(new SamchatAdvertisementMessageListPanel.OnResendAdvertisementListener(){
			@Override
			public void OnResendAdvertisementMsg(IMMessage im){
				resendMessage(im);
			}
		});

		
		inputPanel = new SamchatAdvertisementInputPanel(container, rootView, getActionList());
		if(customization!=null){
			messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
		}
	}

	protected boolean isAllowSendMessage(final IMMessage message) {
        return true;
    }

	@Override
	public boolean sendMessage(final IMMessage message) {
		if (!isAllowSendMessage(message)) {
			return false;
		}

		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(ModeEnum.CUSTOMER_MODE.ordinal()));
		message.setRemoteExtension(msg_from);

		message.setDirect(MsgDirectionEnum.Out);
		message.setStatus(MsgStatusEnum.sending);

		NIMClient.getService(MsgService.class).saveMessageToLocal(message, false).setCallback(new RequestCallback<Void>() {
			@Override
			public void onSuccess(Void a) {
				//send advertisement
				ImageAttachment attachment = (ImageAttachment)message.getAttachment();
				if(attachment != null){
					//send picture advertisment
				}else{
					//send text advertisment
					String text = message.getContent();
					Advertisement adv = new Advertisement(Constants.ADV_TYPE_TEXT, text, SamService.getInstance().get_current_user().getunique_id());
					SamDBManager.getInstance().asyncStoreSendAdvertisementMessage(adv,message, new NIMCallback(){
						@Override
						public void onResult(Object obj1, Object obj2, int code) {
							if(code == 0){
								Advertisement adv = (Advertisement)obj1;
								IMMessage im = (IMMessage)obj2;
								sendAdvertisement(adv.gettype(),adv.getcontent(), im);
							}
						}
					});
				}
			}

			@Override
			public void onFailed(int code) {

			}

			@Override
			public void onException(Throwable exception) {

			}
		});
        

        return true;
    }

	public boolean resendMessage(final IMMessage message) {
		ImageAttachment attachment = (ImageAttachment)message.getAttachment();
		if(attachment != null){
			//send picture advertisment
		}else{
			MsgSession session=SamService.getInstance().getDao().query_MsgSession_db(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT,ModeEnum.SP_MODE.ordinal());
			if(session != null){
				Message msg = SamService.getInstance().getDao().query_Message_db_by_uuid(session.getmsg_table_name(), message.getUuid());
				if(msg != null && msg.gettype() == NimConstants.MSG_TYPE_SEND_ADV && msg.getdata_id() == 0){
					sendAdvertisement(Constants.ADV_TYPE_TEXT, message.getContent(), message);
				}
			}
		}
		return true;
    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        inputPanel.onActivityResult(requestCode, resultCode, data);
        messageListPanel.onActivityResult(requestCode, resultCode, data);
    }



    private void sendAdvertisement(int type, String content, final IMMessage im){
		SamService.getInstance().write_advertisement(type, content, SamService.getInstance().get_current_user().getunique_id(),new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					HttpCommClient hcc = (HttpCommClient) obj;
					Advertisement adv = hcc.adv;
					im.setStatus(MsgStatusEnum.success);
					SamDBManager.getInstance().syncUpdateSendAdvertisementMessage(adv, im);
					LogUtil.e("test", "sendAdvertisement succeed");
				}
				@Override
				public void onFailed(int code) {
					im.setStatus(MsgStatusEnum.fail);
					SamDBManager.getInstance().syncUpdateSendAdvertisementMessage(null, im);
					LogUtil.e("test", "sendAdvertisement failed");
				}
				@Override
				public void onError(int code) {
					im.setStatus(MsgStatusEnum.fail);
					SamDBManager.getInstance().syncUpdateSendAdvertisementMessage(null, im);
					LogUtil.e("test", "sendAdvertisement failed");
				}
		 });
	}

}


