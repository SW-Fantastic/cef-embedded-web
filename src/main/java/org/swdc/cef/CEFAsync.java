package org.swdc.cef;

import org.swdc.cef.control.CEFResult;

public class CEFAsync {

    private CEFResult result;

    public CEFAsyncDispatcher dispatcher;

    public CEFAsync(CEFAsyncDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public CEFAsync(CEFAsyncDispatcher dispatcher, CEFResult result) {
        this.dispatcher = dispatcher;
    }

    public CEFResult getResult() {
        return result;
    }

    public void complete(CEFResult result) {
        this.result = result;
        dispatcher.awake();
    }

    public static CEFAsync fail(CEFAsyncDispatcher dispatcher,int code, String message) {
        return new CEFAsync(dispatcher,CEFResult.fail(code,message));
    }

}
