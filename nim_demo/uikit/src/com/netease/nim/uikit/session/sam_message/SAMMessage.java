package com.netease.nim.uikit.session.sam_message;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nim.uikit.NimConstants;

import java.util.HashMap;
import java.util.Map;

public class SAMMessage implements IMMessage
{
	public static final String UUID_PREFIX_SQ="uuid_prefix_sq_";
	public static final String UUID_PREFIX_RQ="uuid_prefix_rq_";
	public static final String UUID_PREFIX_SADV="uuid_prefix_sadv_";
	public static final String UUID_PREFIX_RADV="uuid_prefix_radv_";

	private long id;
	private String content;
	private int msg_type;
	private String from;
	private String from_username;
	private String to;
	private String to_username;
	private long time;

	public static int getMsgType(IMMessage im){
		if(im.getUuid().indexOf(UUID_PREFIX_SQ)!=-1){
			return NimConstants.MSG_TYPE_SQ;
		}else if(im.getUuid().indexOf(UUID_PREFIX_RQ)!=-1){
			return NimConstants.MSG_TYPE_RQ;
		}else if(im.getUuid().indexOf(UUID_PREFIX_SADV)!=-1){
			return NimConstants.MSG_TYPE_SEND_ADV;
		}else if(im.getUuid().indexOf(UUID_PREFIX_RADV)!=-1){
			return NimConstants.MSG_TYPE_RCVD_ADV;
		}else{
			return NimConstants.MSG_TYPE_IM;
		}
	}

	public SAMMessage(long id, int msg_type, String content,String from, String from_username, String to, String to_username,long time){
		this.id = id;
		this.msg_type = msg_type;
		this.content = content;
		this.from = from;
		this.to = to;
		this.from_username = from_username;
		this.to_username = to_username;
		this.time = time;
	}

	public MsgAttachment getAttachment(){
		return null;
	}

	public AttachStatusEnum getAttachStatus(){
		return AttachStatusEnum.def;
	}

	public CustomMessageConfig getConfig(){
		CustomMessageConfig config = new CustomMessageConfig();
       config.enableRoaming = false;
		return config;
	}

	public String getContent(){
		return content;
	}

	public MsgDirectionEnum getDirect(){
		if(msg_type == NimConstants.MSG_TYPE_SQ){
			return MsgDirectionEnum.Out;
		}else if(msg_type == NimConstants.MSG_TYPE_RQ){
			return MsgDirectionEnum.In;
		}else if(msg_type == NimConstants.MSG_TYPE_SEND_ADV){
			return MsgDirectionEnum.Out;
		}else{
			return MsgDirectionEnum.In;
		}
	}

	public String getFromAccount(){
		return from;
	}

	public String getFromNick(){
		return from_username;
	}

	public Map<String,Object> getLocalExtension(){
		return null;
	}

	public MsgTypeEnum getMsgType(){
		if(msg_type == NimConstants.MSG_TYPE_SQ || msg_type == NimConstants.MSG_TYPE_RQ){
			return MsgTypeEnum.text;
		}else{
			return MsgTypeEnum.text;
		}
	}

	public String getPushContent(){
		return content;
	}

	public Map<java.lang.String,java.lang.Object>	getPushPayload(){
		return null;
	}

	public Map<java.lang.String,java.lang.Object>	getRemoteExtension(){
        Map<String, Object> extension = new HashMap<>(1);
		if(msg_type == NimConstants.MSG_TYPE_SQ){
			extension.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		}else if(msg_type == NimConstants.MSG_TYPE_RQ){
			extension.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_CUSTOMER));
		}else if(msg_type == NimConstants.MSG_TYPE_SEND_ADV){
			extension.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_CUSTOMER));
		}else{
			extension.put(NimConstants.MSG_FROM,new Integer(NimConstants.FROM_SP));
		}
        return extension;
	}

	public String getSessionId(){
		if(msg_type == NimConstants.MSG_TYPE_SQ){
			return to;
		}else if(msg_type == NimConstants.MSG_TYPE_RQ){
			return from;
		}else if(msg_type == NimConstants.MSG_TYPE_SEND_ADV){
			return to;
		}else{
			return from;
		}
	}

	public SessionTypeEnum getSessionType(){
		return SessionTypeEnum.P2P;		
	}

	public MsgStatusEnum getStatus(){
		if(msg_type == NimConstants.MSG_TYPE_SQ){
			return MsgStatusEnum.unread;
		}else if(msg_type == NimConstants.MSG_TYPE_RQ){
			return MsgStatusEnum.success;
		}else if(msg_type == NimConstants.MSG_TYPE_SEND_ADV){
			return MsgStatusEnum.unread;
		}else{
			return MsgStatusEnum.success;
		}
	}

	public long	getTime(){
		return time;
	}

	public String getUuid(){
		if(msg_type == NimConstants.MSG_TYPE_SQ){
			return UUID_PREFIX_SQ + id;
		}else if(msg_type == NimConstants.MSG_TYPE_RQ){
			return UUID_PREFIX_RQ + id;
		}else if(msg_type == NimConstants.MSG_TYPE_SEND_ADV){
			return UUID_PREFIX_SADV + id;
		}else{
			return UUID_PREFIX_RADV + id;
		}
	}

	public boolean isRemoteRead(){
		return true;
	}

	public boolean	isTheSame(IMMessage message){
		if(getUuid().equals(message.getUuid())){
			return true;
		}else{
			return false;
		}
	}

	public void	setAttachment(MsgAttachment attachment){
		return;
	}

	public void setAttachStatus(AttachStatusEnum attachStatus){
		return;
	}

	public void	setConfig(CustomMessageConfig config){
		return;
	}

	public void	setContent(java.lang.String content){
		return;
	}

	public void	setDirect(MsgDirectionEnum direct){
		return;
	}

	public void	setFromAccount(java.lang.String account){
		return;
	}

	public void	setLocalExtension(java.util.Map<java.lang.String,java.lang.Object> localExtension){
		return;
	}

	public void	setPushContent(java.lang.String pushContent){
		return;
	}

	public void	setPushPayload(java.util.Map<java.lang.String,java.lang.Object> pushPayload){
		return;
	}

	public void	setRemoteExtension(java.util.Map<java.lang.String,java.lang.Object> remoteExtension){
		return;
	}

	public void	setStatus(MsgStatusEnum status){
		return;
	}

}



