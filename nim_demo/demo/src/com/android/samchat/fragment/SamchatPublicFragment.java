package com.android.samchat.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nim.demo.main.activity.MainActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.netease.nim.demo.R;
import com.android.samchat.callback.SendQuestionCallback;
import android.widget.LinearLayout;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.adapter.ReceivedQuestionAdapter;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.adapter.SendQuestionAdapter;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.Constants;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import com.android.samchat.type.ModeEnum;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import com.android.samservice.info.FollowUser;
import com.android.samchat.adapter.FollowedSPAdapter;
import com.android.samchat.callback.CustomerPublicCallback;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.SamProsUser;
import com.netease.nim.uikit.contact.core.query.TextComparator;
/**
 * Main Fragment in SamchatPublicListFragment
 */
public class SamchatPublicFragment extends TFragment {
	/*customer mode*/
	//view
	private LinearLayout customer_public_layout;
	private TextView search;
	private ListView customer_public_list;
	
	//data
	private List<FollowUser> followedSPs;
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
		search = (TextView) findView(R.id.search);
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

	private void initFollowedSPList(){
		followedSPs = new ArrayList<FollowUser>();
		fspAdapter = new FollowedSPAdapter(getActivity(),followedSPs);
		customer_public_list.setAdapter(fspAdapter);
		customer_public_list.setItemsCanFocus(true);
		customer_public_list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (cpcallback != null) {
					FollowUser fsp = (FollowUser) parent.getAdapter().getItem(position);
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

	private List<FollowUser> loadedFollowSPs;
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
				
				List<FollowedSamPros> sps= SamService.getInstance().getDao().query_FollowList_db_All();
				LogUtil.e("test","query follow list sps:"+sps.size());
				loadedFollowSPs = new ArrayList<FollowUser>();
				for(FollowedSamPros sp : sps){
					SamProsUser sampros = SamService.getInstance().getDao().query_SamProsUser_db_by_unique_id(sp.getunique_id());
					if(sampros != null){
						FollowUser fuser = new FollowUser(sp.getfavourite_tag(),sp.getblock_tag());
						fuser.setunique_id(sampros.getunique_id());
						fuser.setusername(sampros.getusername());
						fuser.setavatar_thumb(sampros.getavatar());
						fuser.setavatar_original(sampros.getavatar_original());
						fuser.setcompany_name(sampros.getcompany_name());
						fuser.setservice_category(sampros.getservice_category());
						fuser.setservice_description(sampros.getservice_category());
						loadedFollowSPs.add(fuser);
					}
				}
				LogUtil.e("test","loadedFollowSPs:"+loadedFollowSPs.size());
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

	private void sortFollowedSPs(List<FollowUser> list) {
		if (list.size() == 0) {
			return;
		}
		Collections.sort(list, fspcomp);
	}

	private static Comparator<FollowUser> fspcomp = new Comparator<FollowUser>() {
		@Override
		public int compare(FollowUser o1, FollowUser o2) {
			return TextComparator.compare(o1.getusername(),o2.getusername());
		}
	};

/*************************************Service Provide Mode***************************/

}


