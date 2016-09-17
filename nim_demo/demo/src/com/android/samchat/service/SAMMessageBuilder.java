package com.android.samchat.service;

import android.os.Environment;

import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samservice.info.SendQuestion;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.session.sam_message.SAMMessage;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SAMMessageBuilder{
	public static IMMessage createReceivedQuestionMessage(ReceivedQuestion rq){
		IMMessage im = MessageBuilder.createTextMessage(""+rq.getsender_unique_id(), SessionTypeEnum.P2P, rq.getquestion());
		im.setDirect(MsgDirectionEnum.In);
		im.setFromAccount(""+rq.getsender_unique_id());
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

	public static IMMessage createReceivedAdvertisementMessage(Advertisement adv){
		if(adv.gettype() == Constants.ADV_TYPE_TEXT)
			return createReceivedAdvertisementTextMessage(adv);
		else if(adv.gettype() == Constants.ADV_TYPE_PIC)
			return createReceivedAdvertisementImageMessage(adv);
		else
			return createReceivedAdvertisementVideoMessage(adv);
	}

	public static IMMessage createReceivedAdvertisementTextMessage(Advertisement adv){
		IMMessage im = MessageBuilder.createTextMessage(""+adv.getsender_unique_id(), SessionTypeEnum.P2P, adv.getcontent());
		im.setDirect(MsgDirectionEnum.In);
		im.setFromAccount(""+adv.getsender_unique_id());
		im.setStatus(MsgStatusEnum.success);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		im.setRemoteExtension(msg_from);
		return im;
	}

	public static IMMessage createReceivedAdvertisementImageMessage(Advertisement adv){
		String extension = FileUtil.getExtensionName(adv.getcontent());
		String MD5Path = Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
								+ StorageType.TYPE_THUMB_IMAGE.getStoragePath()+"/"+ StringUtil.makeMd5(adv.getcontent());
		IMMessage im = MessageBuilder.createImageMessage(""+adv.getsender_unique_id(), SessionTypeEnum.P2P, new File(MD5Path));
		im.setDirect(MsgDirectionEnum.In);
		im.setFromAccount(""+adv.getsender_unique_id());
		im.setStatus(MsgStatusEnum.success);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		im.setRemoteExtension(msg_from);
		ImageAttachment attachment = (ImageAttachment)im.getAttachment();
		
		attachment.setExtension(extension);
		attachment.setMd5(StringUtil.makeMd5(adv.getcontent_thumb()));
		attachment.setUrl(adv.getcontent());
		attachment.setPath(null);
		
		return im;
	}

	public static IMMessage createReceivedAdvertisementVideoMessage(Advertisement adv){
		String extension = FileUtil.getExtensionName(adv.getcontent());
		String MD5Path = Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
								+ StorageType.TYPE_THUMB_VIDEO.getStoragePath()+"/"+ StringUtil.makeMd5(adv.getcontent());
		IMMessage im = MessageBuilder.createVideoMessage(""+adv.getsender_unique_id(), SessionTypeEnum.P2P, new File(MD5Path),0,0,0,null);
		im.setDirect(MsgDirectionEnum.In);
		im.setFromAccount(""+adv.getsender_unique_id());
		im.setStatus(MsgStatusEnum.success);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		im.setRemoteExtension(msg_from);
		VideoAttachment attachment = (VideoAttachment)im.getAttachment();
		
		attachment.setExtension(extension);
		attachment.setMd5(StringUtil.makeMd5(adv.getcontent_thumb()));
		attachment.setUrl(adv.getcontent());
		attachment.setPath(null);
		
		return im;
	}

	public static IMMessage createSendAdvertisementTextMessage(Advertisement adv,IMMessage msg){
		IMMessage im = MessageBuilder.createTextMessage(msg.getSessionId(), SessionTypeEnum.P2P, adv.getcontent());
		im.setDirect(MsgDirectionEnum.Out);
		im.setFromAccount(SamService.getInstance().get_current_user().getAccount());
		im.setStatus(MsgStatusEnum.unread);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_CUSTOMER));
		msg_from.put(NimConstants.SA_ADV_ID, ""+adv.getadv_id());
		im.setRemoteExtension(msg_from);
		return im;
	}

	public static IMMessage createSendAdvertisementImageMessage(Advertisement adv, IMMessage sm, IMMessage msg){
		ImageAttachment attachment = (ImageAttachment)sm.getAttachment();
		IMMessage im = MessageBuilder.createImageMessage(msg.getSessionId(), SessionTypeEnum.P2P, new File(attachment.getPath()));
		im.setDirect(MsgDirectionEnum.Out);
		im.setFromAccount(SamService.getInstance().get_current_user().getAccount());
		im.setStatus(MsgStatusEnum.unread);
		Map<String, Object> msg_from = new HashMap<>();
		msg_from.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_CUSTOMER));
		msg_from.put(NimConstants.SA_ADV_ID, ""+adv.getadv_id());
		im.setRemoteExtension(msg_from);
		ImageAttachment attachment2 = (ImageAttachment) im.getAttachment();
		attachment2.setDisplayName(attachment.getDisplayName());
		attachment2.setMd5(attachment.getMd5());
		attachment2.setPath(attachment.getPath());
		return im;
	}

}