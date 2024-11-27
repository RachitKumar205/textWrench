module com.example.textwrench {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.material;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.eclipse.lsp4j;
    requires org.kordamp.ikonli.bootstrapicons;
    requires com.fasterxml.jackson.databind;
    requires org.kordamp.ikonli.devicons;
    requires org.kordamp.ikonli.core;

    opens com.example.textwrench to javafx.fxml;
    exports com.example.textwrench;
    exports com.example.textwrench.lsp;
    opens com.example.textwrench.lsp to javafx.fxml;
}