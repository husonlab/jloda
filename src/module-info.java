module jloda {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.fxml;

    requires transitive java.desktop;
    requires transitive java.xml;

    requires xml.apis.ext;
    requires VectorGraphics2D;

    exports jloda.fx.colorscale;
    exports jloda.fx.control;
    exports jloda.fx.control.table;
    exports jloda.fx.find;
    exports jloda.fx.shapes;
    exports jloda.fx.undo;
    exports jloda.fx.util;
    exports jloda.fx.window;
    exports jloda.graph;
    exports jloda.phylo;
    exports jloda.progs;
    exports jloda.swing.commands;
    exports jloda.swing.director;
    exports jloda.swing.export;
    exports jloda.swing.export.gifEncode;
    exports jloda.swing.find;
    exports jloda.swing.format;
    exports jloda.swing.graphview;
    exports jloda.swing.message;
    exports jloda.swing.util;
    exports jloda.swing.util.lang;
    exports jloda.util;
    exports jloda.util.interval;
    exports jloda.util.parse;
    exports jloda.thirdparty;

    opens jloda.fx.colorscale;
    opens jloda.fx.control.table;
    opens jloda.fx.find;
    opens jloda.resources.icons.sun;
    opens jloda.resources.icons.dialog;
    exports jloda;
    exports jloda.swing.window;

}