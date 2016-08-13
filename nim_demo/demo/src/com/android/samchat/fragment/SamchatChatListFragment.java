package com.android.samchat.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.netease.nim.demo.R;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.netease.nim.demo.main.fragment.MainTabFragment;
import com.netease.nim.demo.main.activity.MainActivity;

public class SamchatChatListFragment extends MainTabFragment {

    private View notifyBar;

    private TextView notifyBarText;

    private List<OnlineClient> onlineClients;

    private View multiportBar;

    private SamchatChatFragment fragment;

    public SamchatChatListFragment() {
        this.setContainerId(MainTab.SAMCHAT_CHAT.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    public void onDestroy() {
        registerObservers(false);
        super.onDestroy();
    }

    @Override
    protected void onInit() {
        findViews();
        registerObservers(true);

        addSamchatChatFragment();
    }

    private void registerObservers(boolean register) {

    }

    private void findViews() {
        notifyBar = getView().findViewById(R.id.status_notify_bar);
        notifyBar.setVisibility(View.GONE);

        multiportBar = getView().findViewById(R.id.multiport_notify_bar);
        multiportBar.setVisibility(View.GONE);
    }

    private void addSamchatChatFragment() {
        fragment = new SamchatChatFragment();
        fragment.setContainerId(R.id.samchat_chat_fragment);

        final UI activity = (UI) getActivity();

        fragment = (SamchatChatFragment) activity.addFragment(fragment);

        fragment.setCallbackCustomer(new RecentContactsCallback() {
            @Override
            public void onRecentContactsLoaded() {
                
            }

            @Override
            public void onUnreadCountChange(int unreadCount) {
                LogUtil.e("test","onUnreadCountChange Customer:" + unreadCount);
                ReminderManager.getInstance().updateSessionUnreadNum(unreadCount);
					((MainActivity)getActivity()).setchat_unread_count_customer(unreadCount);
            }

            @Override
            public void onItemClick(RecentContact recent) {
                switch (recent.getSessionType()) {
                    case P2P:
                        SessionHelper.startP2PSession(getActivity(), recent.getContactId());
                        break;
                    case Team:
                        SessionHelper.startTeamSession(getActivity(), recent.getContactId());
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
                    return "[白板]";
                } else if (attachment instanceof StickerAttachment) {
                    return "[贴图]";
                } else if (attachment instanceof SnapChatAttachment) {
                    return "[阅后即焚]";
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
        });

        fragment.setCallbackSP(new RecentContactsCallback() {
            @Override
            public void onRecentContactsLoaded() {
                
            }

            @Override
            public void onUnreadCountChange(int unreadCount) {
                LogUtil.e("test","onUnreadCountChange SP:" + unreadCount);
                ReminderManager.getInstance().updateSessionUnreadNum(unreadCount);
					((MainActivity)getActivity()).setchat_unread_count_sp(unreadCount);
            }

            @Override
            public void onItemClick(RecentContact recent) {
                switch (recent.getSessionType()) {
                    case P2P:
                        SessionHelper.startP2PSession(getActivity(), recent.getContactId());
                        break;
                    case Team:
                        SessionHelper.startTeamSession(getActivity(), recent.getContactId());
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
                    return "[白板]";
                } else if (attachment instanceof StickerAttachment) {
                    return "[贴图]";
                } else if (attachment instanceof SnapChatAttachment) {
                    return "[阅后即焚]";
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
        });
    }
}

