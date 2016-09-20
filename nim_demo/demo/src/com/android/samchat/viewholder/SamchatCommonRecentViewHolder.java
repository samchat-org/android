package com.android.samchat.viewholder;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.helper.TeamNotificationHelper;
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nim.uikit.recent.viewholder.RecentViewHolder;
import com.android.samchat.R;
import com.android.samservice.info.MsgSession;
import java.util.List;
import java.util.ArrayList;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nim.uikit.NimConstants;

public class SamchatCommonRecentViewHolder extends SamchatRecentViewHolder {
	private static final String TAG = "SamchatCommonRecentViewHolder";

	@Override
	protected String getContent(MsgSession session) {
		return descOfMsg(session);
	}

	@Override
    protected int getResId() {
        return R.layout.samchat_recent_contact_list_item;
    }

	protected String descOfMsg(MsgSession session) {
		String digest = getDefaultDigest(session);
		return digest;
	}

   private String getDefaultDigest(MsgSession session) {
		if(session.getrecent_msg_subtype() == MsgTypeEnum.text.getValue()){
            return session.getrecent_msg_content();
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.image.getValue()){
            return "["+context.getString(R.string.samchat_picture)+"]";
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.video.getValue()){
            return "["+context.getString(R.string.samchat_video)+"]";
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.audio.getValue()){
            return "["+context.getString(R.string.samchat_audio)+"]";
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.location.getValue()){
            return "["+context.getString(R.string.samchat_location)+"]";
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.file.getValue()){
            return "["+context.getString(R.string.samchat_file)+"]";
        }else if(session.getrecent_msg_subtype() == MsgTypeEnum.tip.getValue()){
            return "["+context.getString(R.string.samchat_notice)+"]";
        }else{
            return "";
        }
    }
}

