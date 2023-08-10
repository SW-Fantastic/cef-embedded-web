package org.swdc.cef;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CEFDevToolsWindow extends JFrame {

    private CefBrowser browser;

    public CEFDevToolsWindow(String title, CefBrowser browser){

        this.browser = browser.getDevTools();
        getContentPane().add(this.browser.getUIComponent());

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(1000,600));
        this.setTitle("DEV Tools: " + title);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    @Override
    public void dispose() {
        if (browser == null) {
            super.dispose();
            return;
        }
        this.browser.doClose();
        browser = null;
        super.dispose();
    }
}
