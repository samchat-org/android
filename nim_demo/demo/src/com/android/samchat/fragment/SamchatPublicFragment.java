package com.android.samchat.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.android.samchat.activity.SamchatAdvertisementPostActivity;
import com.android.samchat.activity.SamchatSearchPublicActivity;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.cache.MsgSessionDataCache;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.common.SamchatFileNameUtils;
import com.android.samchat.service.ErrorString;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.HttpCommClient;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.MsgSession;
import com.android.samservice.info.RcvdAdvSession;
import com.android.samservice.utils.S3Util;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.cache.SendIMMessageCache;
import com.netease.nim.uikit.common.fragment.TFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.android.samchat.R;
import android.widget.LinearLayout;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.session.actions.ImageAction;
import com.netease.nim.uikit.session.actions.VideoAction;
import com.netease.nim.uikit.session.constant.RequestCode;
import com.netease.nim.uikit.session.module.Container;
import com.netease.nim.uikit.session.module.ModuleProxy;
import com.netease.nim.uikit.session.module.input.SamchatAdvertisementInputPanel;
import com.netease.nim.uikit.session.module.list.SamchatAdvertisementMessageListPanel;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Main Fragment in SamchatPublicListFragment
 */
public class SamchatPublicFragment extends TFragment implements ModuleProxy{
	public static String TAG="SamchatPublicFragment";
	public static final int CONFIRM_ID_GET_POST_CONENT=2000;

	private LinearLayout fragment_root_layout;
	/*customer mode*/
	//view
	private LinearLayout customer_public_layout;
	private ListView customer_public_list;
	
	//data
	private List<FollowedSamPros> followedSPs;
	private FollowedSPAdapter fspAdapter;
	private boolean followedSPLoaded = false;
	private CustomerPublicCallback cpcallback;
	private List<RcvdAdvSession> rcvdSessions;
	
