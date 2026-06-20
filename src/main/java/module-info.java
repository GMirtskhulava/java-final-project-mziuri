module org.example.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jdk.jshell;
    requires javafx.base;
    requires jakarta.mail;
    requires java.desktop;
    requires javafx.graphics;


    opens org.example.finalproject to javafx.fxml;
    exports org.example.finalproject;
}