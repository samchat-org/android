package com.android.samchat.viewholder;

import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.samchat.cache.MsgSessionDataCache;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.recent.viewholder.SamchatRecentContactAdapter;
import com.netease.nim.uikit.NIMCallback;
import com.netease.nim.uikit.NimUIKit;
import com.android.samchat.R;
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
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nim.uikit.recent.viewholder.RecentContactAdapter;
import com.android.samservice.info.MsgSession;
import com.android.samservice.SamService;
import com.netease.nim.uikit.NimConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class SamchatRecentViewHolder extends TViewHolder implements OnClickListener {
    private static final String TAG="SamchatRecentViewHolder";
    protected FrameLayout portraitPanel;

    protected HeadImageView imgHead;

    protected TextView tvNickname;

    protected TextView tvMessage;

    protected TextView tvUnread;

    protected View unreadIndicator;

    protected TextView tvDatetime;

     protected TextView tvCategory;

    // 消息发送错误状态标记，目前没有逻辑处理
    protected ImageView imgMsgStatus;

    protected RecentContact recent;

    protected View bottomLine;
    protected View topLine;

    protected ImageView mute_img_iv;
    protected ImageView block_img_iv;

    protected abstract String getContent(MsgSession session);

    public void refresh(Object item) {
        recent = (RecentContact) item;

        updateBackground();

        loadPortrait();

        updateNewIndicator();

        updateNickLabel(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));

        if(getAdapter() instanceof SamchatRecentContactAdapter){
           if(((SamchatRecentContactAdapter)getAdapter()).getmode() == 0){
               //customer mode
               updateCategory(UserInfoHelper.getServiceCategory(recent.getContactId()));
           }
        }

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
        //int unreadNum = recent.getUnreadCount();
        int unreadNum = 0;
        if(getAdapter() instanceof SamchatRecentContactAdapter){
			  MsgSession session = null;
            if(((SamchatRecentContactAdapter)getAdapter()).getmode() == ModeEnum.CUSTOMER_MODE.getValue()){
                 session = MsgSessionDataCache.getInstance().getMsgSession(recent.getContactId(), ModeEnum.CUSTOMER_MODE.getValue());
			   }else{
                 session = MsgSessionDataCache.getInstance().getMsgSession(recent.getContactId(), ModeEnum.SP_MODE.getValue());
			  }
            unreadNum = session.gettotal_unread();
		  }
        tvUnread.setVisibility(unreadNum > 0 ? View.VISIBLE : View.GONE);
        tvUnread.setText(unreadCountShowRule(unreadNum));
        if(unreadNum > 0){
            imgHead.setBorderColorResource(R.color.samchat_color_avatar_border_reminder);
        }else{
            imgHead.setBorderColorResource(R.color.samchat_color_avatar_border_default);
        }
    }

	private void updateMsgLabel() {
		MsgSession session = MsgSessionDataCache.getInstance().getMsgSession(recent.getContactId(), ((SamchatRecentContactAdapter)getAdapter()).getmode());
		if(session != null){
				MoonUtil.identifyFaceExpressionAndTags(context, tvMessage, getContent(session), ImageSpan.ALIGN_BOTTOM, 0.45f);
				MsgStatusEnum status = 	MsgStatusEnum.statusOfValue(session.getrecent_msg_status());
				LogUtil.e(TAG,"get update msg status:"+status.getValue());
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
	}

    protected void updateNickLabel(String nick) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 64 + 12 + 12 + 12); // 减去固定的头像和时间宽度

        if (labelWidth > 0) {
            tvNickname.setMaxWidth(labelWidth/2);
        }

        tvNickname.setText(nick);
    }

    protected void updateCategory(String category) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 64 + 12 + 12 + 12); // 减去固定的头像和时间宽度

        if (labelWidth > 0) {
            tvCategory.setMaxWidth(labelWidth/2);
        }

        tvCategory.setText(category);
    }

    protected void updateStatus() {
        boolean block = NIMClient.getService(FriendService.class).isInBlackList(recent.getContactId());
        boolean mute = !NIMClient.getService(FriendService.class).isNeedMessageNotify(recent.getContactId());
        mute_img_iv.setVisibility(mute ? View.VISIBLE:View.GONE);
        block_img_iv.setVisibility(block ? View.VISIBLE:View.GONE);
    }

	protected abstract int getResId();

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
        this.tvCategory = (TextView)view.findViewById(R.id.tv_category);
        this.mute_img_iv = (ImageView)view.findViewById(R.id.mute_img);
        this.block_img_iv = (ImageView)view.findViewById(R.id.block_img);
    }

    protected String unreadCountShowRule(int unread) {
        unread = Math.min(unread, 99);
        return String.valueOf(unread);
    }

    protected RecentContactsCallback getCallback() {
        return ((SamchatRecentContactAdapter)getAdapter()).getCallback();
    }

    @Override
    public void onClick(View v) {

    }
}