	/*sp mode*/
	//view
	private RelativeLayout sp_public_layout;
	private View rootView;
	private SessionCustomization customization;
	protected String sessionId;
	protected SessionTypeEnum sessionType;
	protected SamchatAdvertisementInputPanel inputPanel;
	protected SamchatAdvertisementMessageListPanel messageListPanel;
	protected List<BaseAction> actions;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private Map<String,TransferObserver> S3ObserverMap = new ConcurrentHashMap<>();

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
		filter.addAction(Constants.BROADCAST_USER_INFO_UPDATE);
		filter.addAction(Constants.BROADCAST_POST_ADV);

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
				}else if(intent.getAction().equals(Constants.BROADCAST_USER_INFO_UPDATE)){
					ContactUser updateUser = (ContactUser)intent.getSerializableExtra("user");
					userInfoUpdate(updateUser);
				}else if(intent.getAction().equals(Constants.BROADCAST_POST_ADV)){
					//launchAdvertisementPostActivity();
					LogUtil.i(TAG,"receive BROADCAST_POST_ADV");
					showAdvertisementPostBar(true);
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

	private void userInfoUpdate(ContactUser updateUser){
		int index = -1;
		boolean nameUpdate = false;
		
		for(int i=0; i<followedSPs.size(); i++){
			if(updateUser.getunique_id() == followedSPs.get(i).getunique_id()){
				if(!updateUser.getusername().equals(followedSPs.get(i).getusername())){
					followedSPs.get(i).setusername(updateUser.getusername());
					nameUpdate = true;
				}
				followedSPs.get(i).setavatar(updateUser.getavatar());
				followedSPs.get(i).setservice_category(updateUser.getservice_category());
				followedSPs.get(i).setlastupdate(updateUser.getlastupdate());
				index = i;
				break;
			}
		}

		if(index != -1){
			if(nameUpdate){
				refreshFollowedSPsList();
			}else{
				Object tag = ListViewUtil.getViewHolderByIndex(customer_public_list, index);
				FollowedSPAdapter.ViewHolder viewHolder = (FollowedSPAdapter.ViewHolder) tag;
				if(viewHolder != null){
					FollowedSPAdapter.ViewHolder.refreshItem(viewHolder,followedSPs.get(index));
				}
			}
		}
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
		initFollowedSPList();
		LoadRcvdAdvSessions();
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
        notifyDataSetChangedFSP();
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		messageListPanel.onDestroy();
	}
	
	private void findViews() {
		fragment_root_layout = (LinearLayout) findView(R.id.fragment_root);
		//customer mode views
		customer_public_layout = (LinearLayout) findView(R.id.customer_public_layout);
		customer_public_list = (ListView) findView(R.id.customer_public_list);
		//sp mode views
		sp_public_layout = (RelativeLayout) findView(R.id.sp_public_layout);

		if(SamchatGlobal.getmode()== ModeEnum.CUSTOMER_MODE){
			customer_public_layout.setVisibility(View.VISIBLE);
			sp_public_layout.setVisibility(View.GONE);
		}else{
			customer_public_layout.setVisibility(View.GONE);
			sp_public_layout.setVisibility(View.VISIBLE);
		}
    }

	

	private void initFollowedSPList(){
		rcvdSessions = new ArrayList<RcvdAdvSession>();
		followedSPs = new ArrayList<FollowedSamPros>();
		fspAdapter = new FollowedSPAdapter(getActivity(),followedSPs,rcvdSessions);
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
				FollowedSamPros fsp = (FollowedSamPros) parent.getAdapter().getItem(position);
				if(fsp != null){
					showLongClickMenu((FollowedSamPros) parent.getAdapter().getItem(position));
				}
				return true;
			}
		});
	}

	private boolean isMute(long unique_id){
		String public_account = NimConstants.PUBLIC_ACCOUNT_PREFIX+unique_id;
		return !NIMClient.getService(FriendService.class).isNeedMessageNotify(public_account);
	}

	private boolean isBlock(FollowedSamPros fsp){
		return (fsp.getblock_tag()!=Constants.NO_TAG);
	}

	private boolean isSending = false;
	private void showLongClickMenu(final FollowedSamPros fsp) {
		CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
		String title = getString(R.string.samchat_unfollow);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				if (!NetworkUtil.isNetAvailable(getActivity())) {
					Toast.makeText(getActivity(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
					return;
				}
				if(isSending){
					return;
				}
				isSending = true;
				follow(false,fsp);
			}
		});

		title = (isBlock(fsp)?getString(R.string.samchat_unblock):getString(R.string.samchat_block));
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				if (!NetworkUtil.isNetAvailable(getActivity())) {
					Toast.makeText(getActivity(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
					return;
				}
				if(isSending){
					return;
				}
				isSending = true;
				if(isBlock(fsp)){
					//do unblock
					block(false, fsp);
				}else{
					//do block
					block(true, fsp);
				}
			}
		});

		title = (isMute(fsp.getunique_id())?getString(R.string.samchat_unmute):getString(R.string.samchat_mute));
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				if (!NetworkUtil.isNetAvailable(getActivity())) {
					Toast.makeText(getActivity(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(isSending){
					return;
				}
				
				isSending = true;
				
				if(isMute(fsp.getunique_id())){
					//do unmute
					mute(false, fsp);
				}else{
					//do mute
					mute(true, fsp);
				}
			}
		});
		alertDialog.show();
	}

	List<RcvdAdvSession> loadedRcvdSessions;
	private void LoadRcvdAdvSessions(){
		SamDBManager.getInstance().asyncQueryAllRecvAdvSession(new NIMCallback(){
			@Override
			public void onResult(final Object obj1, Object obj2, int code) {
				getHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						loadedRcvdSessions = (List<RcvdAdvSession>)obj1;
						rcvdSessions.clear();
						if(loadedRcvdSessions != null){
							rcvdSessions.addAll(loadedRcvdSessions);
							loadedRcvdSessions = null;
						}
					}
				},0);
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
				
				loadedFollowSPs= FollowDataCache.getInstance().getMyFollowSPsList();
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

		SamDBManager.getInstance().asyncReadTotalUnreadAdvertisementCount(new NIMCallback(){
			@Override
			public void onResult(Object obj1, Object obj2, int code) {
				final int unread = (Integer)obj1;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cpcallback.onUnreadCountChange(unread);
					}
				});
			}
		});
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

	private void updateRcvdAdvSessions(RcvdAdvSession session){
		int index = -1;
		for(int i = 0; i<rcvdSessions.size();i++){
			if(session.getsession() == rcvdSessions.get(i).getsession()){
				index = i;
				break;
			}
		}
		if(index >= 0){
			rcvdSessions.remove(index);
		}
		rcvdSessions.add(session);
	}

	private void removeRcvdAdvSessionByID(long session_id){
		int index = -1;
		for(int i = 0; i<rcvdSessions.size();i++){
			if(session_id == rcvdSessions.get(i).getsession()){
				index = i;
				break;
			}
		}
		if(index >= 0){
			rcvdSessions.remove(index);
		}
	}


