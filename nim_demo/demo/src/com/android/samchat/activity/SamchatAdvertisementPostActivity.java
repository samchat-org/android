package com.android.samchat.activity;

import android.app.Activity;
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

public class SamchatAdvertisementPostActivity extends Activity {
	private static final String TAG = SamchatAdvertisementPostActivity.class.getSimpleName();
	private static final int CONFIRM_ID_IMAGE_SELECTED=100;
	public static final String MIME_JPEG = "image/jpeg";
	public static final String JPG = ".jpg";
	public static final String EXTRA_IS_TEXT="EXTRA_IS_TEXT";
	public static final String EXTRA_TEXT="EXTRA_TEXT";
	
	private FrameLayout back_arrow_layout;
	private TextView post_tv;
	private ImageView select_image_iv;
	private EditText adv_text_ev;
	private ImageViewEx image_show_iv;

	private boolean ready_post = false;
	private String adv_text;
	private Intent adv_image_intent = null;
	
	public static void startActivityForResult(Activity activity, TFragment fragment, int requestCode) {
		Intent intent = new Intent(activity, SamchatAdvertisementPostActivity.class);
       fragment.startActivityForResult(intent, requestCode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_advertisement_post_activity);
		setupPanel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupPanel() {
		back_arrow_layout = (FrameLayout)findViewById(R.id.back_arrow_layout);
		post_tv = (TextView)findViewById(R.id.post);
		select_image_iv = (ImageView)findViewById(R.id.select_image);
		adv_text_ev = (EditText) findViewById(R.id.adv_text);
		image_show_iv = (ImageViewEx) findViewById(R.id.image_show);

		setupBackArrowClick();
		setupPostClick();
		setupTextEditClick();
		setupImageSelectClick();
	}

	private TextWatcher text_textWatcher = new TextWatcher() {
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			ready_post = adv_text_ev.getText().toString().trim().length()>0;
			updatePost();
			if(ready_post){
				adv_text = adv_text_ev.getText().toString().trim();
				adv_text_ev.setVisibility(View.VISIBLE);
				image_show_iv.setVisibility(View.GONE);
			}
		}
	};

	private void setupTextEditClick(){
		adv_text_ev.addTextChangedListener(text_textWatcher);	
	}

	private void setupImageSelectClick(){
		select_image_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				launchAvatarActivity();	
			}
		});
	}

	private String tempFile() {
		String filename = StringUtil.get32UUID() + JPG;
		return StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
	}
	
	private void launchAvatarActivity(){
		PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
		option.titleResId = R.string.samchat_album;
		option.multiSelect = false;
		option.multiSelectMaxCount = 1;
		option.crop = false;
		option.cropOutputImageWidth = 720;
		option.cropOutputImageHeight = 720;
		option.outputPath = tempFile();
		int from = PickImageActivity.FROM_LOCAL;
		PickImageActivity.start(SamchatAdvertisementPostActivity.this, CONFIRM_ID_IMAGE_SELECTED, from, option.outputPath, option.multiSelect,
                            option.multiSelectMaxCount, false, false, 0, 0);
	}

    private void showImageAfterSelfImagePicker(final Intent data) {
		adv_image_intent = data;
		SendImageHelper.showImageAfterSelfImagePicker(SamchatAdvertisementPostActivity.this, data, new SendImageHelper.Callback() {
			@Override
			public void sendImage(File file, boolean isOrig) {
				adv_text = null;
				adv_text_ev.setVisibility(View.GONE);
				image_show_iv.setVisibility(View.VISIBLE);
				image_show_iv.load(("file://"+file.getAbsolutePath()));
				ready_post = true;
				updatePost();	
			}
		});
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode == CONFIRM_ID_IMAGE_SELECTED){
			if(resultCode == Activity.RESULT_OK){
				if (data == null) {
					Toast.makeText(SamchatAdvertisementPostActivity.this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
					return;
				}
				showImageAfterSelfImagePicker(data);
			}
		}
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED, null);
				finish();
			}
		});
	}

	private void updatePost(){
		if(ready_post){
			post_tv.setEnabled(true);
			post_tv.setTextColor(getResources().getColor(R.color.color_white_ffffffff));
		}else{
			post_tv.setEnabled(false);
			post_tv.setTextColor(getResources().getColor(R.color.color_grey_d8d2e2));
		}
	}

	private void setupPostClick(){
		updatePost();
		post_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(adv_image_intent != null){
					setResult(RESULT_OK, adv_image_intent);
					finish();
				}else if(!TextUtils.isEmpty(adv_text)){
					Intent data = new Intent();
					data.putExtra(EXTRA_IS_TEXT, true);
					data.putExtra(EXTRA_TEXT,adv_text);
					setResult(RESULT_OK, data);
					finish();
				}
			}
		});
	}


}





