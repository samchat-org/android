package com.android.samchat.callback;
import com.android.samservice.info.SendQuestion;

public interface SendQuestionCallback {

    void onSendQuestionLoaded();

    void onItemClick(SendQuestion sq);

    void onDelete(SendQuestion sq);

    void onAdd(SendQuestion sq);
}

