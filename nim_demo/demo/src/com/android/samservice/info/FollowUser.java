package com.android.samservice.info;

import com.android.samservice.Constants;

public class FollowUser extends BasicUserInfo{
	public int favourite_tag;
	public int block_tag;

	public FollowUser(){
		super(Constants.SAM_PROS);
		this.favourite_tag = Constants.NO_TAG;
		this.block_tag = Constants.NO_TAG;
	}
	
	public int getblock_tag(){
		return block_tag;
	}
	public void setblock_tag(int block_tag){
		this.block_tag = block_tag;
	}

	public int getfavourite_tag(){
		return favourite_tag;
	}
	public void setfavourite_tag(int favourite_tag){
		this.favourite_tag = favourite_tag;
	}

}

