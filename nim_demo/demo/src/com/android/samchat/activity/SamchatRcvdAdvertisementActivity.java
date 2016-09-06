package com.android.samchat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.samchat.adapter.AdvertisementAdapter;
import com.android.samchat.adapter.ContactAdapter;
import com.android.samchat.adapter.ContactUserAdapter;
import com.android.samservice.QuestionInfo;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.RcvdAdvSession;
import com.android.samservice.info.SendQuestion;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.demo.config.preference.UserPreferences;
import com.netease.nim.demo.contact.ContactHttpClient;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshListView;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.android.samchat.cache.SamchatDataCacheManager;
import com.android.samservice.SamService;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.android.samchat.service.SamDBManager;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.android.samservice.Constants;
import com.android.samchat.factory.UuidFactory;
import com.android.samservice.SMCallBack;
import com.android.samchat.service.ErrorString;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.android.samservice.HttpCommClient;
public class SamchatRcvdAdvertisementActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRcvdAdvertisementActivity.class.getSimpleName();

	private static final int LOAD_ADV_COUNT = 3;

	private FrameLayout back_arrow_layout;
	private TextView titlebar_name_textview;
	private PullToRefreshListView pull_refresh_list;

	private List<Advertisement> items;
	private AdvertisementAdapter adapter;

	private FollowedSamPros user;
	
	public static void start(Context context,FollowedSamPros fsp) {
		Intent intent = new Intent(context, SamchatRcvdAdvertisementActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable("fsp", fsp);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	private void onParseIntent() {
		user = (FollowedSamPros)getIntent().getSerializableExtra("fsp");
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
		setContentView(R.layout.samchat_rcvdadvertisement_activity);

		ToolBarOptions options = new ToolBarOptions();
		options.isNeedNavigate = false;
		options.logoId = R.drawable.actionbar_white_logo_space;
		setToolBar(R.id.toolbar, options);

		onParseIntent();
		setupPanel();

		registerObservers(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		registerObservers(false);
	}

	private void setupPanel() {
		back_arrow_layout = findView(R.id.back_arrow_layout);
		titlebar_name_textview = findView(R.id.titlebar_name);
		pull_refresh_list = findView(R.id.pull_refresh_list);

        pull_refresh_list.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pull_refresh_list.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				LogUtil.i(TAG,"pull refresh list refreshing");
				pull_refresh_list.setRefreshing();
			}
		});

		titlebar_name_textview.setText(user.getusername());
		setupBackArrowClick();
		setupAdvertisementListView();
	}
	
	private void setupBackArrowClick(){
		back_arrow_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setupAdvertisementListView(){
		items = new ArrayList<>();
		adapter = new AdvertisementAdapter(SamchatRcvdAdvertisementActivity.this, items);
		pull_refresh_list.setAdapter(adapter);
       pull_refresh_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Advertisement adv = (Advertisement) parent.getAdapter().getItem(position);
				onAdvertisementClick(adv);
			}
		});

		pull_refresh_list.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
		pull_refresh_list.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
				LogUtil.i(TAG,"loading more advertisement by pull down");
				if(items == null || items.size() == 0){
					new GetDataTask().execute(0L);
				}else{
					new GetDataTask().execute(items.get(0).getpublish_timestamp());
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
				LogUtil.i(TAG,"not support pull up ...");
			}
		});
	}

	
	

	private void notifyDataAdvertisement() {
		adapter.notifyDataSetChanged();
	}

	private void refreshAdvertisementList(){
		sortAdvertisement(items);
		notifyDataAdvertisement();
	}

	private void sortAdvertisement(List<Advertisement> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, comp);
		for(Advertisement r: list){
			LogUtil.e("test","time:"+r.getpublish_timestamp()+" content:"+r.getcontent());
		}
	}

	private static Comparator<Advertisement> comp = new Comparator<Advertisement>() {
		@Override
		public int compare(Advertisement o1, Advertisement o2) {
			long time = o1.getpublish_timestamp() - o2.getpublish_timestamp();
			return time == 0 ? 0 : (time > 0 ? 1 : -1);
		}
	};

/******************************************Data Flow Control***********************************************************/
/***********************************Observers*****************************************************/
	private void updateItems(Advertisement adv){
		int index = -1;
		for(int i = 0; i<items.size();i++){
			if(items.get(i).getadv_id() == adv.getadv_id()){
				index = i;
				break;
			}
		}
		
		if(index >= 0){
			items.remove(index);
		}

		items.add(adv);
	}

	private void registerObservers(boolean register) {
		SamDBManager.getInstance().registerRcvdAdvObserver(rcvdAdvObserver,  register);
	}

	SamchatObserver<Advertisement> rcvdAdvObserver = new SamchatObserver <Advertisement>(){
		@Override
		public void onEvent(final Advertisement adv){
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					LogUtil.i(TAG,"received new adv by rcvdAdvObserver ");
					updateItems(adv);
					refreshAdvertisementList();
					pull_refresh_list.getRefreshableView().setSelection((adapter.getCount() - 1));
				}
			}, 0);
		}
	};

	private void onAdvertisementClick(Advertisement adv){
		if(adv.getresponse() == Constants.ADV_NOT_RESPONSED ){
			LogUtil.i(TAG,"launch p2p session with adv:"+adv.getadv_id());
			SamDBManager.getInstance().asyncInsertRcvdAdvMessage(adv);
			SessionHelper.startP2PSessionWithRcvdAdv(SamchatRcvdAdvertisementActivity.this,""+adv.getsender_unique_id(), adv.getadv_id());
		}else{
			LogUtil.i(TAG,"launch p2p session without adv");
			SessionHelper.startP2PSession(SamchatRcvdAdvertisementActivity.this, ""+adv.getsender_unique_id());
		}
	}


	private class GetDataTask extends AsyncTask<Long, Integer, List<Advertisement>>
	{

		@Override
		protected List<Advertisement> doInBackground(Long... params)
		{
			long time = params[0];
			RcvdAdvSession session = SamService.getInstance().getDao().query_RcvdAdvSession_db(user.getunique_id());
			List<Advertisement> load = new ArrayList<>();
			if(session != null){
				load =  SamService.getInstance().getDao().query_RcvdAdv_db_by_timestamp(session.getname(), time, LOAD_ADV_COUNT);
			}

			return load;
		}

		@Override
		protected void onPostExecute(List<Advertisement> result)
		{
			items.addAll(result);
			refreshAdvertisementList();
			pull_refresh_list.onRefreshComplete();
		}
	}
	
}