/***********************************Observers*****************************************************/
	private void registerObservers(boolean register) {
		SamDBManager.getInstance().registerRcvdAdvSessionObserver(rcvdAdvSessionChangedObserver,  register);
	}

	SamchatObserver< RcvdAdvSession > rcvdAdvSessionChangedObserver = new SamchatObserver <RcvdAdvSession>(){
		@Override
		public void onEvent(final RcvdAdvSession session){
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateRcvdAdvSessions(session);
					LogUtil.i(TAG,"rcvd adv session:"+session.getsession()+" "+session.getname()+" "+session.getrecent_adv_content());
					refreshFollowedSPsList();
				}
			}, 0);
		}
	};


/*************************************Service Provide Mode***************************/
    protected List<BaseAction> getActionList() {
        actions = new ArrayList<>();
        actions.add(new ImageAction());
        actions.add(new VideoAction());
        /*actions.add(new LocationAction());

        if (customization != null && customization.actions != null) {
            actions.addAll(customization.actions);
        }*/
        return actions;
    }

	public boolean onBackPressed() {
		showAdvertisementPostBar(false);
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
		customization = SessionHelper.getSendAdvertisementP2PCustomization();
		Container container = new Container(getActivity(), sessionId, ModeEnum.SP_MODE.getValue(),sessionType, this);
		messageListPanel = new SamchatAdvertisementMessageListPanel(container, rootView, false, false);
		messageListPanel.setOnResendAdvertisementListener(new SamchatAdvertisementMessageListPanel.OnResendAdvertisementListener(){
			@Override
			public void OnResendAdvertisementMsg(IMMessage im){
				resendMessage(im);
			}
		});

		messageListPanel.setOnDeleteMessageListener(new SamchatAdvertisementMessageListPanel.OnDeleteMessageListener(){
			@Override
			public void OnDeleteMessage(IMMessage im){
				releaseTransferObserver(im.getUuid(),true);
			}
		});

		messageListPanel.setOnTouchListener(new MessageListView.OnTouchListener(){
			@Override
			public void OnTouch(){
				
				LogUtil.i(TAG,"dismiss post bar due to message list touch");
				showAdvertisementPostBar(false);
			}
		});

		
		inputPanel = new SamchatAdvertisementInputPanel(container, rootView, getActionList());
		if(customization!=null){
			messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
		}

		showAdvertisementPostBar(false);
	}

	private void launchAdvertisementPostActivity(){
		SamchatAdvertisementPostActivity.startActivityForResult(getActivity(), SamchatPublicFragment.this, CONFIRM_ID_GET_POST_CONENT);
	}

	private void showAdvertisementPostBar(boolean show){
		if(show){
			inputPanel.showBottomLayout(true);
		}else{
			inputPanel.showBottomLayout(false);
		}
	}

	protected boolean isAllowSendMessage(final IMMessage message) {
        return true;
    }

	private void releaseTransferObserver(String uuid, boolean cancel){
		TransferObserver observer = S3ObserverMap.remove(uuid);
		if(observer != null){
			if(cancel){
				S3Util.getTransferUtility(getActivity()).cancel(observer.getId());
			}
			observer.cleanTransferListener();
			S3Util.getTransferUtility(getActivity()).deleteTransferRecord(observer.getId());
		}
	}

	private class UploadListener implements TransferListener {
		private IMMessage im;
		private String s3name_origin;
		private String abspath_origin;
		private TransferObserver observer;

		public UploadListener(IMMessage im,String abspath_origin, String s3name_origin){
			this.im = im;
			this.s3name_origin = s3name_origin;
			this.abspath_origin = abspath_origin;
		}

		// Simply updates the UI list when notified.
		@Override
		public void onError(int id, Exception e) {
			LogUtil.i(TAG,"onStateChanged error");
			e.printStackTrace();
			releaseTransferObserver(im.getUuid(),false);
			im.setStatus(MsgStatusEnum.fail);
			SendIMMessageCache.getInstance().remove(im.getUuid());
			SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(null, im);
			AttachmentStore.deleteIfExist(SamchatFileNameUtils.getTempFileName(abspath_origin, s3name_origin));
		}

		@Override
		public void onProgressChanged(int id, final long bytesCurrent, final long bytesTotal) {
			LogUtil.i(TAG,"onProgressChanged "+bytesCurrent+"/"+bytesTotal);
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					messageListPanel.onAttachmentProgressChange(im, bytesCurrent,  bytesTotal);
				}
			}, 0);
		}

		@Override
		public void onStateChanged(int id, TransferState newState) {
			LogUtil.e(TAG,"onStateChanged "+newState);
			if(newState == TransferState.COMPLETED) {
				releaseTransferObserver(im.getUuid(),false);
				String url_origin = NimConstants.S3_URL_UPLOAD+NimConstants.S3_PATH_ADV+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
				if(im.getMsgType() == MsgTypeEnum.image){
					sendAdvertisement(Constants.ADV_TYPE_PIC, url_origin, null, im);
					AttachmentStore.deleteIfExist(SamchatFileNameUtils.getTempFileName(abspath_origin, s3name_origin));
				}else{
					sendAdvertisement(Constants.ADV_TYPE_VEDIO, url_origin, 
						"http://samchat-test.s3-website-us-west-2.amazonaws.com/advertisement/thumb/thumb_10000000008_1474031723942.jpg", im);
				}
			}else if(newState == TransferState.IN_PROGRESS){
				
			}else{
				releaseTransferObserver(im.getUuid(),false);
				im.setStatus(MsgStatusEnum.fail);
				SendIMMessageCache.getInstance().remove(im.getUuid());
				SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(null, im);
				AttachmentStore.deleteIfExist(SamchatFileNameUtils.getTempFileName(abspath_origin, s3name_origin));
			}
		}
	}

	private void uploadAdvertisementOrigin(IMMessage im,String absPath,String s3nameOrig){
		Bitmap bitmap = null;
		File file = null;
		if(im.getMsgType() == MsgTypeEnum.image){
			bitmap = BitmapDecoder.decodeSampled(absPath, Constants.ADV_PIC_MAX, Constants.ADV_PIC_MAX);
			if(bitmap == null){
				im.setStatus(MsgStatusEnum.fail);
				SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(null, im);
				return;
			}
			String temp = SamchatFileNameUtils.getTempFileName( absPath,  s3nameOrig);
			if(AttachmentStore.saveBitmap(bitmap, temp, true)){
				file = new File(temp);
			}else{
				file = new File(absPath);
			}
		}else{
			file = new File(absPath);
		}

		String key = NimConstants.S3_PATH_ADV+NimConstants.S3_FOLDER_ORIGIN+s3nameOrig;
		TransferObserver observer = S3Util.getTransferUtility(getActivity()).upload(NimConstants.S3_BUCKETNAME,key,file);
		observer.setTransferListener(new UploadListener(im,absPath,s3nameOrig));
		S3ObserverMap.put(im.getUuid(),observer);

	}


	@Override
	public boolean sendMessage(final IMMessage message) {
		if (!isAllowSendMessage(message)) {
			return false;
		}

		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,ModeEnum.CUSTOMER_MODE.getValue());
		message.setRemoteExtension(msg_from);
		message.setDirect(MsgDirectionEnum.Out);
		message.setStatus(MsgStatusEnum.fail);

		FileAttachment attachment = (FileAttachment)message.getAttachment();
		if(attachment != null){
			String s3nameOrig = SamchatFileNameUtils.getS3FileNameOfOrigin(attachment.getPath());
			Map<String, Object> hMap = new HashMap<>();
			hMap.put(NimConstants.S3_ORIG,s3nameOrig);
			message.setLocalExtension(hMap);
			message.setAttachStatus(AttachStatusEnum.transferred);
		}

		NIMClient.getService(MsgService.class).saveMessageToLocal(message, false).setCallback(new RequestCallback<Void>() {
			@Override
			public void onSuccess(Void a) {
				message.setStatus(MsgStatusEnum.sending);
				SamDBManager.getInstance().asyncStoreSendAdvertisementMessage(message, new NIMCallback(){
					@Override
					public void onResult(Object obj1, Object obj2, int code) {
						IMMessage im = (IMMessage)obj1;
						if(code == 0){
							if(im.getAttachment() != null){
								SendIMMessageCache.getInstance().add(im.getUuid());
								String path = ((FileAttachment)im.getAttachment()).getPath();
								String s3nameOrig = (String)im.getLocalExtension().get(NimConstants.S3_ORIG);
								uploadAdvertisementOrigin(im,path,s3nameOrig);
							}else{
								String text = im.getContent();
								Advertisement adv = new Advertisement(Constants.ADV_TYPE_TEXT, text, null ,SamService.getInstance().get_current_user().getunique_id());
								SendIMMessageCache.getInstance().add(im.getUuid());
								sendAdvertisement(adv.gettype(),adv.getcontent(), adv.getcontent_thumb(), im);
							}
						}else{
							NIMClient.getService(MsgService.class).deleteChattingHistory(im);
							Toast.makeText(getActivity(), getString(R.string.samchat_database_error), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}

			@Override
			public void onFailed(int code) {
				Toast.makeText(getActivity(), getString(R.string.samchat_database_error), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onException(Throwable exception) {
				Toast.makeText(getActivity(), getString(R.string.samchat_database_error), Toast.LENGTH_SHORT).show();
			}
		});
        

        return true;
    }

	public boolean resendMessage(IMMessage message) {
		message.setStatus(MsgStatusEnum.sending);
		SendIMMessageCache.getInstance().add(message.getUuid());
		if(messageListPanel.isLastMessage(message)){
			MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(message.getSessionId(), ModeEnum.SP_MODE.getValue());
			session.setrecent_msg_status(message.getStatus().getValue());
		}
		
		FileAttachment attachment = (FileAttachment)message.getAttachment();
		if(attachment != null){
			//send picture advertisment
			Map<String, Object> content = message.getLocalExtension();
			String s3name_orig = (String)content.get(NimConstants.S3_ORIG);
			String path = attachment.getPath();
			uploadAdvertisementOrigin(message,path,s3name_orig);
		}else{
			sendAdvertisement(Constants.ADV_TYPE_TEXT, message.getContent(),null, message);
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
		LogUtil.d(TAG, "onActivityResult called");
		//messageListPanel.onActivityResult(requestCode, resultCode, data);
		/*if(requestCode == CONFIRM_ID_GET_POST_CONENT && resultCode == Activity.RESULT_OK){
			if(data.getBooleanExtra(SamchatAdvertisementPostActivity.EXTRA_IS_TEXT,false)){
				String adv_text = data.getStringExtra(SamchatAdvertisementPostActivity.EXTRA_TEXT);
				inputPanel.onTextMessageSendButtonPressed(adv_text);
			}else{
				inputPanel.onActivityResult(((ImageAction)actions.get(0)).makeRequestCode(RequestCode.PICK_IMAGE), Activity.RESULT_OK,data);
			}
		}*/
	}



    private void sendAdvertisement(int type, String content, String content_thumb, final IMMessage im){
		SamService.getInstance().write_advertisement(type,content, content_thumb,
			SamService.getInstance().get_current_user().getunique_id(),im.getUuid(),new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					HttpCommClient hcc = (HttpCommClient) obj;
					Advertisement adv = hcc.adv;
					im.setStatus(MsgStatusEnum.success);
					SendIMMessageCache.getInstance().remove(im.getUuid());
					SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(adv, im);
				}
				@Override
				public void onFailed(int code) {
					im.setStatus(MsgStatusEnum.fail);
					SendIMMessageCache.getInstance().remove(im.getUuid());
					SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(null, im);
				}
				@Override
				public void onError(int code) {
					im.setStatus(MsgStatusEnum.fail);
					SendIMMessageCache.getInstance().remove(im.getUuid());
					SamDBManager.getInstance().asyncUpdateSendAdvertisementMessage(null, im);
				}
		 });
	}

	private void follow(boolean follow, FollowedSamPros fsp){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(fsp.getunique_id());
		if(user == null){
			user = new ContactUser(Constants.SAM_PROS);
			user.setunique_id(fsp.getunique_id());
			user.setusername(fsp.getusername());
			user.setavatar(fsp.getavatar());
			user.setservice_category(fsp.getservice_category());
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().follow(follow, user ,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							HttpCommClient hcc = (HttpCommClient)obj;
							DialogMaker.dismissProgressDialog();
							isSending = false;
							removeRcvdAdvSessionByID(hcc.userinfo.getunique_id());
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					final ErrorString error = new ErrorString(getActivity(),code);
					
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSending = false;
							EasyAlertDialogHelper.showOneButtonDiolag(getActivity(), null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(getActivity(),code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSending = false;
							EasyAlertDialogHelper.showOneButtonDiolag(getActivity(), null,
                    			error.reminder, getString(R.string.samchat_ok), true, null);
						}
					}, 0);
				}

		} );

	}

	
	private void block(final boolean block,FollowedSamPros fsp){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(fsp.getunique_id());
		if(user == null){
			user = new ContactUser(Constants.SAM_PROS);
			user.setunique_id(fsp.getunique_id());
			user.setusername(fsp.getusername());
			user.setavatar(fsp.getavatar());
			user.setservice_category(fsp.getservice_category());
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().block(block, user ,new SMCallBack(){
				@Override
				public void onSuccess(final Object obj, final int WarningCode) {
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSending = false;
							Toast.makeText(getActivity(), block?R.string.samchat_block_public_succeed:R.string.samchat_unblock_public_succeed, Toast.LENGTH_SHORT).show();
						}
					}, 0);
				}

				@Override
				public void onFailed(int code) {
					final ErrorString error = new ErrorString(getActivity(),code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSending = false;
							Toast.makeText(getActivity(), block?R.string.samchat_block_public_failed:R.string.samchat_unblock_public_failed, Toast.LENGTH_SHORT).show();
						}
					}, 0);
				}

				@Override
				public void onError(int code) {
					final ErrorString error = new ErrorString(getActivity(),code);
					getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							DialogMaker.dismissProgressDialog();
							isSending = false;
							Toast.makeText(getActivity(), block?R.string.samchat_block_public_failed:R.string.samchat_unblock_public_failed, Toast.LENGTH_SHORT).show();
						}
					}, 0);
				}

		} );

	}

	private void mute(final boolean muteState,FollowedSamPros fsp){
		String public_account = NimConstants.PUBLIC_ACCOUNT_PREFIX+fsp.getunique_id();
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		NIMClient.getService(FriendService.class).setMessageNotify(public_account, !muteState).setCallback(new RequestCallback<Void>() {
			@Override
			public void onSuccess(Void param) {
				DialogMaker.dismissProgressDialog();
				isSending = false;
				refreshFollowedSPsList();
				if(muteState){
					Toast.makeText(getActivity(), R.string.samchat_mute_public_succeed, Toast.LENGTH_SHORT).show();
				} else{
					Toast.makeText(getActivity(), R.string.samchat_unmute_public_succeed, Toast.LENGTH_SHORT).show();
				} 
			}

			@Override
			public void onFailed(int code) {
				DialogMaker.dismissProgressDialog();
				isSending = false;
				if(muteState){
					Toast.makeText(getActivity(), R.string.samchat_mute_public_failed, Toast.LENGTH_SHORT).show();
				} else{
					Toast.makeText(getActivity(), R.string.samchat_unmute_public_failed, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onException(Throwable exception) {
				DialogMaker.dismissProgressDialog();
				isSending = false;
				if(muteState){
					Toast.makeText(getActivity(), R.string.samchat_mute_public_failed, Toast.LENGTH_SHORT).show();
				} else{
					Toast.makeText(getActivity(), R.string.samchat_unmute_public_failed, Toast.LENGTH_SHORT).show();
				}      
			}
		});
	}

	/*private void block(boolean blockState,FollowedSamPros fsp){
		String public_account = NimConstants.PUBLIC_ACCOUNT_PREFIX+fsp.getunique_id();
		LogUtil.e("test","blockState:"+blockState+" public_account:"+public_account);
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		if(blockState){
			NIMClient.getService(FriendService.class).addToBlackList(public_account).setCallback(new RequestCallback<Void>() {
				@Override
				public void onSuccess(Void param) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					refreshFollowedSPsList();
					Toast.makeText(getActivity(), R.string.samchat_block_public_succeed, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					Toast.makeText(getActivity(), R.string.samchat_block_public_failed, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onException(Throwable exception) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					Toast.makeText(getActivity(), R.string.samchat_block_public_failed, Toast.LENGTH_SHORT).show();
				}
			});
		}else{
			NIMClient.getService(FriendService.class).removeFromBlackList(public_account).setCallback(new RequestCallback<Void>() {
				@Override
				public void onSuccess(Void param) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					refreshFollowedSPsList();
					Toast.makeText(getActivity(), R.string.samchat_unblock_public_succeed, Toast.LENGTH_SHORT).show();
				}
				@Override
				public void onFailed(int code) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					Toast.makeText(getActivity(), R.string.samchat_unblock_public_failed, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onException(Throwable exception) {
					DialogMaker.dismissProgressDialog();
					isSending = false;
					Toast.makeText(getActivity(), R.string.samchat_unblock_public_failed, Toast.LENGTH_SHORT).show();
				}    
			});
		}
	}*/
}


