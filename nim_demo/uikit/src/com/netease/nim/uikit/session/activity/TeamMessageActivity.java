package com.netease.nim.uikit.session.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.SimpleCallback;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;
import com.netease.nim.uikit.session.fragment.TeamMessageFragment;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nim.uikit.session.sam_message.SessionBasicInfo;
import com.netease.nim.uikit.team.activity.NormalTeamInfoActivity;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * 群聊界面
 * <p/>
 * Created by huangjun on 2015/3/5.
 */
public class TeamMessageActivity extends BaseMessageActivity {

    // model
    private Team team;

    private View invalidTeamTipView;

    private TextView invalidTeamTipText;

    private TeamMessageFragment fragment;

    private Class<? extends Activity> backToClass;

	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(TeamMessageActivity.this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(NimConstants.BROADCAST_P2P_ACTIVITY_START);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(NimConstants.BROADCAST_P2P_ACTIVITY_START)){
					finish();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
		isBroadcastRegistered  = true;
	}
	private void unregisterBroadcastReceiver(){
	    if(isBroadcastRegistered){
			broadcastManager.unregisterReceiver(broadcastReceiver);
			isBroadcastRegistered = false;
		}
	}

	private void sendbroadcast(){
		Intent intent = new Intent();
		intent.setAction(NimConstants.BROADCAST_TEAM_ACTIVITY_START);
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
		manager.sendBroadcast(intent);
	}

    public static void start(Context context, String tid, SessionCustomization customization, int mode, Class<? extends Activity> backToClass) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, tid);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(Extras.EXTRA_MODE,mode);
        intent.putExtra(Extras.EXTRA_BACK_TO_CLASS, backToClass);
        intent.setClass(context, TeamMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intent);
    }

    protected void findViews() {
        invalidTeamTipView = findView(R.id.invalid_team_tip);
        invalidTeamTipText = findView(R.id.invalid_team_text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backToClass = (Class<? extends Activity>) getIntent().getSerializableExtra(Extras.EXTRA_BACK_TO_CLASS);
        findViews();

        registerTeamUpdateObserver(true);
        registerBroadcastReceiver();
        sendbroadcast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerTeamUpdateObserver(false);
        unregisterBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestTeamInfo();
    }

    /**
     * 请求群基本信息
     */
    private void requestTeamInfo() {
        // 请求群基本信息
        Team t = TeamDataCache.getInstance().getTeamById(sessionId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            TeamDataCache.getInstance().fetchTeamById(sessionId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onRequestTeamInfoFailed();
                    }
                }
            });
        }
    }

    private void onRequestTeamInfoFailed() {
        Toast.makeText(TeamMessageActivity.this, R.string.samchat_fetch_team_info_failed, Toast.LENGTH_SHORT);
        finish();
    }

    /**
     * 更新群信息
     *
     * @param d
     */
    private void updateTeamInfo(final Team d) {
        if (d == null) {
            return;
        }

        team = d;
        fragment.setTeam(team);

        //setTitle(team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + ")");
        titlebar_name_tv.setText(team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + ")");

        invalidTeamTipText.setText(team.getType() == TeamTypeEnum.Normal ? R.string.samchat_normal_team_invalid_tip : R.string.samchat_team_invalid_tip);
        invalidTeamTipView.setVisibility(team.isMyTeam() ? View.GONE : View.VISIBLE);
    }

    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerTeamUpdateObserver(boolean register) {
		if (register) {
			TeamDataCache.getInstance().registerTeamDataChangedObserver(teamDataChangedObserver);
			TeamDataCache.getInstance().registerTeamMemberDataChangedObserver(teamMemberDataChangedObserver);
		} else {
			TeamDataCache.getInstance().unregisterTeamDataChangedObserver(teamDataChangedObserver);
			TeamDataCache.getInstance().unregisterTeamMemberDataChangedObserver(teamMemberDataChangedObserver);
		}
		FriendDataCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver, register);
        NimUIKit.getCallback().registerClearHistoryObserver(ClearHistoryObserver, register);
    }

	SamchatObserver<SessionBasicInfo> ClearHistoryObserver = new SamchatObserver < SessionBasicInfo >(){
		@Override
		public void onEvent(SessionBasicInfo sinfo){
			if(sinfo.gettype() == SessionTypeEnum.Team && sinfo.getsession_id().equals(sessionId)){
				clearMessageList();
			}
		}
	};

    /**
     * 群资料变动通知和移除群的通知（包括自己退群和群被解散）
     */
    TeamDataCache.TeamDataChangedObserver teamDataChangedObserver = new TeamDataCache.TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            if (team == null) {
                return;
            }
            for (Team t : teams) {
                if (t.getId().equals(team.getId())) {
                    updateTeamInfo(t);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team == null) {
                return;
            }
            if (team.getId().equals(TeamMessageActivity.this.team.getId())) {
                updateTeamInfo(team);
            }
        }
    };

    /**
     * 群成员资料变动通知和移除群成员通知
     */
    TeamDataCache.TeamMemberDataChangedObserver teamMemberDataChangedObserver = new TeamDataCache.TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> members) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveTeamMember(TeamMember member) {
        }
    };

    FriendDataCache.FriendDataChangedObserver friendDataChangedObserver = new FriendDataCache.FriendDataChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            fragment.refreshMessageList();
        }
    };

    @Override
    protected MessageFragment fragment() {
        // 添加fragment
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.Team);
        fragment = new TeamMessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_team_message_activity;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (backToClass != null) {
            Intent intent = new Intent();
            intent.setClass(this, backToClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

	private FrameLayout back_arrow_layout;
	private ImageView back_icon_iv;
	private TextView titlebar_name_tv;

	private void setTitlebarCustomerMode(){
        getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
    }

    private void setTitlebarSPMode(){
        getToolBar().setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left_sp);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
    }

	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onNavigateUpClicked();
			}
		});
	}

	@Override
	protected void initToolBar() {
		setToolBar(R.id.toolbar);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		back_icon_iv = findView(R.id.back_icon);
		titlebar_name_tv = findView(R.id.titlebar_name);
		titlebar_name_tv.setText(team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + ")");
		if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
			setTitlebarCustomerMode();
		}else{
			setTitlebarSPMode();
		}
		setupBackArrowClick();
    }
}
