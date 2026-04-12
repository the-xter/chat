package com.thex.chat.chatapi.chat;

import org.springframework.stereotype.Component;

@Component
public class DefaultCallBackInvoker implements CallBackInvoker {
    @Override
    public void callBack(String channel, CallResult callResult, String cometSessionId) {

    }
}
