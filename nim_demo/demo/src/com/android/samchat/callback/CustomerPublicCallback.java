package com.android.samchat.callback;

import com.android.samservice.info.FollowedSamPros;

public interface CustomerPublicCallback {

    void onCustomerPublicLoaded();

    void onItemClick(FollowedSamPros fsp);

    void onDelete();

    void onAdd();
}