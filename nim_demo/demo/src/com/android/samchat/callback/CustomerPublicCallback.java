package com.android.samchat.callback;
import com.android.samservice.info.FollowUser;

public interface CustomerPublicCallback {

    void onCustomerPublicLoaded();

    void onItemClick(FollowUser fsp);

    void onDelete();

    void onAdd();
}