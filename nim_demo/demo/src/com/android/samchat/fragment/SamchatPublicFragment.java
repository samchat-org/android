package com.android.samchat.fragment;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.android.samchat.activity.SamchatSearchPublicActivity;
import com.android.samchat.cache.FollowDataCache;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.service.ErrorString;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.HttpCommClient;
import com.android.samservice.SMCallBack;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.Message;
import com.android.samservice.info.MsgSession;
import com.android.samservice.info.RcvdAdvSession;
import com.android.samservice.utils.S3Util;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.fragment.TFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
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
import android.widget.Toast;

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
		filter.addAction(Constants.BROADCAST_USER_INFO_UPDATE);

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
				FollowedSamPros fsp = (FollowedSamPros) parent.getAdapter().getItem(position);
				if(fsp != null){
					showLongClickMenu((FollowedSamPros) parent.getAdapter().getItem(position));
				}
				return true;
			}
		});
	}

	private void showLongClickMenu(final FollowedSamPros fsp) {
		CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity());
		String title = getString(R.string.samchat_unfollow);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				follow(false,fsp);
			}
		});

		title = (fsp.getblock_tag()==Constants.NO_TAG?getString(R.string.samchat_block):getString(R.string.samchat_unblock));
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
                if(fsp.getblock_tag() ==Constants.NO_TAG){
                    block(true,fsp);
                }else{
                    block(false,fsp);
                }
			}
		});

		title = (fsp.getfavourite_tag()==Constants.NO_TAG?getString(R.string.samchat_favourite):getString(R.string.samchat_unfavourite));
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
                if(fsp.getfavourite_tag() ==Constants.NO_TAG){
                    favourite(true,fsp);
                }else{
                    favourite(false,fsp);
                }
			}
		});
		alertDialog.show();
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

	private class UploadListener implements TransferListener {
		private IMMessage im;
		private String s3name_origin;
		private String abspath_origin;
		private TransferObserver observer;
		public UploadListener(IMMessage im,String abspath_origin, String s3name_origin,TransferObserver observer){
			this.im = im;
			this.s3name_origin = s3name_origin;
			this.abspath_origin = abspath_origin;
			this.observer = observer;
		}

		// Simply updates the UI list when notified.
		@Override
		public void onError(int id, Exception e) {
			//observer.cleanTransferListener();			
		}

		@Override
		public void onProgressChanged(int id, final long bytesCurrent, final long bytesTotal) {
			LogUtil.e("test","onProgressChanged "+bytesCurrent+bytesTotal);
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					messageListPanel.onAttachmentProgressChange(im, bytesCurrent,  bytesTotal);
				}
			}, 0);
		}

		@Override
		public void onStateChanged(int id, TransferState newState) {
			LogUtil.e("test","onStateChanged "+newState);
			if(newState == TransferState.COMPLETED) {
				//observer.cleanTransferListener();
				String url_origin = NimConstants.S3_URL+NimConstants.S3_PATH_ADV+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
				sendAdvertisement(Constants.ADV_TYPE_PIC, url_origin, url_origin, im);
				deleteFile(abspath_origin,s3name_origin);
			}else if(newState == TransferState.FAILED){
				//observer.cleanTransferListener();
				im.setStatus(MsgStatusEnum.fail);
				SamDBManager.getInstance().syncUpdateSendAdvertisementMessage(null, im);
				deleteFile(abspath_origin,s3name_origin);
			}
		}
	}

	private void deleteFile(String abspath_origin, String s3name_origin){
		String extension = FileUtil.getExtensionName(abspath_origin);
		String temp = ImageUtil.getTempAdvFilePath(StringUtil.makeMd5(s3name_origin), extension);
		File file = new File(temp);
		file.delete();
	}



    private boolean createTmpFile(String temp, Bitmap bitmap, String extension){
        FileOutputStream fOut = null;
        try{
            File tmpfile = new File(temp);
            fOut = new FileOutputStream(tmpfile);
            if(extension.equals("jpg")){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            }else{
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
            fOut.flush();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            try {
                if(fOut != null)
                    fOut.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	private void uploadAdvertisementOrigin(final IMMessage im, final String abspath_origin, final String s3name_origin){
		Bitmap bitmap = BitmapDecoder.decodeSampled(abspath_origin, Constants.ADV_PIC_MAX, Constants.ADV_PIC_MAX);
		String extension = FileUtil.getExtensionName(abspath_origin);
		String temp = ImageUtil.getTempAdvFilePath(StringUtil.makeMd5(s3name_origin), extension);
		File file = null;
		LogUtil.e("test","upload adv temp file path:"+temp);
		if(createTmpFile(temp,bitmap,extension)){
			file = new File(temp);
		}else{
			file = new File(abspath_origin);
		}
		bitmap.recycle();
		String key = NimConstants.S3_PATH_ADV+NimConstants.S3_FOLDER_ORIGIN+s3name_origin;
		TransferObserver observer = S3Util.getTransferUtility(getActivity()).upload(NimConstants.S3_BUCKETNAME,key,file);
		observer.setTransferListener(new UploadListener(im,abspath_origin,s3name_origin,observer));
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

		ImageAttachment attachment = (ImageAttachment)message.getAttachment();
		if(attachment != null){
			String s3name_orig = "orig_"+SamService.getInstance().get_current_user().getunique_id()+"_"+ TimeUtil.currentTimeMillis()+"."+FileUtil.getExtensionName(attachment.getPath());
			Map<String, Object> save_s3_origin = new HashMap<>();
			save_s3_origin.put(NimConstants.S3_ORIG,s3name_orig);
			message.setLocalExtension(save_s3_origin);
		}

		NIMClient.getService(MsgService.class).saveMessageToLocal(message, false).setCallback(new RequestCallback<Void>() {
			@Override
			public void onSuccess(Void a) {
				//send advertisement
				ImageAttachment attachment = (ImageAttachment)message.getAttachment();
				if(attachment != null){
					final String path = attachment.getPath();
					final String s3name_orig = (String)message.getLocalExtension().get(NimConstants.S3_ORIG);
					SamDBManager.getInstance().asyncStoreSendAdvertisementMessage(message, new NIMCallback(){
						@Override
						public void onResult(Object obj1, Object obj2, int code) {
							IMMessage im = (IMMessage)obj1;
							if(code == 0){
								uploadAdvertisementOrigin(im,path,s3name_orig);
							}else{
								NIMClient.getService(MsgService.class).deleteChattingHistory(im);
								Toast.makeText(getActivity(), getString(R.string.samchat_database_error), Toast.LENGTH_SHORT).show();
							}
						}
					});

					//send picture advertisment
					
				}else{
					//send text advertisment
					String text = message.getContent();
					final Advertisement adv = new Advertisement(Constants.ADV_TYPE_TEXT, text, null ,SamService.getInstance().get_current_user().getunique_id());
					SamDBManager.getInstance().asyncStoreSendAdvertisementMessage(message, new NIMCallback(){
						@Override
						public void onResult(Object obj1, Object obj2, int code) {
							IMMessage im = (IMMessage)obj1;
							if(code == 0){	
								sendAdvertisement(adv.gettype(),adv.getcontent(), adv.getcontent_thumb(), im);
							}else{
								NIMClient.getService(MsgService.class).deleteChattingHistory(im);
								Toast.makeText(getActivity(), getString(R.string.samchat_database_error), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
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

	public boolean resendMessage(final IMMessage message) {
		ImageAttachment attachment = (ImageAttachment)message.getAttachment();
		if(attachment != null){
			//send picture advertisment
			MsgSession session=SamService.getInstance().getDao().query_MsgSession_db(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT,ModeEnum.SP_MODE.ordinal());
			if(session == null){
				return true;
			}

			Message msg = SamService.getInstance().getDao().query_Message_db_by_uuid(session.getmsg_table_name(), message.getUuid());
			if(msg == null || msg.gettype() != NimConstants.MSG_TYPE_SEND_ADV || msg.getdata_id() != 0){
				return true;
			}

			//check orig_url existed in S3 or not  first
			Map<String, Object> content = message.getLocalExtension();
			if(content == null){
				return true;
			}
			
			String s3name_orig = (String)content.get(NimConstants.S3_ORIG);
			String url_origin = NimConstants.S3_URL+NimConstants.S3_PATH_ADV+NimConstants.S3_FOLDER_ORIGIN+s3name_orig;
			String path = attachment.getPath();
			uploadAdvertisementOrigin(message,path,s3name_orig);
		}else{
			MsgSession session=SamService.getInstance().getDao().query_MsgSession_db(NimConstants.SESSION_ACCOUNT_ADVERTISEMENT,ModeEnum.SP_MODE.ordinal());
			if(session != null){
				Message msg = SamService.getInstance().getDao().query_Message_db_by_uuid(session.getmsg_table_name(), message.getUuid());
				if(msg != null && msg.gettype() == NimConstants.MSG_TYPE_SEND_ADV && msg.getdata_id() == 0){
					sendAdvertisement(Constants.ADV_TYPE_TEXT, message.getContent(),null, message);
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

        LogUtil.e("test", "SamchatPublic Fragment  onActivityResult");

        inputPanel.onActivityResult(requestCode, resultCode, data);
        //messageListPanel.onActivityResult(requestCode, resultCode, data);
    }



    private void sendAdvertisement(int type, String content, String content_thumb, final IMMessage im){
		//if(type != Constants.ADV_TYPE_TEXT){
		//	content = "http://samchat.s3-website-us-west-1.amazonaws.com/timg.jpg";
		//	content_thumb = "http://samchat.s3-website-us-west-1.amazonaws.com/timg_thumb.jpg";
		//}
		SamService.getInstance().write_advertisement(type,content, content_thumb,SamService.getInstance().get_current_user().getunique_id(),new SMCallBack(){
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

	private void follow(boolean follow, FollowedSamPros fsp){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(fsp.getunique_id());
		if(user == null){
			return;
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().follow(follow, user ,new SMCallBack(){
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

	
	private void block(boolean block,FollowedSamPros fsp){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(fsp.getunique_id());
		if(user == null){
			return;
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().block(block, user ,new SMCallBack(){
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

	private void favourite(boolean favourite,FollowedSamPros fsp){
		ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(fsp.getunique_id());
		if(user == null){
			return;
		}
		
		DialogMaker.showProgressDialog(getActivity(), null, getString(R.string.samchat_processing), false, null).setCanceledOnTouchOutside(false);
		SamService.getInstance().favourite(favourite, user ,new SMCallBack(){
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


