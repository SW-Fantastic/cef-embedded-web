package org.swdc.cef.schema;

import java.io.InputStream;

/**
 * Schema的资源对象。
 * 需要为Resource提供一个InputStream对象，用以读取数据，
 * MimeType也是必须得。
 */
public class SchemaResource {

    private InputStream inputStream;

    private String mimeType;

    private int size;

    public SchemaResource(InputStream inputStream, String mimeType,int size) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
        this.size = size;
        if (inputStream == null || mimeType == null) {
            throw new RuntimeException("invalid parameter");
        }
    }

    public int getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
