package com.android.samchat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.android.samchat.activity.SamchatContactUserNameCardActivity;
import com.android.samchat.activity.SamchatContactUserSPNameCardActivity;
import com.android.samchat.cache.SamchatUserInfoCache;
import com.android.samchat.callback.ContactCallback;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.DemoCache;
import com.android.samchat.R;
import com.netease.nim.demo.contact.activity.BlackListActivity;
import com.netease.nim.demo.main.activity.SystemMessageActivity;
import com.netease.nim.demo.main.activity.TeamListActivity;
import com.netease.nim.demo.main.helper.SystemMessageUnreadManager;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderId;
import com.netease.nim.demo.main.reminder.ReminderItem;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.contact.ContactsCustomization;
import com.netease.nim.uikit.contact.ContactsFragment;
import com.netease.nim.uikit.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.contact.core.item.ItemTypes;
import com.netease.nim.uikit.contact.core.viewholder.AbsContactViewHolder;

import java.util.ArrayList;
import java.util.List;
import com.netease.nim.demo.main.fragment.MainTabFragment;

public class SamchatContactListFragment extends MainTabFragment {

    private SamchatContactFragment fragment;

    public SamchatContactListFragment() {
        setContainerId(MainTab.SAMCHAT_CONTACT.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        onCurrent(); 
    }

    @Override
    protected void onInit() {
        addSamchatContactFragment();  
    }

	private void addSamchatContactFragment() {
		fragment = new SamchatContactFragment();
		fragment.setContainerId(R.id.samchat_contact_fragment);

		UI activity = (UI) getActivity();

		fragment = (SamchatContactFragment) activity.addFragment(fragment);
		fragment.setContactCallback(new ContactCallback() {
			@Override
			public void onLoaded(){

			}

			@Override
			public void onItemClick(Contact ui){
				//SessionHelper.startP2PSession(getActivity(), ""+ui.getunique_id());
				ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ui.getunique_id());
				if(user != null){
					SamchatContactUserSPNameCardActivity.start(getActivity(), user);
				}
			}

			@Override
			public void onDelete(){

			}

			@Override
			public void onAdd(){

			}
		});

		fragment.setCustomerCallback(new ContactCallback() {
			@Override
			public void onLoaded(){

			}

			@Override
			public void onItemClick(Contact ui){
				//SessionHelper.startP2PSession(getActivity(), ""+ui.getunique_id());
				ContactUser user = SamchatUserInfoCache.getInstance().getUserByUniqueID(ui.getunique_id());
				if(user != null){
					SamchatContactUserNameCardActivity.start(getActivity(), user);
				}
			}

			@Override
			public void onDelete(){

			}

			@Override
			public void onAdd(){

			}
		});
    }
}

