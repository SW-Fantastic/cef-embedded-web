package org.swdc.cef.schema;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CEFResourceSchema extends CEFSchema {

    public CEFResourceSchema(String prefix, Class caller) {
        super("res", new CEFResourceSchemaFactory(prefix,caller));
    }

    static class CEFJavaResourceHandler extends SchemaHandler {

        private Class caller;

        private String prefix;

        public CEFJavaResourceHandler(String resourcePrefix, Class caller) {
            this.caller = caller;
            this.prefix = resourcePrefix;
        }

        @Override
        public SchemaResource getResource(CefRequest request) {
            String url = request.getURL();
            if (url.startsWith("res://")) {
                url = url.replace("res://","");
            } else if (url.startsWith("res:")) {
                url = url.replace("res:","");
            }

            int posDot = url.lastIndexOf(".");
            String ext = url.substring(posDot + 1);
            String mime = MimeTypes.getMimeType(ext);

            try {
                InputStream in = caller.getModule().getResourceAsStream(prefix + "/" + url);
                if (in == null) {
                    return null;
                }

                return new SchemaResource(in,mime,in.available());
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public void prepareResponse(CefResponse response) {

        }


    }


    public static class CEFResourceSchemaFactory implements CefSchemeHandlerFactory {

        private Class caller;

        private String prefix;


        public CEFResourceSchemaFactory(String prefix, Class caller) {
            this.caller = caller;
            this.prefix = prefix;
        }
        @Override
        public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
            return new CEFJavaResourceHandler(prefix,caller);
        }
    }



}
