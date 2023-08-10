package org.swdc.cef.test;

import org.swdc.cef.control.CEFResult;
import org.swdc.cef.control.CEFWebView;
import org.swdc.cef.CEFView;

@CEFWebView(title = "主窗口",devTools = true,location = "index.html")
public class MainView extends CEFView {

    public CEFResult hello() {
        return CEFResult.success("hello world");
    }

}
