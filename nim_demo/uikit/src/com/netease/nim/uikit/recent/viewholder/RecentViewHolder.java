package com.netease.nim.uikit.recent.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.netease.nim.uikit.recent.RecentContactsFragment;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nim.uikit.NimConstants;

public abstract class RecentViewHolder extends TViewHolder implements OnClickListener {

    protected FrameLayout portraitPanel;

    protected HeadImageView imgHead;

    protected TextView tvNickname;

    protected TextView tvMessage;

    protected TextView tvUnread;

    protected View unreadIndicator;

    protected TextView tvDatetime;

    protected ImageView imgMsgStatus;

    protected RecentContact recent;

    protected View bottomLine;
    protected View topLine;

    protected ImageView mute_img_iv;

    protected abstract String getContent();

    public void refresh(Object item) {
        recent = (RecentContact) item;

        updateBackground();

        loadPortrait();

        updateNewIndicator();

        updateNickLabel(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));

        updateMsgLabel();

        updateStatus();
    }

    public void refreshCurrentItem() {
        if (recent != null) {
            refresh(recent);
        }
    }

    private void updateBackground() {
        topLine.setVisibility(isFirstItem() ? View.GONE : View.VISIBLE);
        bottomLine.setVisibility(isLastItem() ? View.VISIBLE : View.GONE);
        /*SAMC_BEGIN(support multiple mode)*/
        if(getAdapter() instanceof SamchatRecentContactAdapter){
           if(((SamchatRecentContactAdapter)getAdapter()).getmode() == 0){
               //customer mode
                if ((recent.getTag() & NimConstants.RECENT_TAG_STICKY_CUSTOMER_ROLE) == 0) {
                    view.setBackgroundResource(R.drawable.nim_list_item_selector);
                } else {
                     view.setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
                }
           }else{
                //sp mode
                if ((recent.getTag() & NimConstants.RECENT_TAG_STICKY_SP_ROLE) == 0) {
                    view.setBackgroundResource(R.drawable.nim_list_item_selector);
                } else {
                     view.setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
                }

           }
        }

        /*if ((recent.getTag() & RecentContactsFragment.RECENT_TAG_STICKY) == 0) {
            view.setBackgroundResource(R.drawable.nim_list_item_selector);
        } else {
            view.setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
        }*/

        /*SAMC_END(support multiple mode)*/
        
    }

    protected void loadPortrait() {
        // 设置头像
        if (recent.getSessionType() == SessionTypeEnum.P2P) {
            imgHead.loadBuddyAvatar(recent.getContactId());
        } else if (recent.getSessionType() == SessionTypeEnum.Team) {
            Team team = TeamDataCache.getInstance().getTeamById(recent.getContactId());
            imgHead.loadTeamIconByTeam(team);
        }
    }

    private void updateNewIndicator() {
        int unreadNum = recent.getUnreadCount();
        tvUnread.setVisibility(unreadNum > 0 ? View.VISIBLE : View.GONE);
        tvUnread.setText(unreadCountShowRule(unreadNum));
        if(unreadNum > 0){
            imgHead.setBorderColorResource(R.color.samchat_color_avatar_border_reminder);
        }else{
            imgHead.setBorderColorResource(R.color.samchat_color_avatar_border_default);
        }
    }

    private void updateMsgLabel() {
        // 显示消息具体内容
        MoonUtil.identifyFaceExpressionAndTags(context, tvMessage, getContent(), ImageSpan.ALIGN_BOTTOM, 0.45f);
        //tvMessage.setText(getContent());

        MsgStatusEnum status = recent.getMsgStatus();
        switch (status) {
            case fail:
                imgMsgStatus.setImageResource(R.drawable.nim_g_ic_failed_small);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            case sending:
                imgMsgStatus.setImageResource(R.drawable.nim_recent_contact_ic_sending);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            default:
                imgMsgStatus.setVisibility(View.GONE);
                break;
        }

        String timeString = TimeUtil.getTimeShowString(recent.getTime(), true);
        tvDatetime.setText(timeString);
        if (!TextUtils.isEmpty(timeString) && timeString.equals("1970-01-01")) {
            tvDatetime.setVisibility(View.GONE);
        } else {
            tvDatetime.setVisibility(View.VISIBLE);
        }
    }

    protected void updateNickLabel(String nick) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 64 + 12 + 12); // 减去固定的头像和时间宽度

        if (labelWidth > 0) {
            tvNickname.setMaxWidth(labelWidth);
        }

        tvNickname.setText(nick);
    }

    protected void updateStatus() {
        boolean mute = !NIMClient.getService(FriendService.class).isNeedMessageNotify(recent.getContactId());
        mute_img_iv.setVisibility(mute ? View.VISIBLE:View.GONE);
    }

    protected int getResId() {
        return R.layout.nim_recent_contact_list_item;
    }

    public void inflate() {
        this.portraitPanel = (FrameLayout) view.findViewById(R.id.portrait_panel);
        this.imgHead = (HeadImageView) view.findViewById(R.id.img_head);
        this.tvNickname = (TextView) view.findViewById(R.id.tv_nickname);
        this.tvMessage = (TextView) view.findViewById(R.id.tv_message);
        this.tvUnread = (TextView) view.findViewById(R.id.unread_number_tip);
        this.unreadIndicator = view.findViewById(R.id.new_message_indicator);
        this.tvDatetime = (TextView) view.findViewById(R.id.tv_date_time);
        this.imgMsgStatus = (ImageView) view.findViewById(R.id.img_msg_status);
        this.bottomLine = view.findViewById(R.id.bottom_line);
        this.topLine = view.findViewById(R.id.top_line);
        this.mute_img_iv = (ImageView)view.findViewById(R.id.mute_img);
    }

    protected String unreadCountShowRule(int unread) {
        unread = Math.min(unread, 99);
        return String.valueOf(unread);
    }

    protected RecentContactsCallback getCallback() {
		 if(getAdapter() instanceof SamchatRecentContactAdapter){
			 return ((SamchatRecentContactAdapter)getAdapter()).getCallback();
		 }else{
          return ((RecentContactAdapter)getAdapter()).getCallback();
		 }
    }

    @Override
    public void onClick(View v) {

    }
}
