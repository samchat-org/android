package com.android.samchat.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;  
import android.graphics.Paint;  
import android.graphics.Typeface;  
import android.graphics.drawable.ColorDrawable;  
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;  
import android.widget.TextView;
import com.android.samchat.R;
import com.netease.nim.uikit.common.util.log.LogUtil;

import android.widget.EditText;

public class CircleEditText extends EditText {
	private boolean filled;
	private int emptyColor;
	private int solidColor;
	private int ringColor;
	private int ringWidth;
	
	private Paint mPaint;
	
	public CircleEditText(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleEditText, defStyle, 0);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		if(a != null){
			emptyColor = a.getColor(R.styleable.CircleEditText_emptyColor,getResources().getColor(R.color.samchat_color_mid_grey));
			solidColor = a.getColor(R.styleable.CircleEditText_solidColor,getResources().getColor(R.color.samchat_color_lime));
			ringColor = a.getColor(R.styleable.CircleEditText_ringColor, getResources().getColor(R.color.samchat_color_lemmon));
			ringWidth = a.getDimensionPixelSize(R.styleable.CircleEditText_ringWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm));
			a.recycle();
		}

		mPaint = new Paint();
		mPaint.setColor(emptyColor);
	}  
  
	public CircleEditText(Context context, AttributeSet attrs) {  
		this(context, attrs, R.attr.editTextStyle);
	}  
  
	public CircleEditText(Context context) {  
		this(context,null);
	}  

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);  
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);  
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);  
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);  
		int width;  
		int height ;  
		width = widthSize;  
  		height = heightSize; 
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas){
		int radius = getMeasuredWidth()/2;

		if(filled){
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(solidColor);
			canvas.drawCircle(getScrollX()+radius,getScrollY()+radius,radius-ringWidth,mPaint);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(ringColor);
			mPaint.setStrokeWidth(ringWidth);
			canvas.drawCircle(getScrollX()+radius,getScrollY()+radius,radius-ringWidth/2,mPaint);
		}else{
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(emptyColor);
			canvas.drawCircle(getScrollX()+radius,getScrollY()+radius,radius,mPaint);
		}

		super.onDraw(canvas);
	}

	public void setFilled(){
		if(!filled){
			filled = true;
			invalidate();
		}
	}

	public void clearFilled(){
		filled = false;
		invalidate();
	}
}

