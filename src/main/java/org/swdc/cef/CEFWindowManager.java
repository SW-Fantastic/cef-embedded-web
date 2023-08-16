package org.swdc.cef;

import org.cef.CefClient;

import java.util.ArrayList;
import java.util.List;

public class CEFWindowManager {

    private List<CEFViewControl> views = new ArrayList<>();

    private String baseUri;

    private CEFApplication application;

    private CEFContext context;

    public CEFWindowManager(CEFApplication application,String baseUri) {
        this.application = application;
        this.baseUri = baseUri;
    }

    public void setContext(CEFContext context) {
        this.context = context;
    }


    public void initialize(CEFViewControl view) {
        view.initialize(baseUri,application.getInstance().createClient(),context);
    }

    public <T extends CEFViewControl> T createView(Class<T> cefView) {
        try {
            T view = cefView.getConstructor()
                    .newInstance();
            view.initialize(baseUri,application.getInstance().createClient(),context);
            views.add(view);
            return view;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
