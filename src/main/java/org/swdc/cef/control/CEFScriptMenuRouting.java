package org.swdc.cef.control;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CEFScriptMenuRouting extends CefContextMenuHandlerAdapter {

    private Map<Integer,CEFMenu> indexedMenus = new HashMap<>();
    private List<CEFMenu> menu;

    public void setCEFMenus(List<CEFMenu> menu) {
        this.menu = menu;
    }

    public List<CEFMenu> getMenu() {
        return menu;
    }

    @Override
    public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
        if (menu == null) {
            return;
        }
        model.clear();
        int index = 0;
        for (CEFMenu menuItem: this.menu) {
            applyMenuItem(index,menuItem,model);
            index = index + 1;
        }
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId, int eventFlags) {
        String callbackId = indexedMenus.get(commandId).getScriptCallback();
        browser.executeJavaScript(
                  "window.contextMenuHandlers ? " +
                        "window.contextMenuHandlers[\"" + callbackId + "\"]() :" +
                        "console.log(\" add the window.contextMenuHandlers object and please provides this function on it  : " + callbackId + "\");",
                "res://JavaCEF",0
        );
        return true;
    }

    private int applyMenuItem(int index, CEFMenu menu, CefMenuModel menuModel) {
        if (menu.getChildren().size() > 0) {
            CefMenuModel model = menuModel.addSubMenu(index,menu.getName());
            indexedMenus.put(index,menu);
            for (CEFMenu child : menu.getChildren()) {
                index = index + 1;
                index = applyMenuItem(index, child,model);
            }
        } else {
            menuModel.addItem(index,menu.getName());
            indexedMenus.put(index,menu);
        }
        return index;
    }
}
