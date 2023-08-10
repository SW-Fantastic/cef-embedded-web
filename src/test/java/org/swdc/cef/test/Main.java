package org.swdc.cef.test;

import com.formdev.flatlaf.FlatLightLaf;
import org.swdc.cef.*;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        setupFlatLAF();

        CEFApplication application = new CEFApplication(new File("assets"),"static", Main.class);
        CEFAsyncDispatcher dispatcher = new CEFAsyncDispatcher();
        CEFWindowManager manager = new CEFWindowManager(application,"res://");
        CEFContext context = new SingletonCEFViewContext(dispatcher,manager);
        manager.setContext(context);

        CEFView view = context.getView(MainView.class);
        view.exitOnClose(true);
        view.show();

    }

    public static void setupFlatLAF(){
        try {
            class LAF extends FlatLightLaf {

                @Override
                public UIDefaults getDefaults() {
                    UIDefaults defaults = super.getDefaults();
                    defaults.put("ClassLoader",FlatLightLaf.class.getModule().getClassLoader());
                    return defaults;
                }
            }
            System.setProperty("flatlaf.useWindowDecorations","true" );
            System.setProperty("flatlaf.menuBarEmbedded","true");

            LAF laf = new LAF();
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}