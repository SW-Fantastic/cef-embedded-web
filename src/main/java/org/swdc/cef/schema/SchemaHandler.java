package org.swdc.cef.schema;

import org.cef.callback.CefCallback;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.IOException;
import java.io.InputStream;

public abstract class SchemaHandler implements CefResourceHandler {

    private SchemaResource resource;

    private int offset = 0;

    public abstract SchemaResource getResource(CefRequest request);

    public abstract void prepareResponse(CefResponse response);

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        SchemaResource data = getResource(request);
        if (data == null) {
            return false;
        }
        callback.Continue();
        this.resource = data;
        return true;
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
        prepareResponse(response);
        try {
            response.setStatus(200);
            response.setMimeType(resource.getMimeType());
            response.setHeaderByName("Access-Control-Allow-Origin","*",true);
            responseLength.set(resource.getSize());
        } catch (Exception e){
            response.setError(CefLoadHandler.ErrorCode.ERR_FILE_NOT_FOUND);
        }
    }

    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
        try {
            if (resource == null) {
                return false;
            }
            InputStream inputStream = resource.getInputStream();
            int transfer = Math.min(bytesToRead, (resource.getSize() - offset));
            inputStream.read(dataOut,0,transfer);
            offset = offset + transfer;
            bytesRead.set(transfer);
            if (resource.getSize() - offset <= 0) {
                inputStream.close();
                resource = null;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resource.getInputStream().close();
                resource = null;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return false;
        }
    }

    @Override
    public void cancel() {
        if (resource == null) {
            return;
        }
        try {
            resource.getInputStream().close();
            resource = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
