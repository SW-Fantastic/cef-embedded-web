package org.swdc.cef;

import java.util.ArrayList;
import java.util.List;

public class CEFWindowManager {

    private List<CEFView> views = new ArrayList<>();

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

    public <T extends CEFView> T createView(Class<T> cefView) {
        try {
            T view = cefView.getConstructor()
                    .newInstance();
            view.setup(baseUri,application.getInstance().createClient(),context);
            views.add(view);
            return view;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends CEFModal> T createModal(CEFView view, Class<T> modal) {
        try {
            T theView = modal.getConstructor()
                    .newInstance();
            theView.setup(view,baseUri,application.getInstance().createClient(),context);
            return theView;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
