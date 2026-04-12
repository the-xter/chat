package com.thex.chat.chatapi.chat;

public interface CallBackInvoker {
    void callBack(String channel, CallResult callResult, String cometSessionId);
}
