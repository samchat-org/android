package com.netease.nim.uikit.recent.viewholder;

import android.content.Context;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.recent.RecentContactsCallback;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.List;

public class SamchatRecentContactAdapter extends TAdapter<RecentContact> {

    private RecentContactsCallback callback;
    private int mode;

    public void setmode(int mode){
        this.mode = mode;
    }

    public int getmode(){
        return this.mode;
    }

    public SamchatRecentContactAdapter(Context context, List<RecentContact> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }

    public RecentContactsCallback getCallback() {
        return callback;
    }

    public void setCallback(RecentContactsCallback callback) {
        this.callback = callback;
    }
}

