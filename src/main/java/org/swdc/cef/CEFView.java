package org.swdc.cef;

import org.cef.CefClient;
import org.cef.browser.CefMessageRouter;
import org.swdc.cef.control.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;

public class CEFView implements CEFViewControl {

    private CEFWindow window;

    private CEFContext context;

    private CEFScriptMenuRouting menuHandler = new CEFScriptMenuRouting();

    private boolean initialized;

    public CEFView() {

    }

    public void initialize(String baseURI, CefClient client, CEFContext context) {
        if (initialized) {
            return;
        }

        CEFWebView location = this.getClass().getAnnotation(CEFWebView.class);
        if (location == null) {
            throw new RuntimeException("invalid location");
        }

        String url = (baseURI == null || baseURI.isBlank()) ?
                location.location() :
                location.location().isBlank() ?
                        baseURI : baseURI + "/" + location.location();

        window = new CEFWindow(client,url );
        window.setMinimumSize(new Dimension(location.width(),location.height()));
        window.setResizable(location.resizeable());
        window.setTitle(location.title());
        window.setLocationRelativeTo(null);

        this.context = context;

        List<Method> methods = CEFUtils.getScriptExport(this.getClass());
        for (Method m: methods) {
            if (m.getName().equals("call")) {
                continue;
            }
            CefMessageRouter router = CefMessageRouter.create(new CefMessageRouter
                    .CefMessageRouterConfig(
                            "$" + m.getName(),
                    "$cancel" + m.getName()
                    ),
                    new CEFMethodRouting(context.dispatcher(),this,m)
            );
            client.addMessageRouter(router);
        }

        client.addContextMenuHandler(menuHandler);
        initialized = true;
    }

    CEFWindow getWindow() {
        return window;
    }

    public void exitOnClose(boolean val) {
        if (val) {
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
    }

    public CEFAsyncDispatcher dispatcher() {
        return context.dispatcher();
    }

    public void show() {
        if (window.isVisible()) {
            window.requestFocus();
        } else {
            window.setVisible(true);
            CEFWebView location = this.getClass().getAnnotation(CEFWebView.class);
            if (location.devTools()) {
                window.openDevTools();
            }
        }
    }

    public void close() {
        window.setVisible(false);
    }


    public CEFResult setContextMenu(List<CEFMenu> menu) {
        menuHandler.setCEFMenus(menu);
        return CEFResult.success("OK");
    }


    public CEFResult setBarMenu(List<CEFMenu> menus) {
        window.setMenu(menus);
        return CEFResult.success("OK");
    }

    public CEFResult showView(String className) {
        try {
            Class clazz = ClassLoader.getSystemClassLoader().loadClass(className);
            if (CEFView.class.isAssignableFrom(clazz)) {
                CEFView view = (CEFView) context.getView(clazz);
                view.show();
                return CEFResult.success("OK");
            } else {
                return CEFResult.fail(500,"no such view");
            }
        } catch (Exception e) {
            return CEFResult.fail(500,"error on creating view");
        }
    }

    public CEFAsync showViewModal(String className,String optionJson) {
        try {
            Class clazz = ClassLoader.getSystemClassLoader().loadClass(className);
            if (CEFModal.class.isAssignableFrom(clazz)) {
                CEFAsync result = new CEFAsync(dispatcher());
                CEFModal view = (CEFModal) context.getView(clazz);
                view.show(this,result,optionJson);
                return result;
            }
            return CEFAsync.fail(context.dispatcher(),500,"class is not a modal view.");
        } catch (Exception e) {
            return CEFAsync.fail(context.dispatcher(),500,"error on creating view");
        }
    }


}
