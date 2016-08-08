package com.netease.nim.uikit.session.sam_message;

import java.io.Serializable;

public interface SamchatObserver<T> extends Serializable {
    void onEvent(T var1);
}

