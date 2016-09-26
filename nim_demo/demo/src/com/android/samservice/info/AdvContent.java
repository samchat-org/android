package com.android.samservice.info;

import com.android.samservice.type.AdvContentTypeEnum;

public class AdvContent{
	public int seq;
	public AdvContentTypeEnum type;
	public String content;

	public AdvContent(){
		this.seq = 0;
		this.type = AdvContentTypeEnum.Text;
		this.content = null;
	}

	public AdvContent(int seq, AdvContentTypeEnum type, String content){
		this.seq = seq;
		this.type = type;
		this.content = content;
	}
	
}