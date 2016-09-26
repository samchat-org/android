package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.android.samchat.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.android.samservice.SamService;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import android.widget.ImageView;
import android.graphics.Bitmap;
import com.karics.library.zxing.encode.CodeCreator;
public class SamchatQRCodeActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatQRCodeActivity.class.getSimpleName();

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private TextView name_textview;
	private TextView category_textview;
	private ImageView qrcode_imageview;
	private TextView samchat_id_textview;
	private TextView phone_textview;

	private Bitmap qrcode=null;

	private static final String FROM = "FROM";
	private int from;

	public static void start(Context context, int f) {
		Intent intent = new Intent(context, SamchatQRCodeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(FROM,f);
		context.startActivity(intent);
	}

	private void onParseIntent() {
		from = getIntent().getIntExtra(FROM,Constants.FROM_CUSTOMER_ACTIVITY_LAUNCH);
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
		setContentView(R.layout.samchat_qrcode_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();

		setupPanel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(qrcode != null && !qrcode.isRecycled()){
			qrcode.recycle();  
		}
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		name_textview = findView(R.id.name);
		category_textview =  findView(R.id.category);
		qrcode_imageview = findView(R.id.qrcode);
		samchat_id_textview = findView(R.id.samchat_id);
		phone_textview = findView(R.id.phone);

		setupBackArrowClick();

		if(from == Constants.FROM_CUSTOMER_ACTIVITY_LAUNCH){
			titlebar_name_textview.setText(getString(R.string.samchat_my_qrcode));
			name_textview.setText(SamService.getInstance().get_current_user().getusername());
			category_textview.setVisibility(View.GONE);
			samchat_id_textview.setText(""+SamService.getInstance().get_current_user().getunique_id());
			phone_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode()+SamService.getInstance().get_current_user().getcellphone());
		}else{
			titlebar_name_textview.setText(getString(R.string.samchat_qr_bussiness_card));
			name_textview.setText(SamService.getInstance().get_current_user().getcompany_name());
			category_textview.setVisibility(View.VISIBLE);
			category_textview.setText(SamService.getInstance().get_current_user().getservice_category());
			samchat_id_textview.setText(""+SamService.getInstance().get_current_user().getunique_id());
			phone_textview.setText("+"+SamService.getInstance().get_current_user().getcountrycode()+SamService.getInstance().get_current_user().getcellphone());
		}
		
		try{
			qrcode = CodeCreator.createQRCode(""+SamService.getInstance().get_current_user().getunique_id());
			qrcode_imageview.setImageBitmap(qrcode);
		}catch (WriterException e){
			e.printStackTrace();
		}

	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}


}



