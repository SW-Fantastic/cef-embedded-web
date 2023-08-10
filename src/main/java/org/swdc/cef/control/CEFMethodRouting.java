package org.swdc.cef.control;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.swdc.cef.CEFAsync;
import org.swdc.cef.CEFAsyncDispatcher;
import org.swdc.cef.CEFUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

public class CEFMethodRouting extends CefMessageRouterHandlerAdapter {

    private Method method;

    private Object view;

    private ObjectMapper mapper;

    private CEFAsyncDispatcher dispatcher;

    public CEFMethodRouting(CEFAsyncDispatcher dispatcher, Object view, Method method) {
        this.method = method;
        this.view = view;
        mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        this.dispatcher = dispatcher;
    }


    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        try {

            CEFRequest theRequest = mapper.readValue(request,CEFRequest.class);
            if (method.getReturnType() == CEFAsync.class) {
                CEFAsync result = callAsync(theRequest.getParameters());
                dispatcher.dispatch(result,callback);
                return true;
            } else {
                CEFResult result = call(theRequest.getParameters());
                if (result == null) {
                    return true;
                }
                if (result.getCode() != 200) {
                    callback.failure(result.getCode(),result.getMessage());
                } else {
                    callback.success(result.getMessage());
                }
            }
            return true;
        } catch (Exception ignore) {
            return true;
        }
    }

    public CEFAsync callAsync(List<String> parameters) {
        if (method != null) {
            try {
                CEFAsync result = null;
                if (method.getParameters().length == 0) {
                    result = (CEFAsync) method.invoke(view);
                } else {
                    Object[] params = CEFUtils.convertStringAsParameters(method,parameters);
                    result = (CEFAsync)  method.invoke(view,params);
                }
                return result;
            } catch (Throwable t) {
                return CEFAsync.fail(dispatcher,500,"failed to call method");
            }
        }
        return CEFAsync.fail(dispatcher,500,"no such method");
    }

    public CEFResult call(List<String> parameters) {
        if (method != null) {
            try {
                CEFResult result = null;
                if (method.getParameters().length == 0) {
                    result = (CEFResult) method.invoke(view);
                } else {
                    Object[] params = CEFUtils.convertStringAsParameters(method,parameters);
                    result = (CEFResult)  method.invoke(view,params);
                }
                return result;
            } catch (Throwable t) {
                return CEFResult.fail(500,"failed to call method");
            }
        }
        return CEFResult.fail(500,"no such method");
    }

}
