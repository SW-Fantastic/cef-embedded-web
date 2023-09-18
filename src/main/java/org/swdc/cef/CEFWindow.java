package org.swdc.cef;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.swdc.cef.control.CEFMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CEFWindow extends JFrame {

    private CefBrowser browser;

    private Component browserUI;

    private JMenuBar menuBar;


    public CEFWindow(CefClient client, String url) {
        browser = client.createBrowser(url,false,false);
        browserUI = browser.getUIComponent();
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        getContentPane().add(browserUI);
    }


    public void openDevTools() {
       CompletableFuture.runAsync(() -> {
           while (browser.isLoading());
           try {
               Thread.sleep(1000 * 5);
           } catch (InterruptedException e) {
           }
       }).thenAccept(v -> {
           JFrame window = new CEFDevToolsWindow(getTitle(),this.browser);
           window.setMinimumSize(new Dimension(1000,600));
           window.setTitle("DEV Tools: " + getTitle());
           window.setLocationRelativeTo(null);
           window.setVisible(true);
       });
    }

    public void setMenu(List<CEFMenu> menu) {
        menuBar.removeAll();
        for (CEFMenu item: menu) {
            JMenu menuItem = CEFUtils.parseJMenu(browser,item);
            menuItem.setText(item.getName());
            menuBar.add(menuItem);
        }
        revalidate();
        repaint();
    }

    @Override
    public void dispose() {
        if (browser == null) {
            super.dispose();
            return;
        }
        super.dispose();
        CefClient client = this.browser.getClient();
        this.browser.doClose();
        client.dispose();
        browser = null;
    }


}
