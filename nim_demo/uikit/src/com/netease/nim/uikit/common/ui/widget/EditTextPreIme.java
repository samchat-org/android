package com.netease.nim.uikit.common.ui.widget;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.netease.nim.uikit.common.util.log.LogUtil;

public class EditTextPreIme extends EditText {
	public interface OnBackKeyListener {
		public void OnBackKeyPress();
	}

	public OnBackKeyListener callback;

	public void setOnBackKeyListener(OnBackKeyListener cb){
		callback = cb;
	}
	
	public EditTextPreIme(Context context) {
		super(context);
	}
  
	public EditTextPreIme(Context context, AttributeSet attrs){
		super(context, attrs);
	}
  
	public EditTextPreIme(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
  
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if(callback!=null){
				callback.OnBackKeyPress();
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}
}