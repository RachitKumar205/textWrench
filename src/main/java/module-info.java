module com.example.textwrench {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.material;
    requires org.kordamp.ikonli.core;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.eclipse.lsp4j;

    opens com.example.textwrench to javafx.fxml;
    exports com.example.textwrench;
    exports com.example.textwrench.lsp;
    opens com.example.textwrench.lsp to javafx.fxml;
}