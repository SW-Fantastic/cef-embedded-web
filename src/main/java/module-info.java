module embdCEF {

    requires java.desktop;
    requires jcefmaven;
    requires jcef;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires com.formdev.flatlaf;

    opens org.swdc.cef.control to com.formdev.flatlaf;

}