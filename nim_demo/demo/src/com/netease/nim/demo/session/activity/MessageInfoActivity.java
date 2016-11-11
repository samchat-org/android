package com.netease.nim.demo.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.activity.SamchatContactUserNameCardActivity;
import com.android.samchat.activity.SamchatContactUserSPNameCardActivity;
import com.android.samchat.activity.SamchatMemberSelectActivity;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.service.SamDBManager;
import com.android.samservice.Constants;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.contact.activity.UserProfileActivity;
import com.netease.nim.demo.team.TeamCreateHelper;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

import java.util.ArrayList;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

/**
 * Created by hzxuwen on 2015/10/13.
 */
public class MessageInfoActivity extends UI {
	private final static String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
	private final static String EXTRA_MODE = "EXTRA_MODE";
		
	private static final int REQUEST_CODE_NORMAL = 1;
	// data
	private String account;
    // view
	private SwitchButton switchButtonMute;
	private LinearLayout create_layout;

	private SwitchButton switchButtonBlock;
	private RelativeLayout delete_layout;

    private int mode;

    public static void startActivity(Context context, String account, int mode) {
        Intent intent = new Intent();
        intent.setClass(context, MessageInfoActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.putExtra(EXTRA_MODE,mode);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, MessageInfoActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samchat_message_info_activity);

        account = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mode = getIntent().getIntExtra(EXTRA_MODE, ModeEnum.CUSTOMER_MODE.getValue());

        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.samchat_chat_options;
        if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
            options.navigateId = R.drawable.samchat_arrow_left;
        }else{
             options.navigateId = R.drawable.samchat_arrow_left_sp;
        }
        setToolBar(R.id.toolbar, options);
        if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
			getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
			getToolBar().setTitleTextColor(getResources().getColor(R.color.samchat_color_dark_blue));
		}else{
			getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
			getToolBar().setTitleTextColor(getResources().getColor(R.color.samchat_color_white));
		}
       findViews();
    }

	@Override
	protected void onResume() {
		super.onResume();
		updateMuteSwitchBtn();
		updateBlockSwitchBtn();
	}

    private void findViews() {
        create_layout = (LinearLayout) findViewById(R.id.create_layout);
        if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
            getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
            create_layout.setVisibility(View.GONE);
        }else{
            getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
            create_layout.setVisibility(View.VISIBLE);
        }
			
        HeadImageView userHead = (HeadImageView) findViewById(R.id.user_layout).findViewById(R.id.imageViewHeader);
        TextView userName = (TextView) findViewById(R.id.user_layout).findViewById(R.id.textViewName);
        userHead.loadBuddyAvatar(account);
        userName.setText(NimUserInfoCache.getInstance().getUserDisplayName(account));
        userHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	ContactUser user = SamchatUserInfoCache.getInstance().getUserByAccount(account);
				if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
					if(user != null)
						SamchatContactUserSPNameCardActivity.start(MessageInfoActivity.this,user);
				}else{
					if(user != null)
						SamchatContactUserNameCardActivity.start(MessageInfoActivity.this, user, false);
				}
			}
		});

        ((TextView)findViewById(R.id.create_team_layout).findViewById(R.id.textViewName)).setText(R.string.samchat_create_group_chat);
        HeadImageView addImage = (HeadImageView) findViewById(R.id.create_team_layout).findViewById(R.id.imageViewHeader);
        addImage.setBackgroundResource(com.netease.nim.uikit.R.drawable.nim_team_member_add_selector);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTeamMsg();
            }
        });

        ((TextView)findViewById(R.id.mute_layout).findViewById(R.id.mute_title)).setText(R.string.samchat_mute_chat);
        switchButtonMute = (SwitchButton) findViewById(R.id.mute_layout).findViewById(R.id.mute_toggle);
        switchButtonMute.setOnChangedListener(onMuteChangedListener);

        ((TextView)findViewById(R.id.block_layout).findViewById(R.id.block_title)).setText(R.string.samchat_block_chat);
        switchButtonBlock = (SwitchButton) findViewById(R.id.block_layout).findViewById(R.id.block_toggle);
        switchButtonBlock.setOnChangedListener(onBlockChangedListener);

		 ((TextView)findViewById(R.id.delete_layout).findViewById(R.id.delete_title)).setText(R.string.samchat_delete_chat);
		 delete_layout = (RelativeLayout)findViewById(R.id.delete_layout);
		 delete_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearChatHistory();
            }
        });
    }

    private void sendBroadcastForBlockMuteUpdata(){
		Intent intent = new Intent();
		intent.setAction(Constants.BROADCAST_CHAT_BLOCK_MUTE_UPDATE);
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DemoCache.getContext());
		manager.sendBroadcast(intent);
	}

    private void updateMuteSwitchBtn() {
        boolean mute = !NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
        switchButtonMute.setCheck(mute);
    }

    private SwitchButton.OnChangedListener onMuteChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean muteState) {
            if (!NetworkUtil.isNetAvailable(MessageInfoActivity.this)) {
                Toast.makeText(MessageInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                switchButtonMute.setCheck(!muteState);
                return;
            }

            NIMClient.getService(FriendService.class).setMessageNotify(account, !muteState).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    if (muteState) {
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_mute_chat_succeed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_unmute_chat_succeed, Toast.LENGTH_SHORT).show();
                    }

                    sendBroadcastForBlockMuteUpdata();
                }

                @Override
                public void onFailed(int code) {
                    if (code == 408) {
                        Toast.makeText(MessageInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                    } else if(muteState){
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_mute_chat_failed, Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_unmute_chat_failed, Toast.LENGTH_SHORT).show();
                    }
                    switchButtonMute.setCheck(!muteState);
                }

                @Override
                public void onException(Throwable exception) {
                    if(muteState){
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_mute_chat_failed, Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_unmute_chat_failed, Toast.LENGTH_SHORT).show();
                    }
                    switchButtonMute.setCheck(!muteState);
                }
            });
        }
    };

    private void updateBlockSwitchBtn() {
        boolean block = NIMClient.getService(FriendService.class).isInBlackList(account);
        switchButtonBlock.setCheck(block);
    }

    private SwitchButton.OnChangedListener onBlockChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean blockState) {
            if (!NetworkUtil.isNetAvailable(MessageInfoActivity.this)) {
                Toast.makeText(MessageInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                switchButtonBlock.setCheck(!blockState);
                return;
            }

            if(blockState){
                NIMClient.getService(FriendService.class).addToBlackList(account).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        sendBroadcastForBlockMuteUpdata();
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_block_chat_succeed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            Toast.makeText(MessageInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MessageInfoActivity.this, R.string.samchat_block_chat_failed, Toast.LENGTH_SHORT).show();
                        }
                        switchButtonBlock.setCheck(!blockState);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_block_chat_failed, Toast.LENGTH_SHORT).show();
                        switchButtonBlock.setCheck(!blockState);
                    }
                });
            }else{
                NIMClient.getService(FriendService.class).removeFromBlackList(account).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        sendBroadcastForBlockMuteUpdata();
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_unblock_chat_succeed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            Toast.makeText(MessageInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MessageInfoActivity.this, R.string.samchat_unblock_chat_failed, Toast.LENGTH_SHORT).show();
                        }
                        switchButtonBlock.setCheck(!blockState);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Toast.makeText(MessageInfoActivity.this, R.string.samchat_unblock_chat_failed, Toast.LENGTH_SHORT).show();
                        switchButtonBlock.setCheck(!blockState);
                    }
                });
            }

        }
    };

	private boolean isClearing=false;
	private void clearChatHistory(){
		if(isClearing){
			return;
		}
		isClearing = true;
		DialogMaker.showProgressDialog(this, null, getString(R.string.samchat_processing), false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		}).setCanceledOnTouchOutside(false);
		SamDBManager.getInstance().asyncClearChatHisotry(SessionTypeEnum.P2P, account, mode, new NIMCallback(){
			@Override
			public void onResult(Object obj1, Object obj2, int code) {
				getHandler().postDelayed(new Runnable() {
					@Override
					public void run() {
						DialogMaker.dismissProgressDialog();
						if(!isDestroyedCompatible()){
							isClearing=false;
						}
					}
				},0);
			}
		});
	}


    private void openUserProfile() {
        UserProfileActivity.start(this, account);
    }

    /**
     * 创建群聊
     */
    private void createTeamMsg() {
        ArrayList<String> memberAccounts = new ArrayList<>();
        memberAccounts.add(account);
		 /*
        ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(memberAccounts, 50);
		 LogUtil.e("test","createTeamMsg 2 memberAccounts:"+memberAccounts);
        NimUIKit.startContactSelect(this, option, REQUEST_CODE_NORMAL);*/
        /*SAMC_BEGIN(samchat team msg)*/
        SamchatMemberSelectActivity.startActivityForResult(MessageInfoActivity.this,new SamchatMemberSelectActivity.Option(1,50,memberAccounts),REQUEST_CODE_NORMAL);
		 
		 /*SAMC_END(samchat team msg)*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NORMAL) {
                final ArrayList<String> selected = data.getStringArrayListExtra(SamchatMemberSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    TeamCreateHelper.createNormalTeam(MessageInfoActivity.this, selected, true, new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    Toast.makeText(DemoCache.getContext(), R.string.samchat_select_one_member_at_least, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
