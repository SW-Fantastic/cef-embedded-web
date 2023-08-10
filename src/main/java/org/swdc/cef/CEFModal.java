package org.swdc.cef;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cef.CefClient;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.swdc.cef.control.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 *
 * Dialog直接返回的内容是JSON的String，提供给调用它的
 * JavaScript使用
 *
 * @param <T> 如果Modal需要一些配置、选项之类的，这里是它的类型，
 *           不需要的话就直接写Void。
 */
public class CEFModal<T>  {

    private T theOptions;

    private CEFAsync callback;

    private CEFResult result;


    private CEFModalWindow window;

    private CEFContext context;

    private CEFScriptMenuRouting menuHandler = new CEFScriptMenuRouting();

    public CEFModal() {

    }

    void setup(CEFView view,String baseURI, CefClient client, CEFContext context) {
        CEFWebView location = this.getClass().getAnnotation(CEFWebView.class);
        if (location == null) {
            throw new RuntimeException("invalid location");
        }
        window = new CEFModalWindow(view,client,baseURI + (location.location().isBlank() ? "" : ("/" + location.location())));
        window.setMinimumSize(new Dimension(location.width(),location.height()));
        window.setResizable(location.resizeable());
        window.setTitle(location.title());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.context = context;

        java.util.List<Method> methods = CEFUtils.getScriptExport(this.getClass());
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

    }

    CEFModalWindow getWindow() {
        return window;
    }

    public void exitOnClose(boolean val) {
        if (val) {
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
    }

    public void show(CEFAsync callback, String jsonObject) {

        try {

            ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
            Class jsonType = (Class) type.getActualTypeArguments()[0];

            if (jsonType == Void.class || jsonType == void.class) {
                theOptions = null;
            } else if (jsonType == String.class) {
                theOptions = (T)jsonObject;
            } else {
                ObjectMapper mapper = new ObjectMapper();
                theOptions = (T)mapper.readValue(jsonObject,jsonType);
            }

            this.callback = callback;

            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    callback.complete(result == null ? CEFResult.fail(404,"") : result);
                }
            });
            if (window.isVisible()) {
                window.requestFocus();
            } else {
                window.setVisible(true);
                CEFWebView location = this.getClass().getAnnotation(CEFWebView.class);
                if (location.devTools()) {
                    window.openDevTools();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        callback.complete(result == null ? CEFResult.fail(404,"") : result);
        window.setVisible(false);
        SwingUtilities.invokeLater(() -> {
            window.dispose();
        });
    }


    public CEFResult setContextMenu(java.util.List<CEFMenu> menu) {
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
                CEFView view = context.getView(clazz);
                view.show();
                return CEFResult.success("OK");
            } else {
                return CEFResult.fail(500,"no such view");
            }
        } catch (Exception e) {
            return CEFResult.fail(500,"error on creating view");
        }
    }


    public CEFResult complete(T result) {

        this.result = CEFResult.success(result);
        this.close();
        return CEFResult.success("OK");

    }

    public T getTheOptions() {
        return theOptions;
    }
}
