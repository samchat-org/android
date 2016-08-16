package com.android.samchat.ui;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.Paint;  
import android.graphics.Typeface;  
import android.graphics.drawable.ColorDrawable;  
import android.util.AttributeSet;  
import android.view.MotionEvent;  
import android.view.View;  
import android.widget.TextView;
import com.netease.nim.demo.R;
import android.widget.EditText;

public class CircleEditText extends EditText {

	public CircleEditText(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);  
	}  
  
	public CircleEditText(Context context, AttributeSet attrs) {  
		super(context, attrs);  
	}  
  
	public CircleEditText(Context context) {  
		super(context);  
	}  

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(widthMeasureSpec, widthMeasureSpec); 
	}  
}

