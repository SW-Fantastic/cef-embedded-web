package org.swdc.cef;


public interface CEFContext {

    <T extends CEFView> T getView(Class<T> clazz);

    <T extends CEFModal> T createModal(CEFView parent,Class<T> modal);

    CEFAsyncDispatcher dispatcher();
}
