package org.swdc.cef;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class SingletonCEFViewContext implements CEFContext {

    private Map<Class<? extends CEFView>,CEFView> views = new HashMap<>();

    private CEFWindowManager manager;

    private CEFAsyncDispatcher dispatcher;

    public SingletonCEFViewContext(CEFAsyncDispatcher dispatcher,  CEFWindowManager manager) {
        this.manager = manager;
        this.dispatcher = dispatcher;
    }

    @Override
    public <T extends CEFView> T getView(Class<T> clazz) {
        if (views.containsKey(clazz)) {
            return (T)views.get(clazz);
        }
        T target = manager.createView(clazz);
        views.put(clazz,target);
        return target;
    }

    @Override
    public <T extends CEFModal> T createModal(CEFView parent, Class<T> modal) {
        return manager.createModal(parent,modal);
    }

    @Override
    public CEFAsyncDispatcher dispatcher() {
        return dispatcher;
    }

}
