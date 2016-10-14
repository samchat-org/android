package com.netease.nim.demo.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.samchat.SamchatGlobal;
import com.android.samchat.fragment.SamchatPublicListFragment;
import com.android.samchat.type.ModeEnum;
import com.android.samchat.R;
import com.netease.nim.demo.common.ui.viewpager.FadeInOutPageTransformer;
import com.netease.nim.demo.common.ui.viewpager.PagerSlidingTabStrip;
import com.netease.nim.demo.main.adapter.MainTabPagerAdapter;
import com.netease.nim.demo.main.helper.SystemMessageUnreadManager;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderItem;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
/*SAMC_BEGIN(......)*/
import com.netease.nim.demo.main.activity.MainActivity;
/*SAMC_END(......)*/

/**
 * 云信主界面（导航页）
 */
public class HomeFragment extends TFragment implements OnPageChangeListener, ReminderManager.UnreadNumChangedCallback {

    private PagerSlidingTabStrip tabs;

    private ViewPager pager;

    private int scrollState;

    private MainTabPagerAdapter adapter;

    private View rootView;

    public HomeFragment() {
        setContainerId(R.id.welcome_container);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViews();
        setupPager();
        setupTabs();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TO TABS
        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        // TO ADAPTER
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
        // TO TABS
        tabs.onPageSelected(position);

        selectPage(position);

        enableMsgNotification(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TO TABS
        tabs.onPageScrollStateChanged(state);

        scrollState = state;

        selectPage(pager.getCurrentItem());
    }

    private void selectPage(int page) {
        // TO PAGE
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
            /*SAMC_BEGIN(change title and icon when switch)*/
			  ((MainActivity)getActivity()).setCurrentPostition(page);
            ((MainActivity)getActivity()).refreshToolBar(page);
            /*SAMC_END(change title and icon when switch)*/
        }
    }

    public void switchTab(int tabIndex, String params) {
        pager.setCurrentItem(tabIndex);
    }

    /**
     * get tabs instance and viewpager instance
     */
    private void findViews() {
        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
    }

    @Override
    public void onResume() {
        super.onResume();
        enableMsgNotification(false);
        //quitOtherActivities();
    }

    @Override
    public void onPause() {
        super.onPause();
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
    }

    public boolean onBackPressed() {
        /*SAMC_BEGIN()*/
        if(SamchatGlobal.getmode() == ModeEnum.SP_MODE &&
			((MainActivity)getActivity()).getCurrentPostition() == MainTab.SAMCHAT_PUBLIC.tabIndex){
           return ((SamchatPublicListFragment)adapter.getItem(MainTab.SAMCHAT_PUBLIC.tabIndex)).onBackPressed();
        }
        /*SAMC_END()*/
        return false;
    }

    public boolean onClick(View v) {
        return true;
    }

    /**
     * 设置viewPager
     */
    private void setupPager() {
        // CACHE COUNT
        //view pager adapter create
        adapter = new MainTabPagerAdapter(getFragmentManager(), getActivity(), pager);
        //view pager set cache count which will be pre-cached before fragment selected 
        pager.setOffscreenPageLimit(adapter.getCacheCount());
        // page swtich animation
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        // ADAPTER
        pager.setAdapter(adapter);
        // Most import function which will handle all page change action
        // including onPageScrolled/onPageSelected/onPageScrollStateChanged
        pager.setOnPageChangeListener(this);
    }

    /**
     * 设置tab条目
     */
    private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {
            @Override
            public int getTabLayoutResId(int position) {
                /*SAMC_BEGIN(change to samchat tab layout)*/
                return R.layout.samchat_tab_layout_main;
                /*SAMC_END(change to samchat tab layout)*/
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnTabClickListener(adapter);
        tabs.setOnTabDoubleTapListener(adapter);
    }

    private void enableMsgNotification(boolean enable) {
        /*SAMC_BEGIN(change Msg notification for samchat chat tab)*/
        boolean msg = (pager.getCurrentItem() != MainTab.SAMCHAT_CHAT.tabIndex/*MainTab.RECENT_CONTACTS.tabIndex*/);
        /*SAMC_END(change Msg notification for samchat chat tab)*/
        if (enable | msg) {
            /**
             * 设置最近联系人的消息为已读
             *
             * @param account,    聊天对象帐号，或者以下两个值：
             *                    {@link #MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
             *                    {@link #MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
             */
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 未读消息数量观察者实现
     */
    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        MainTab tab = MainTab.fromReminderId(item.getId());
        if (tab != null) {
            tabs.updateTab(tab.tabIndex, item);
        }
    }


    /**
     * 注册/注销系统消息未读数变化
     *
     * @param register
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver,
                register);
    }

    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unreadCount);
            ReminderManager.getInstance().updateContactUnreadNum(unreadCount);
        }
    };

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }


	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //adapter.getItem(MainTab.SAMCHAT_PUBLIC.tabIndex).onActivityResult(requestCode, resultCode, data);
	}
}