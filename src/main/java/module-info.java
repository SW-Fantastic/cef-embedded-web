module embdCEF {

    requires java.desktop;
    requires jcefmaven;
    requires jcef;
    requires jakarta.inject;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires com.formdev.flatlaf;

    opens org.swdc.cef.control to com.formdev.flatlaf;

    exports org.swdc.cef;
    exports org.swdc.cef.control;
    exports org.swdc.cef.schema;

}