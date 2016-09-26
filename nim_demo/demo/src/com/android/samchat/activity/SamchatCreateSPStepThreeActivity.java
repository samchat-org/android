package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;

public class SamchatCreateSPStepThreeActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatCreateSPStepThreeActivity.class.getSimpleName();

	private TextView start_textview;

	
	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatCreateSPStepThreeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	protected boolean displayHomeAsUpEnabled() {
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_createspstepthree_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		start_textview = findView(R.id.start);

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



