package com.android.samchat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.TextView;

import com.android.samchat.activity.SamchatSearchPublicActivity;
import com.android.samchat.cache.FollowDataCache;
import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.netease.nim.demo.R;
import android.widget.LinearLayout;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.core.query.TextComparator;
import android.view.View.OnClickListener;

/**
 * Main Fragment in SamchatPublicListFragment
 */
public class SamchatPublicFragment extends TFragment {
	/*customer mode*/
	//view
	private LinearLayout customer_public_layout;
	private TextView search_textview;
	private ListView customer_public_list;
	
	//data
	private List<FollowedSamPros> followedSPs;
	private FollowedSPAdapter fspAdapter;
	private boolean followedSPLoaded = false;
	private CustomerPublicCallback cpcallback;
	
	/*sp mode*/
	//view
	private LinearLayout sp_public_layout;
	
	//observer and broadcast
	private boolean isBroadcastRegistered = false;
	private BroadcastReceiver broadcastReceiver;
	private LocalBroadcastManager broadcastManager;

	private void switchMode(ModeEnum to){
		if(to == ModeEnum.SP_MODE){
			
		}else{

		}
	}

	private void registerBroadcastReceiver() {
		broadcastManager = LocalBroadcastManager.getInstance(getActivity());
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_SWITCH_MODE);
		filter.addAction(Constants.BROADCAST_FOLLOWEDSP_UPDATE);

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Constants.BROADCAST_SWITCH_MODE)){
					int to = intent.getExtras().getInt("to");
					if(to == ModeEnum.valueOfType(ModeEnum.CUSTOMER_MODE)){
						customer_public_layout.setVisibility(View.VISIBLE);
						sp_public_layout.setVisibility(View.GONE);
					}else{
						customer_public_layout.setVisibility(View.GONE);
						sp_public_layout.setVisibility(View.VISIBLE);
					}
					((MainActivity)getActivity()).dimissSwitchProgress();
				}else if(intent.getAction().equals(Constants.BROADCAST_FOLLOWEDSP_UPDATE)){
					loadedFollowSPs= FollowDataCache.getInstance().getMyFollowSPsList();
					onFollowedSPsLoaded();
				}
			}
		};
		
		broadcastManager.registerReceiver(broadcastReceiver, filter);
	}
		

	
	private void unregisterBroadcastReceiver(){
	    broadcastManager.unregisterReceiver(broadcastReceiver);
	}

	public SamchatPublicFragment(){
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.samchat_public_fragment_layout, container, false);
	}

    @Override
    public void onDestroyView(){
        unregisterBroadcastReceiver();
        super.onDestroyView();
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findViews();
		setupSearchClick();
		


		
		initFollowedSPList();
		
		LoadFollowedSPs(true);

		registerBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void findViews() {
		//customer mode views
		customer_public_layout = (LinearLayout) findView(R.id.customer_public_layout);
		search_textview = (TextView) findView(R.id.search);
		customer_public_list = (ListView) findView(R.id.customer_public_list);
		//sp mode views
		sp_public_layout = (LinearLayout) findView(R.id.sp_public_layout);

		if(SamchatGlobal.getmode()== ModeEnum.CUSTOMER_MODE){
			customer_public_layout.setVisibility(View.VISIBLE);
			sp_public_layout.setVisibility(View.GONE);
		}else{
			customer_public_layout.setVisibility(View.GONE);
			sp_public_layout.setVisibility(View.VISIBLE);
		}
    }

	private void setupSearchClick(){
		LogUtil.e("test","setupSearchClick");
		search_textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LogUtil.e("test","click called...");
				SamchatSearchPublicActivity.start(getActivity());
			}
		});
	}

	private void initFollowedSPList(){
		followedSPs = new ArrayList<FollowedSamPros>();
		fspAdapter = new FollowedSPAdapter(getActivity(),followedSPs);
		customer_public_list.setAdapter(fspAdapter);
		customer_public_list.setItemsCanFocus(true);
		customer_public_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (cpcallback != null) {
					FollowedSamPros fsp = (FollowedSamPros) parent.getAdapter().getItem(position);
					if(fsp != null){
						cpcallback.onItemClick(fsp);
					}
				}
			}
		});

		customer_public_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
			}
		});
		
	}

	private List<FollowedSamPros> loadedFollowSPs;
	private void LoadFollowedSPs(boolean delay){
		if(followedSPLoaded){
			return;
		}

		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(followedSPLoaded || getActivity() == null || ((UI)getActivity()).isDestroyedCompatible()){
					return;
				}
				
				loadedFollowSPs= SamService.getInstance().getDao().query_FollowList_db_All();
				followedSPLoaded = true;
				if(isAdded()){
					onFollowedSPsLoaded();
				}
			}
		}, delay ? 250 : 0);

	}

	private void onFollowedSPsLoaded(){
		followedSPs.clear();
		if(loadedFollowSPs!=null){
			followedSPs.addAll(loadedFollowSPs);
			loadedFollowSPs = null;
		}

		refreshFollowedSPsList();
	}

	private void notifyDataSetChangedFSP() {
		fspAdapter.notifyDataSetChanged();
	}

	private void refreshFollowedSPsList(){
		sortFollowedSPs(followedSPs);
		notifyDataSetChangedFSP();
	}

	public void setCpCallback(CustomerPublicCallback callback) {
		cpcallback = callback;
	}

	private void sortFollowedSPs(List<FollowedSamPros> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, fspcomp);
	}

	private static Comparator<FollowedSamPros> fspcomp = new Comparator<FollowedSamPros>() {
		@Override
		public int compare(FollowedSamPros o1, FollowedSamPros o2) {
			return TextComparator.compare(o1.getusername(),o2.getusername());
		}
	};

/*************************************Service Provide Mode***************************/

}


