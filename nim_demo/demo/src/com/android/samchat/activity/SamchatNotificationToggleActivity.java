package com.android.samchat.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.R;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;

import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class SamchatNotificationToggleActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatNotificationToggleActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private SwitchButton reminder_toggle;
	private LinearLayout detail_layout;
	private SwitchButton sound_toggle;
	private SwitchButton vibrate_toggle;

	public static void start(Context context) {
		Intent intent = new Intent(context, SamchatNotificationToggleActivity.class);
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
		setContentView(R.layout.samchat_notification_toggle_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		setupPanel();

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateReminderToggle();
		updateSoundToggle();
		updateVibrateToggle();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupReminderToggle(){
		 reminder_toggle.setOnChangedListener(onReminderToggleListener);
		 
	}
	private void updateReminderToggle() {
		boolean toggle = UserPreferences.getNotificationToggle();
		reminder_toggle.setCheck(toggle);
    }

    private SwitchButton.OnChangedListener onReminderToggleListener = new SwitchButton.OnChangedListener() {
		@Override
		public void OnChanged(View v, final boolean toggleState) {
			UserPreferences.setNotificationToggle(toggleState);
			NIMClient.toggleNotification(toggleState);
			detail_layout.setVisibility(toggleState?View.VISIBLE:View.GONE);
		}
    };

	private void setupSoundToggle(){
		 sound_toggle.setOnChangedListener(onSoundToggleListener);
	}

	private void updateSoundToggle() {
		boolean toggle = UserPreferences.getRingToggle();
       sound_toggle.setCheck(toggle);            
    }

    private SwitchButton.OnChangedListener onSoundToggleListener = new SwitchButton.OnChangedListener() {
		@Override
		public void OnChanged(View v, final boolean toggleState) {
			UserPreferences.setRingToggle(toggleState);
			StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
			config.ring = toggleState;
			UserPreferences.setStatusConfig(config);
			NIMClient.updateStatusBarNotificationConfig(config);
		}
    };

	private void setupVibrateToggle(){
		 vibrate_toggle.setOnChangedListener(onVibrateToggleListener);
	}

	private void updateVibrateToggle() {
		boolean toggle = UserPreferences.getVibrateToggle();
       vibrate_toggle.setCheck(toggle);            
    }

    private SwitchButton.OnChangedListener onVibrateToggleListener = new SwitchButton.OnChangedListener() {
		@Override
		public void OnChanged(View v, final boolean toggleState) {
			UserPreferences.setVibrateToggle(toggleState);
			StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
			config.vibrate = toggleState;
			UserPreferences.setStatusConfig(config);
			NIMClient.updateStatusBarNotificationConfig(config);
		}
    };
	
	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		reminder_toggle = findView(R.id.reminder_toggle);
		detail_layout = findView(R.id.detail_layout);
		sound_toggle = findView(R.id.sound_toggle);
		vibrate_toggle = findView(R.id.vibrate_toggle);

		setupBackArrowClick();
		setupReminderToggle();
		setupSoundToggle();
		setupVibrateToggle();
	}
}


