package com.android.samservice.info;

import com.netease.nimlib.sdk.msg.model.IMMessage;

public class AdvancedMessage{
	private Message msg;
	private IMMessage im;
	private SendQuestion sq;
	private Advertisement adv;

	public AdvancedMessage(Message msg, IMMessage im, SendQuestion sq){
		this.msg = msg;
		this.im = im;
		this.sq = sq;
		this.adv = null;
	}

	public AdvancedMessage(Message msg, IMMessage im, Advertisement adv){
		this.msg = msg;
		this.im = im;
		this.sq = null;
		this.adv = adv;
	}

	public Message getmsg(){
		return msg;
	}

	public IMMessage getim(){
		return im;
	}

	public SendQuestion getsq(){
		return sq;
	}

	public Advertisement getadv(){
		return adv;
	}
}