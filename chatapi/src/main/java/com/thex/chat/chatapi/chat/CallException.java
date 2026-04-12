package com.thex.chat.chatapi.chat;

public class CallException extends Exception {
    private String clientReasonCode = "error.unknown";

    public static CallException withReasonCode(String clientReasonCode) {
        return new CallException("", clientReasonCode);
    }

    public static CallException withReasonCode(String message, String clientReasonCode) {
        return new CallException(message, clientReasonCode);
    }

    public CallException() {
    }

    public CallException(String message) {
        super(message);
    }

    private CallException(String message, String clientReasonCode) {
        super(message);

        this.clientReasonCode = clientReasonCode;
    }

    public CallException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallException(Throwable cause) {
        super(cause);
    }

    public CallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getClientReasonCode() {
        return clientReasonCode;
    }

    public CallException addToMessage(String postfix) {
        return CallException.withReasonCode(
            getMessage() + postfix,
            getClientReasonCode()
        );
    }
}
