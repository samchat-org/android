package com.android.samchat.swipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class AutoNormalSwipeRefreshLayout extends SwipeRefreshLayout{
	public AutoNormalSwipeRefreshLayout(Context context) {
		this(context, null);
	}
 
	public AutoNormalSwipeRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
 
    /**
     * �Զ�ˢ��
     */
    public void autoRefresh() {
        try {
            Field mCircleView = SwipeRefreshLayout.class.getDeclaredField("mCircleView");
            mCircleView.setAccessible(true);
            View progress = (View) mCircleView.get(this);
            progress.setVisibility(VISIBLE);
 
            Method setRefreshing = SwipeRefreshLayout.class.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
            setRefreshing.setAccessible(true);
            setRefreshing.invoke(this, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	
}


