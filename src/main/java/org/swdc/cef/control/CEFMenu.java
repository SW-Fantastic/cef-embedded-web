package org.swdc.cef.control;

import java.util.ArrayList;
import java.util.List;

public class CEFMenu {

    private String name;

    private String scriptCallback;

    private List<CEFMenu> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<CEFMenu> getChildren() {
        return children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<CEFMenu> children) {
        this.children = children;
    }

    public void setScriptCallback(String scriptCallback) {
        this.scriptCallback = scriptCallback;
    }

    public String getScriptCallback() {
        return scriptCallback;
    }
}
