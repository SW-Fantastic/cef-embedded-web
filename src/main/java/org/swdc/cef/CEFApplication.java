package org.swdc.cef;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.callback.CefSchemeRegistrar;
import org.swdc.cef.schema.CEFResourceSchema;
import org.swdc.cef.schema.CEFSchema;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CEFApplication {

    private CefApp app;

    private Map<String,CEFSchema> schemas = new HashMap<>();

    private File assetFolder;

    private Class caller;

    public CEFApplication(File assetFolder,String webResourcePrefix,Class caller) {
        this.assetFolder = assetFolder;
        this.caller = caller;
        this.registerURLSchema(new CEFSchema("res",new CEFResourceSchema(webResourcePrefix,caller)));
    }

    public CEFApplication registerURLSchema(CEFSchema schema) {
        String name = schema.getName().toLowerCase();
        if (name.equals("http") || name.equals("ftp") || name.equals("https")) {
            throw new RuntimeException("duplication schema: " + name);
        }
        if (schemas.containsKey(name)) {
            throw new RuntimeException("duplication schema: " + schema.getName());
        }
        schemas.put(name,schema);
        return this;
    }

    public synchronized CefApp getInstance() {
        if (app != null) {
            return app;
        }
        CefAppBuilder builder = CEFHelper.createBuilder(assetFolder,this.caller);
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {

            @Override
            public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                for (CEFSchema schema: schemas.values()) {
                    schema.register(registrar);
                }
            }

            @Override
            public void onContextInitialized() {
                for (CEFSchema schema : schemas.values()) {
                    schema.append(app);
                }
            }
        });
        try {
            app = builder.build();
            return app;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
