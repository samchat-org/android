package com.android.samchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.adapter.PlaceInfoAdapter;
import com.android.samchat.adapter.SimpleListAdapter;
import com.android.samchat.common.SCell;
import com.android.samchat.factory.LocationFactory;
import com.android.samservice.info.QuestionInfo;
import com.android.samservice.info.PlacesInfo;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.Toast;

import com.android.samservice.Constants;
import com.android.samservice.callback.SMCallBack;
import com.android.samchat.service.ErrorString;
import com.android.samservice.HttpCommClient;
import com.netease.nim.uikit.session.helper.SendImageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

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







