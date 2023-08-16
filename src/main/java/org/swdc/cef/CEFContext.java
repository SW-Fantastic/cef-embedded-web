package org.swdc.cef;


public interface CEFContext {

    <T extends CEFViewControl> T getView(Class<T> clazz);

    CEFAsyncDispatcher dispatcher();
}
