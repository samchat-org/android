package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import com.android.samchat.R;
import com.hp.hpl.sparta.Text;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;

public class SamchatCreateSPStepFourActivity extends Activity {
	private static final String TAG = SamchatCreateSPStepFourActivity.class.getSimpleName();

	private TextView start_textview;

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatCreateSPStepFourActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_createspstepfour_activity);
		setupPanel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		start_textview = (TextView) findViewById(R.id.start);
		setupStartClick();
	}
	
	private void setupStartClick(){
		start_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
}




