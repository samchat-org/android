package com.android.samservice.info;

import com.netease.nimlib.sdk.msg.model.IMMessage;

public class AdvancedMessage{
	private Message msg;
	private IMMessage im;

	public AdvancedMessage(Message msg, IMMessage im){
		this.msg = msg;
		this.im = im;
	}

	public Message getmsg(){
		return msg;
	}

	public IMMessage getim(){
		return im;
	}
}