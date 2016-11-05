package com.android.samchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.samchat.adapter.AdvertisementAdapter;
import com.android.samchat.service.SAMMessageBuilder;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.RcvdAdvSession;
import com.android.samchat.R;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.uikit.common.ui.ptr.PullToRefreshListView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.activity.WatchMessagePictureActivity;
import com.netease.nim.uikit.session.sam_message.SamchatObserver;
import com.android.samservice.SamService;
import com.android.samchat.service.SamDBManager;
import android.widget.FrameLayout;
import com.android.samservice.Constants;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class SamchatRcvdAdvertisementActivity extends UI implements OnKeyListener {
	private static final String TAG = SamchatRcvdAdvertisementActivity.class.getSimpleName();

	private static final int LOAD_ADV_COUNT = 20;

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
		SamDBManager.getInstance().asyncClearReceivedAdvertisementUnreadCount(user.getunique_id());
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

	private void removeItems(Advertisement adv){
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
	}

	private void showLongClickMenu(final Advertisement adv) {
		CustomAlertDialog alertDialog = new CustomAlertDialog(SamchatRcvdAdvertisementActivity.this);
		String title = getString(R.string.samchat_chat);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				onAdvertisementClick(adv);
			}
		});

		title = getString(R.string.samchat_delete);
		alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
			@Override
			public void onClick() {
				SamDBManager.getInstance().asyncDeleteRcvdAdvMessage(adv);
				removeItems(adv);
				refreshAdvertisementList();
			}
		});

       if(adv.gettype() == Constants.ADV_TYPE_TEXT){
			title = getString(R.string.samchat_copy);
			alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
				@Override
				public void onClick() {
					ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					cmb.setText(adv.getcontent());  
				}
			});
		}

		alertDialog.show();
    }

	private void setupAdvertisementListView(){
		items = new ArrayList<>();
		adapter = new AdvertisementAdapter(SamchatRcvdAdvertisementActivity.this, items);
		pull_refresh_list.setAdapter(adapter);
       	pull_refresh_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Advertisement adv = (Advertisement) parent.getAdapter().getItem(position);
				if(adv.gettype() == Constants.ADV_TYPE_PIC){
					IMMessage im = SAMMessageBuilder.createReceivedAdvertisementImageMessage(adv);
					WatchMessagePictureActivity.start(SamchatRcvdAdvertisementActivity.this, im,true);
				}
			}
		});

		pull_refresh_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showLongClickMenu((Advertisement) parent.getAdapter().getItem(position));
				return true;
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
					if(user.getunique_id() != adv.getsender_unique_id()){
						return;
					}
					
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

			SystemClock.sleep(500);

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






