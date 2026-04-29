package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.dto.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AbstractServiceDispatcher {
    static final String REQUEST_ID = "req";
    static final String METHOD = "action";

    static final String ERROR_METHOD_UNKNOWN = "error.method.unknown";

    private final String serviceName;
    private final CallBackInvoker callBackInvoker;
    private final HashMap<String, MethodHandler> methodHandlers = new HashMap<>();

    protected AbstractServiceDispatcher(String serviceName, CallBackInvoker callBackInvoker) {
        this.serviceName = serviceName;
        this.callBackInvoker = callBackInvoker;
    }

    String getServiceName() {
        return serviceName;
    }

    MethodHandler getMethodHandler(String method) {
        return methodHandlers.get(method);
    }

    protected void addMethodHandler(String method, MethodHandler handler) {
        methodHandlers.put(method, handler);
    }

    protected void dispatch(ServerSession session, ServerMessage message) {
        log.trace("comet message {} for {}", message, session);

        ConnectionInfo connectionInfo = (ConnectionInfo) session.getAttribute(Consts.CONNECTION_INFO);
        if (connectionInfo == null) {
            throw new NullPointerException("connectionInfo is null for " + session + "; " + message);
        }

        Map<String, Object> data = message.getDataAsMap();
        if (null == data) {
            throw new NullPointerException("data is null for " + session + "; " + message) ;
        }
        ParametersWrapper parameters = ParametersWrapper.wrap(data);

        CallResult callResult;
        Integer requestId = parameters.getIntOptional(REQUEST_ID);
        String method = parameters.getString(METHOD);

        MethodHandler handler = getMethodHandler(method);
        if (handler != null) {
            callResult = safeCall(handler, connectionInfo, parameters);
        } else {
            log.error("Unknown method {} in {} with {}", method, getServiceName(), parameters);
            callResult = CallResult.error(ERROR_METHOD_UNKNOWN);
        }

        callBack(session.getId(), requestId, method, callResult, parameters);
    }

    CallResult safeCall(MethodHandler handler, ConnectionInfo connectionInfo, ParametersWrapper parameters) {
        try {
            return handler.handle(connectionInfo, parameters);
        } catch (CallException e) {
            return CallResult.error(e.getMessage());
        } catch (Throwable e) {
            log.error("Unexpected call error", e);
            return CallResult.errorInternal();
        }
    }

    void callBack(
        String clientId,
        Integer requestId,
        String method,
        CallResult callResult,
        ParametersWrapper parameters
    ) {
        if (callResult.isOk()) {
            if (callResult.isOptional()) {
                return;
            }
        } else {
            log.info(
                "Unsuccessful call of '{}/{}' method for {} with result: {}; call params: {}",
                getServiceName(), method, clientId, callResult, parameters
            );
        }

        if (null != requestId) {
            callResult.add(REQUEST_ID, requestId);
        }

        callResult.add(METHOD, method);
        callBackInvoker.callBack(getServiceName(), callResult, clientId);
    }

    public interface MethodHandler {
        CallResult handle(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException;
    }
}
