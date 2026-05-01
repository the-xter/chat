package com.thex.chat.chatapi.chat;

import java.util.Map;

@SuppressWarnings("unused")
public class ParametersWrapper {
    private final Map<String, Object> data;

    public static ParametersWrapper wrap(Map<String, Object> data) {
        return new ParametersWrapper(data);
    }

    private ParametersWrapper(Map<String, Object> data) {
        this.data = data;
    }

    public Integer getIntOptional(String name) {
        return data.get(name) == null ? null : ((Number) data.get(name)).intValue();
    }

    public int getInt(String name) throws NullPointerException {
        return getIntOptional(name);
    }

    public int getInt(String name, int defaultValue) {
        Integer result = getIntOptional(name);
        return result == null ? defaultValue : result;
    }

    public Double getDoubleOptional(String name) {
        return data.get(name) == null ? null : ((Number) data.get(name)).doubleValue();
    }

    public double getDouble(String name) throws NullPointerException {
        return getDoubleOptional(name);
    }

    public Long getLongOptional(String name) {
        return data.get(name) == null ? null : ((Number) data.get(name)).longValue();
    }

    public String getStringOptional(String name) {
        return data.get(name) == null ? null : (String) data.get(name);
    }

    public String getString(String name) {
        return (String) data.get(name);
    }

    public String getValueAsString(String name) {
        return data.get(name).toString();
    }

    public Boolean getBoolOptional(String name) {
        return (Boolean) data.get(name);
    }

    public boolean getBool(String name) {
        return getBoolOptional(name);
    }

    public boolean getBool(String name, boolean defaultValue) {
        Boolean result = getBoolOptional(name);
        return result == null ? defaultValue : result;
    }

    @Override
    public String toString() {
        return "{" +
            "data=" + data +
            '}';
    }
}
