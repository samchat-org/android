package com.android.samchat.service;

import com.android.samservice.SamService;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samservice.info.SendQuestion;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.sam_message.SAMMessage;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.HashMap;
import java.util.Map;

public class SAMMessageBuilder{
	public static IMMessage createReceivedQuestionMessage(ReceivedQuestion rq){
		IMMessage im = MessageBuilder.createTextMessage(""+rq.getsender_unique_id(), SessionTypeEnum.P2P, rq.getquestion());
		im.setDirect(MsgDirectionEnum.In);
		im.setFromAccount(rq.getsender_username());
		im.setStatus(MsgStatusEnum.success);
		Map<String, Object> msg_from = new HashMap<>();
        msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_CUSTOMER));
		im.setRemoteExtension(msg_from);
		return im;
	}

	public static IMMessage createSendQuestionMessage(SendQuestion sq, IMMessage msg){
		IMMessage im = MessageBuilder.createTextMessage(msg.getSessionId(), SessionTypeEnum.P2P, sq.getquestion());
		im.setDirect(MsgDirectionEnum.Out);
		im.setFromAccount(SamService.getInstance().get_current_user().getAccount());
		im.setStatus(MsgStatusEnum.unread);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		msg_from.put(NimConstants.SQ_QUEST_ID, ""+sq.getquestion_id());
		im.setRemoteExtension(msg_from);
		return im;
	}

}