package com.netease.nim.uikit.recent.viewholder;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.helper.TeamNotificationHelper;
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

public class CommonRecentViewHolder extends RecentViewHolder {

    @Override
    protected String getContent() {
        return descOfMsg();
    }

    protected String descOfMsg() {
        if (recent.getMsgType() == MsgTypeEnum.text) {
            return recent.getContent();
        } else if (recent.getMsgType() == MsgTypeEnum.tip) {
            String digest = null;
            if (getCallback() != null) {
                digest = getCallback().getDigestOfTipMsg(recent);
            }

            if (digest == null) {
                digest = getDefaultDigest(null);
            }

            return digest;
        } else if (recent.getAttachment() != null) {
            String digest = null;
            if (getCallback() != null) {
                digest = getCallback().getDigestOfAttachment(recent.getAttachment());
            }

            if (digest == null) {
                digest = getDefaultDigest(recent.getAttachment());
            }

            return digest;
        }
        return "";
    }

    // SDK本身只记录原始数据，第三方APP可根据自己实际需求，在最近联系人列表上显示缩略消息
    // 以下为一些常见消息类型的示例。
    private String getDefaultDigest(MsgAttachment attachment) {
        switch (recent.getMsgType()) {
            case text:
                return recent.getContent();
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
                        sb.append("["+context.getString(R.string.samchat_video_call)+"]: ");
                    } else {
                        sb.append("["+context.getString(R.string.samchat_audio_call)+"]: ");
                    }
                    sb.append(TimeUtil.secToTime(avchat.getDuration()));
                    return sb.toString();
                } else {
                    if (avchat.getType() == AVChatType.VIDEO) {
                        return ("["+context.getString(R.string.samchat_video_call)+"]: ");
                    } else {
                        return ("["+context.getString(R.string.samchat_audio_call)+"]: ");
                    }
                }
            default:
                return "";
        }
    }
}
