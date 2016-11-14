package com.netease.nim.uikit.session.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.fragment.MessageFragment;

import java.util.List;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public abstract class BaseMessageActivity extends UI {
    private final int BASIC_PERMISSION_REQUEST_CODE = 100;
    /*SAMC_BEGIN(support mode setting for p2p activity)*/
    protected int mode;
    protected long question_id;
    protected long adv_id;
    /*SAMC_BEGIN(support mode setting for p2p activity)*/
    protected String sessionId;

    private SessionCustomization customization;

    private MessageFragment messageFragment;

    protected abstract MessageFragment fragment();
    protected abstract int getContentViewId();
    protected abstract void initToolBar();

	private void requestBasicPermission() {
		MPermission.with(BaseMessageActivity.this)
			.addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
			.permissions(
				Manifest.permission.RECORD_AUDIO,
				Manifest.permission.CAMERA,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			)
			.request();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionSuccess(){
		//Toast.makeText(this, getString(R.string.samchat_permission_grant), Toast.LENGTH_SHORT).show();
	}

	@OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_message), Toast.LENGTH_SHORT).show();
	}

	@OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
	public void onBasicPermissionNeverAskAgainFailed(){
		Toast.makeText(this, getString(R.string.samchat_permission_refused_message), Toast.LENGTH_SHORT).show();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBasicPermission();
				
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        setContentView(getContentViewId());
        parseIntent();
        initToolBar();
        if (customization != null) {
            addRightCustomViewOnActionBar(this, customization.buttons);
        }

        messageFragment = (MessageFragment) switchContent(fragment());
    }

    @Override
    public void onBackPressed() {
        if (messageFragment == null || !messageFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (messageFragment != null) {
            messageFragment.onActivityResult(requestCode, resultCode, data);
        }

        if (customization != null) {
            customization.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private void parseIntent() {
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        customization = (SessionCustomization) getIntent().getSerializableExtra(Extras.EXTRA_CUSTOMIZATION);
        /*SAMC_BEGIN(support mode setting for p2p activity)*/
        mode = getIntent().getIntExtra(Extras.EXTRA_MODE,0);
        question_id = getIntent().getLongExtra(Extras.EXTRA_QUESTIONID,0L);
        adv_id = getIntent().getLongExtra(Extras.EXTRA_ADVID,0L);
        /*SAMC_BEGIN(support mode setting for p2p activity)*/
    }

    // 添加action bar的右侧按钮及响应事件
    private void addRightCustomViewOnActionBar(UI activity, List<SessionCustomization.OptionsButton> buttons) {
        if (buttons == null || buttons.size() == 0) {
            return;
        }

        Toolbar toolbar = getToolBar();
        if (toolbar == null) {
            return;
        }

        LinearLayout view = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.nim_action_bar_custom_view, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        for (final SessionCustomization.OptionsButton button : buttons) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(button.iconId);
            if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
                imageView.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
            }else{
                imageView.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
            }
            imageView.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.onClick(BaseMessageActivity.this, v, sessionId);
                }
            });
            view.addView(imageView, params);
        }

        toolbar.addView(view, new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER));
    }

	public void clearMessageList(){
		messageFragment.clearMessageList();
	}

}
