package com.thex.chat.chatapi.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class CallResult {
    static private final String CODE = "code";
    static private final String OK = "ok";
    static private final String ERROR_INTERNAL = "error.internal";

    static final CallResult OPTIONAL = new CallResult(OK) {
        @Override
        boolean isOptional() {
            return true;
        }

        @Override
        public CallResult add(String key, Object value) {
            throw new UnsupportedOperationException();
        }
    };
    static final CallResult INTERNAL = new CallResult(ERROR_INTERNAL);

    private final String errInfo;
    private final Map<String, Object> data = new HashMap<>();

    public static CallResult ok() {
        return new CallResult(OK);
    }
    public static CallResult error(String code, String errInfo) {
        return new CallResult(code, errInfo);
    }
    public static CallResult error(String code) {
        return new CallResult(code);
    }
    public static CallResult errorInternal() {
        return INTERNAL;
    }
    public static CallResult optional() {
        return OPTIONAL;
    }

    private CallResult(String code, String errInfo) {
        data.put(CODE, code);
        this.errInfo = errInfo;
    }

    private CallResult(String code) {
        this(code, "");
    }

    public CallResult add(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public String getString(String key) {
        return String.valueOf(data.get(key));
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> getData() {
        return data;
    }

    String getCode() {
        return String.valueOf(data.get(CODE));
    }

    boolean isOk() {
        return OK == data.get(CODE);
    }

    boolean isOptional() {
        return false;
    }

    String getErrInfo() {
        return errInfo;
    }

    @Override
    public String toString() {
        return "{" +
            (errInfo.isEmpty() ? "" : "errInfo='" + errInfo + "', ") +
            "data=" + data +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CallResult that)) return false;
        return Objects.equals(errInfo, that.errInfo) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errInfo, data);
    }
}
