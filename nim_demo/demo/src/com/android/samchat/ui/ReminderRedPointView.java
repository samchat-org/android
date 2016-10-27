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

public class ReminderRedPointView extends View {
	private int rrpv_fillColor;
	private int rrpv_circleRadius;

	private int curSelect=0;

	private Paint mPaint;

	public ReminderRedPointView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ReminderRedPointView,defStyle,0);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		if(a != null){
			rrpv_fillColor = a.getColor(R.styleable.ReminderRedPointView_rrpv_fillColor,getResources().getColor(R.color.samchat_color_red));
			rrpv_circleRadius = a.getDimensionPixelSize(R.styleable.ReminderRedPointView_rrpv_circleRadius, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm));
			a.recycle();
		}
		
		mPaint = new Paint();
		mPaint.setColor(rrpv_fillColor);
	}  
  
	public ReminderRedPointView(Context context, AttributeSet attrs) {  
		this(context, attrs,0);
	}  
  
	public ReminderRedPointView(Context context) {  
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
		if(widthMode == MeasureSpec.EXACTLY){  
			width = MeasureSpec.makeMeasureSpec(widthSize,widthMode);  
		}else{
			int desired = (int) (getPaddingLeft() + 2*rrpv_circleRadius+ getPaddingRight());  
			width = MeasureSpec.makeMeasureSpec(desired,widthMode);  
		}  
  
		if(heightMode == MeasureSpec.EXACTLY){  
			height = MeasureSpec.makeMeasureSpec(heightSize,heightMode);   
		}else{
			int desired = (int) (getPaddingTop() + 2*rrpv_circleRadius + getPaddingBottom());  
			height = MeasureSpec.makeMeasureSpec(desired,heightMode);  
		}
		setMeasuredDimension(width, height);  
	}  

	@Override
	protected void onDraw(Canvas canvas){
		canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,rrpv_circleRadius,mPaint);
	}
}