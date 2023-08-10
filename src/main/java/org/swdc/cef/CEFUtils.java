package org.swdc.cef;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cef.browser.CefBrowser;
import org.swdc.cef.control.CEFMenu;
import org.swdc.cef.control.CEFResult;

import javax.swing.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CEFUtils {

    public static List<Method> getScriptExport(Class type) {

        List<Method> results = new ArrayList<>();
        Class current = type;
        while (current != null) {
            Method[] methods = current.getDeclaredMethods();
            for (Method method: methods) {
                if (method.getReturnType().equals(CEFResult.class)  || method.getReturnType().equals(CEFAsync.class)) {
                    method.setAccessible(true);
                    results.add(method);
                }
            }
            current = current.getSuperclass();
        }

       return results;
    }

    public static Object[] convertStringAsParameters(Method method, List<String> param) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        Object[] result = new Object[param.size()];
        Parameter[] parameters = method.getParameters();
        if (param.size() != parameters.length) {
            return null;
        }
        for (int idx = 0; idx < parameters.length; idx ++) {
            Parameter theParam = parameters[idx];
            Class paramType = theParam.getType();
            try {
                if (theParam.getParameterizedType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) theParam.getParameterizedType();
                    Type[] types = type.getActualTypeArguments();
                    Class[] paramTypes = new Class[types.length];
                    for (int typeParamIdx = 0; typeParamIdx < types.length; typeParamIdx ++) {
                        paramTypes[typeParamIdx] = (Class) types[typeParamIdx];
                    }
                    JavaType jtype = mapper.getTypeFactory().constructParametricType((Class<?>) type.getRawType(),paramTypes);
                    result[idx] = mapper.readValue(param.get(idx), jtype);
                } else {
                    result[idx] = mapper.readValue(param.get(idx), paramType);
                }
            } catch (Exception e) {
                result[idx] = null;
            }
        }
        return result;
    }


    public static JMenu parseJMenu(CefBrowser browser,CEFMenu menu) {
        JMenu result = new JMenu();
        for (CEFMenu child: menu.getChildren()) {
            if (child.getChildren().size() > 0) {
                JMenu target = parseJMenu(browser,child);
                target.setText(child.getName());
                result.add(target);
            } else {
                JMenuItem item = new JMenuItem();
                item.setText(child.getName());
                item.addActionListener(e -> browser.executeJavaScript(
                        "window.menubarHandlers ?" +
                                "window.menubarHandlers[\"" + child.getScriptCallback() + "\"]() : "+
                                "console.log(\"please add window.menubarHandlers object and then provides a function named :" + child.getScriptCallback() + "\")",
                        "res://CEFView",
                        0
                ));
                result.add(item);
            }
        }
        return result;
    }



}
