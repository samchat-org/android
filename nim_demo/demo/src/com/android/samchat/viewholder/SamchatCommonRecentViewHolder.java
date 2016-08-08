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

public class SamchatCommonRecentViewHolder extends SamchatRecentViewHolder {

	@Override
	protected String getContent(MsgSession session) {
		return descOfMsg(session);
	}

	@Override
    protected int getResId() {
        return R.layout.samchat_recent_contact_list_item;
    }

	protected String descOfMsg(MsgSession session) {
		List<String> uuids = new ArrayList<String>();
		uuids.add(session.getrecent_msg_uuid());
		List<IMMessage> msgs = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
		return msgs.get(0).getContent();
	}

    // SDK本身只记录原始数据，第三方APP可根据自己实际需求，在最近联系人列表上显示缩略消息
    // 以下为一些常见消息类型的示例。
    private String getDefaultDigest(MsgAttachment attachment) {
        switch (recent.getMsgType()) {
            case text:
                return recent.getContent();
            case image:
                return "[图片]";
            case video:
                return "[视频]";
            case audio:
                return "[语音消息]";
            case location:
                return "[位置]";
            case file:
                return "[文件]";
            case tip:
                return "[通知提醒]";
            case notification:
                return TeamNotificationHelper.getTeamNotificationText(recent.getContactId(),
                        recent.getFromAccount(),
                        (NotificationAttachment) recent.getAttachment());
            case avchat:
                AVChatAttachment avchat = (AVChatAttachment) attachment;
                if (avchat.getState() == AVChatRecordState.Missed && !recent.getFromAccount().equals(NimUIKit.getAccount())) {
                    // 未接通话请求
                    StringBuilder sb = new StringBuilder("[未接");
                    if (avchat.getType() == AVChatType.VIDEO) {
                        sb.append("视频电话]");
                    } else {
                        sb.append("音频电话]");
                    }
                    return sb.toString();
                } else if (avchat.getState() == AVChatRecordState.Success) {
                    StringBuilder sb = new StringBuilder();
                    if (avchat.getType() == AVChatType.VIDEO) {
                        sb.append("[视频电话]: ");
                    } else {
                        sb.append("[音频电话]: ");
                    }
                    sb.append(TimeUtil.secToTime(avchat.getDuration()));
                    return sb.toString();
                } else {
                    if (avchat.getType() == AVChatType.VIDEO) {
                        return ("[视频电话]");
                    } else {
                        return ("[音频电话]");
                    }
                }
            default:
                return "[自定义消息]";
        }
    }
}

