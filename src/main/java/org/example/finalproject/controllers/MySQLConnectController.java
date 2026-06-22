package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.finalproject.MySQL;
import org.example.finalproject.MySQLConfig;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.sql.SQLException;

public class MySQLConnectController {
    @FXML
    private TextField portField;

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleExitButton() {
        System.exit(0);
    }

    @FXML
    private void handleSaveButton(ActionEvent event) throws IOException {
        String port = portField.getText() == null ? "" : portField.getText().trim();
        String username = userField.getText() == null ? "" : userField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        MySQL.setPort(port);
        MySQL.setUsername(username);
        MySQL.setPassword(password);
        try {
            MySQL.ConnectToDatabase();
            MySQL.InitDB();
            MySQL.initTestData();
        } catch (SQLException e) {
            System.out.println("Error while initing DB (MySQL Conect Controller)");
            System.out.println(e.getMessage());
        }
        Utils.serializeObject(new MySQLConfig(port, username, password), "mysql.ser");
        Utils.changeScene(event, "login-page");
    }
}
