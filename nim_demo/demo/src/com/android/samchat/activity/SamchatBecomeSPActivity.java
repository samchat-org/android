package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.samchat.R;
import android.widget.FrameLayout;



public class SamchatBecomeSPActivity extends Activity {
	private static final String TAG = SamchatBecomeSPActivity.class.getSimpleName();
	
	private FrameLayout back_arrow_layout;
	private TextView become_tv;
	
	public static void start(Context context) {
       Intent intent = new Intent(context, SamchatBecomeSPActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_become_sp_activity);
		setupPanel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = (FrameLayout)findViewById(R.id.back_arrow_layout);
		become_tv = (TextView)findViewById(R.id.become);

		setupBackArrowClick();
		setupBecomeClick();
	}

	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupBecomeClick(){
		become_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SamchatCreateSPStepOneActivity.start(SamchatBecomeSPActivity.this);
				finish();
			}
		});
	}
}







