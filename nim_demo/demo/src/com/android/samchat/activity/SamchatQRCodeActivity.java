package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.samchat.SamchatGlobal;
import com.android.samservice.info.ContactUser;
import com.google.zxing.WriterException;
import com.android.samchat.R;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
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
	private RelativeLayout titlebar_layout;
	private ImageView back_icon_iv;
	private TextView titlebar_name_tv;
	private HeadImageView avatar_hv;
	private TextView name_tv;
	private TextView company_tv;
	private TextView category_tv;
	private ImageView qrcode_iv;

	private Bitmap qrcode=null;

	private static final String SHOW_WHICH_INFO = "show_which_info";
	private int show_which_info;
	private static final String CONTACTUSER = "conatct_user";
	private ContactUser user;

	public static void start(Context context, int f, ContactUser u) {
		Intent intent = new Intent(context, SamchatQRCodeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(SHOW_WHICH_INFO,f);
		Bundle bundle = new Bundle();
		bundle.putSerializable(CONTACTUSER, u);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	private void onParseIntent() {
		show_which_info = getIntent().getIntExtra(SHOW_WHICH_INFO,Constants.SHOW_CUSTOMER_INFO);
		user = (ContactUser)getIntent().getSerializableExtra(CONTACTUSER);
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
		titlebar_layout = findView(R.id.titlebar_layout);
		back_icon_iv = findView(R.id.back_icon);
		titlebar_name_tv = findView(R.id.titlebar_name);
		avatar_hv = findView(R.id.avatar);
		name_tv = findView(R.id.name);
		company_tv = findView(R.id.company);
		category_tv = findView(R.id.category);
		qrcode_iv = findView(R.id.qrcode);

		setupBackArrowClick();
		setupTitleBar();

		if(show_which_info == Constants.SHOW_CUSTOMER_INFO){
			name_tv.setText(user.getusername());
			company_tv.setVisibility(View.GONE);
			category_tv.setVisibility(View.GONE);
		}else{
			name_tv.setText(user.getusername());
			company_tv.setVisibility(View.VISIBLE);
			company_tv.setText(user.getcompany_name());
			category_tv.setVisibility(View.VISIBLE);
			category_tv.setText(user.getservice_category());
		}
		avatar_hv.loadBuddyAvatarByUrl(user.getAccount(), user.getAvatar(),80);
		
		int labelWidth = ScreenUtil.screenWidth;
		labelWidth -= ScreenUtil.dip2px(96);
		setupQrCode(user.getunique_id(),labelWidth,labelWidth);
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setTitlebarCustomerMode(){
        titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
    }

    private void setTitlebarSPMode(){
        titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
        back_icon_iv.setImageResource(R.drawable.samchat_arrow_left_sp);
        titlebar_name_tv.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
    }

    private void setupTitleBar(){
        if(SamchatGlobal.isCustomerMode()){
            setTitlebarCustomerMode();
        }else{
            setTitlebarSPMode();
        }
    }

	private void setupQrCode(long unique_id,int width, int height){
        try{
			qrcode = CodeCreator.createQRCode(NimConstants.QRCODE_PREFIX+unique_id,width,height);
			qrcode_iv.setImageBitmap(qrcode);
		}catch (WriterException e){
			e.printStackTrace();
		}
    }


}



