package com.android.samchat.viewholder;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.helper.TeamNotificationHelper;
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nim.uikit.recent.viewholder.RecentViewHolder;
import com.netease.nim.demo.R;
import com.android.samservice.info.MsgSession;
import java.util.List;
import java.util.ArrayList;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nim.uikit.NimConstants;

public class SamchatCommonRecentViewHolder extends SamchatRecentViewHolder {

	@Override
	protected String getContent(MsgSession session, IMMessage im) {
		return descOfMsg(session,im);
	}

	@Override
    protected int getResId() {
        return R.layout.samchat_recent_contact_list_item;
    }

	protected String descOfMsg(MsgSession session,IMMessage im) {
		String digest = getDefaultDigest(im,im.getAttachment());
		return digest;
	}

   private String getDefaultDigest(IMMessage im, MsgAttachment attachment) {
        switch (im.getMsgType()) {
            case text:
                return im.getContent();
            case image:
                return "["+context.getString(R.string.samchat_picture)+"]";
            case video:
                return "["+context.getString(R.string.samchat_video)+"]";
            case audio:
                return "["+context.getString(R.string.samchat_audio)+"]";
            case location:
                return "["+context.getString(R.string.samchat_location)+"]";
            case file:
                return "["+context.getString(R.string.samchat_file)+"]";
            case tip:
                return "["+context.getString(R.string.samchat_notice)+"]";
            case notification:
                return TeamNotificationHelper.getTeamNotificationText(recent.getContactId(),
                        recent.getFromAccount(),
                        (NotificationAttachment) recent.getAttachment());
            case avchat:
                AVChatAttachment avchat = (AVChatAttachment) attachment;
                if (avchat.getState() == AVChatRecordState.Missed && !recent.getFromAccount().equals(NimUIKit.getAccount())) {
                    // 未接通话请求
                    StringBuilder sb = new StringBuilder("["+context.getString(R.string.samchat_missing));
                    if (avchat.getType() == AVChatType.VIDEO) {
                        sb.append(context.getString(R.string.samchat_video_call)+"]");
                    } else {
                        sb.append(context.getString(R.string.samchat_audio_call)+"]");
                    }
                    return sb.toString();
                } else if (avchat.getState() == AVChatRecordState.Success) {
                    StringBuilder sb = new StringBuilder();
                    if (avchat.getType() == AVChatType.VIDEO) {
                        sb.append("[" + context.getString(R.string.samchat_video_call) + "]: ");
                    } else {
                        sb.append("[" + context.getString(R.string.samchat_audio_call) + "]: ");
                    }
                    sb.append(TimeUtil.secToTime(avchat.getDuration()));
                    return sb.toString();
                } else {
                    if (avchat.getType() == AVChatType.VIDEO) {
                        return ("["+ context.getString(R.string.samchat_video_call) +"]");
                    } else {
                        return ("["+ context.getString(R.string.samchat_audio_call) +"]");
                    }
                }
            default:
                return "";
        }
    }
}

