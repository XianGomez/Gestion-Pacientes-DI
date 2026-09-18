module com.clase {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.clase to javafx.fxml;
    exports com.clase;
}
