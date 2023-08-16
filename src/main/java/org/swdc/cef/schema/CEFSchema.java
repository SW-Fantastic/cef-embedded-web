package org.swdc.cef.schema;

import org.cef.CefApp;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;

public class CEFSchema {

    private String name;

    private CefSchemeHandlerFactory factory;


    public CEFSchema(String name, CefSchemeHandlerFactory factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public CefSchemeHandlerFactory getFactory() {
        return factory;
    }

    public void register(CefSchemeRegistrar registrar) {
        registrar.addCustomScheme(
                this.name,
                false,
                true,
                true,
                true,
                true,
                true,
                true
        );
    }

    public void append(CefApp app) {
        app.registerSchemeHandlerFactory(this.name,"/",this.factory);
    }


}
