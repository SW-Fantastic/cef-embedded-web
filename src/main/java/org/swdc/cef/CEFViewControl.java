package org.swdc.cef;

import org.cef.CefClient;

public interface CEFViewControl {

    void initialize(String baseURI, CefClient client, CEFContext context);

}
