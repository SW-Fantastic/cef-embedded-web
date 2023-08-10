package org.swdc.cef.schema;

import java.io.InputStream;

public class SchemaResource {

    private InputStream inputStream;

    private String mimeType;

    SchemaResource(InputStream inputStream, String mimeType) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
        if (inputStream == null || mimeType == null) {
            throw new RuntimeException("invalid parameter");
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
