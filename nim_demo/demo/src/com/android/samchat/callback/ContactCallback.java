package com.android.samchat.callback;
import com.android.samservice.info.Contact;

public interface ContactCallback {

    void onLoaded();

    void onItemClick(Contact ui);

    void onDelete();

    void onAdd();
}