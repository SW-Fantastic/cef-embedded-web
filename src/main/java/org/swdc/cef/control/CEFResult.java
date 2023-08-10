package org.swdc.cef.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CEFResult {

    private String message;

    private int code;

    CEFResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static CEFResult success(Object response) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        try {
            return new CEFResult(200, mapper.writeValueAsString(response));
        } catch (Exception e) {
            return fail(500,"exception on executing");
        }
    }

    public static CEFResult fail(int code, String message) {
        return new CEFResult(code,message);
    }

}
