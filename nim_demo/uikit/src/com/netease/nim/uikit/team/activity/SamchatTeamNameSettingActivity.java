package com.netease.nim.uikit.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;

public class SamchatTeamNameSettingActivity extends UI{

    private static final String EXTRA_TID = "EXTRA_TID";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private static final String EXTRA_FIELD = "EXTRA_FIELD";

    // view
    private RelativeLayout titlebar_layout;
    private FrameLayout back_arrow_layout;
    private ImageView back_icon_iv;
    private TextView titlebar_name_tv;
    private FrameLayout sp_save_layout;
    private TextView sp_save_tv;
    private TextView customer_save_tv;
    private ClearableEditTextWithIcon discussion_name_ev;

    // data
    private String teamId;
    private TeamFieldEnum filed;
    private String initialValue;


    /**
     * 修改群某一个属性公用界面
     * @param activity
     * @param teamId
     * @param field
     * @param initialValue
     * @param requestCode
     */
    public static void start(Activity activity, String teamId, TeamFieldEnum field, String initialValue, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, SamchatTeamNameSettingActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        intent.putExtra(EXTRA_DATA, initialValue);
        intent.putExtra(EXTRA_FIELD, field);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 修改群某一个属性公用界面
     * @param context
     * @param teamId
     * @param field
     * @param initialValue
     */
    public static void start(Context context, String teamId, TeamFieldEnum field, String initialValue) {
        Intent intent = new Intent();
        intent.setClass(context, SamchatTeamNameSettingActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        intent.putExtra(EXTRA_DATA, initialValue);
        intent.putExtra(EXTRA_FIELD, field);
        context.startActivity(intent);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samchat_team_name_activity);

		ToolBarOptions options = new ToolBarOptions();
		setToolBar(R.id.toolbar, options);

		parseIntent();
		setupPanel();
    }

    private void parseIntent() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
        filed = (TeamFieldEnum) getIntent().getSerializableExtra(EXTRA_FIELD);
        initialValue = getIntent().getStringExtra(EXTRA_DATA);
    }

	private void setupPanel() {
		discussion_name_ev = findView(R.id.discussion_name);
		titlebar_layout = findView(R.id.titlebar_layout);
		back_arrow_layout = findView(R.id.back_arrow_layout);
		back_icon_iv = findView(R.id.back_icon);
		titlebar_name_tv = findView(R.id.titlebar_name);
		sp_save_layout = findView(R.id.sp_save_layout);
		sp_save_tv = findView(R.id.sp_save);
		customer_save_tv = findView(R.id.customer_save);

		if(NimUIKit.getCallback().getCurrentMode() == ModeEnum.CUSTOMER_MODE.getValue()){
			setTitlebarCustomerMode();
		}else{
			setTitlebarSPMode();
		}

		setupBackArrowClick();
		setupSpSaveClick();
		setupCustomerSaveClick();
		setupEditText();
    }

	private void updateSaveButtonBackground(boolean clickable){
		if(NimUIKit.getCallback().getCurrentMode() == ModeEnum.CUSTOMER_MODE.getValue()){
			customer_save_tv.setEnabled(clickable);
			customer_save_tv.setVisibility(View.VISIBLE);
			sp_save_layout.setVisibility(View.GONE);
			customer_save_tv.setBackgroundResource(clickable?R.drawable.samchat_button_green_active:R.drawable.samchat_button_green_inactive);
		}else{
			sp_save_layout.setEnabled(clickable);
			customer_save_tv.setVisibility(View.GONE);
			sp_save_layout.setVisibility(View.VISIBLE);
			if(clickable){
				sp_save_tv.setTextColor(getResources().getColor(R.color.samchat_color_white));
				sp_save_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
			}else{
				sp_save_tv.setTextColor(getResources().getColor(R.color.samchat_color_grey));
				sp_save_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp_inactive);
			}
		}	
	}

	private void updateSaveButton(Editable s){
		if(s.length()>0){
			updateSaveButtonBackground(true);
		}else{
			updateSaveButtonBackground(false);
		}
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(back_arrow_layout.getWindowToken(), 0);
				finish();
			}
		});
	}

	private void setupSpSaveClick(){
		sp_save_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				complete();
			}
		});
		updateSaveButtonBackground(false);
		sp_save_layout.setEnabled(false);
	}

	private void setupCustomerSaveClick(){
		customer_save_tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				complete();
			}
		});
		updateSaveButtonBackground(false);
		customer_save_tv.setEnabled(false);
	}

	private void setupEditText(){
		discussion_name_ev.setDeleteImage(R.drawable.nim_grey_delete_icon);
		discussion_name_ev.setText(initialValue);
       Editable etext = discussion_name_ev.getText();
		Selection.setSelection(etext, etext.length());
		discussion_name_ev.setAfterTextChangedListener(new ClearableEditTextWithIcon.afterTextChangedListener(){
			@Override
			public void afterTextChangedCallback(Editable s){
				updateSaveButton(s);
			}
		});
		updateSaveButton(etext);
	}

    private void complete() {
		if (TextUtils.isEmpty(discussion_name_ev.getText().toString())) {
			Toast.makeText(this, R.string.not_allow_empty, Toast.LENGTH_SHORT).show();
		} else {
			saveTeamProperty();
		}
	}

    private void saved() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, discussion_name_ev.getText().toString());
        setResult(Activity.RESULT_OK, intent);
        showKeyboard(false);
        finish();
    }

    /**
     * 保存设置
     */
    private void saveTeamProperty() {
        if(teamId == null) { // 讨论组创建时，设置群名称
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DATA, discussion_name_ev.getText().toString());
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            NIMClient.getService(TeamService.class).updateTeam(teamId, filed, discussion_name_ev.getText().toString()).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    Toast.makeText(SamchatTeamNameSettingActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
                    saved();
                }

                @Override
                public void onFailed(int code) {
                    if (code == ResponseCode.RES_TEAM_ENACCESS) {
                        Toast.makeText(SamchatTeamNameSettingActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SamchatTeamNameSettingActivity.this, String.format(getString(R.string.update_failed), code),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onException(Throwable exception) {
                	Toast.makeText(SamchatTeamNameSettingActivity.this, R.string.update_failed,
                                Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }
}

