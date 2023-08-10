package org.swdc.cef.control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CEFWebView {

    String location() default "";

    int width() default 800;

    int height() default 600;

    boolean resizeable() default true;

    boolean devTools() default false;

    String title() default "CEF View";

}
