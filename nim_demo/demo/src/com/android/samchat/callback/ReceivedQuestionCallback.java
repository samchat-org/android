package com.android.samchat.callback;
import com.android.samservice.info.ReceivedQuestion;

public interface ReceivedQuestionCallback {

    void onReceivedQuestionLoaded();

    void onItemClick(ReceivedQuestion rq);

    void onDelete(ReceivedQuestion rq);

    void onAdd(ReceivedQuestion rq);
}
