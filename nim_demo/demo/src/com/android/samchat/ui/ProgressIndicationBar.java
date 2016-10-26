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

public class ProgressIndicationBar extends View {
	private int defaultColor;
	private int selectColor;
	private int circleRadius;
	private int lineWidth;
	private int lineHeight;

	private int curSelect=0;

	private Paint paintCircleSelected;
	private Paint paintCircleUnSelected;
	private Paint paintLine;

	public ProgressIndicationBar(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressIndicationBar,defStyle,0);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		if(a != null){
			defaultColor = a.getColor(R.styleable.ProgressIndicationBar_defaultColor,getResources().getColor(R.color.samchat_color_light_grey));
			selectColor = a.getColor(R.styleable.ProgressIndicationBar_selectColor,getResources().getColor(R.color.samchat_color_green));
			circleRadius = a.getDimensionPixelSize(R.styleable.ProgressIndicationBar_circleRadius, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, dm));
			lineWidth = a.getDimensionPixelSize(R.styleable.ProgressIndicationBar_lineWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm));
			lineHeight = a.getDimensionPixelSize(R.styleable.ProgressIndicationBar_lineHeight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm));
			curSelect = a.getInteger(R.styleable.ProgressIndicationBar_curSelect, 0);
			a.recycle();
		}
		
		paintCircleSelected = new Paint();
		paintCircleUnSelected = new Paint();
		paintLine = new Paint();

		paintCircleSelected.setColor(selectColor);
		
		paintCircleUnSelected.setColor(defaultColor);
		
		paintLine.setColor(defaultColor);
		paintLine.setStrokeWidth((float)lineHeight);
		
	}  
  
	public ProgressIndicationBar(Context context, AttributeSet attrs) {  
		this(context, attrs,0);
	}  
  
	public ProgressIndicationBar(Context context) {  
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
			width = widthSize;  
		}else{
			int desired = (int) (getPaddingLeft() + circleRadius*2*3+lineWidth*2+ getPaddingRight());  
			width = desired;//MeasureSpec.makeMeasureSpec(desired,widthMode);  
		}  
  
		if(heightMode == MeasureSpec.EXACTLY){  
			height = heightSize; 
		}else{
			int desired = (int) (getPaddingTop() + 2*circleRadius + getPaddingBottom());  
			height = desired;//MeasureSpec.makeMeasureSpec(desired,heightMode);  
		}
		setMeasuredDimension(width, height);  
	}  

	@Override
	protected void onDraw(Canvas canvas){
	
		if(curSelect == 0){
			canvas.drawCircle(circleRadius,circleRadius,circleRadius,paintCircleSelected);
		}else{
			canvas.drawCircle(circleRadius,circleRadius,circleRadius,paintCircleUnSelected);
		}
		canvas.drawLine(2*circleRadius,circleRadius,2*circleRadius+lineWidth,circleRadius,paintLine);

		if(curSelect == 1){
			canvas.drawCircle(3*circleRadius+lineWidth,circleRadius,circleRadius,paintCircleSelected);
		}else{
			canvas.drawCircle(3*circleRadius+lineWidth,circleRadius,circleRadius,paintCircleUnSelected);
		}
		canvas.drawLine(4*circleRadius+lineWidth,circleRadius,4*circleRadius+2*lineWidth,circleRadius,paintLine);

		if(curSelect == 2){
			canvas.drawCircle(5*circleRadius+2*lineWidth,circleRadius,circleRadius,paintCircleSelected);
		}else{
			canvas.drawCircle(5*circleRadius+2*lineWidth,circleRadius,circleRadius,paintCircleUnSelected);
		}
	}

	public void setCurSelect(int index){
		curSelect = index;
	}
}