package org.example.finalproject.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.finalproject.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GAuthCheckController implements Initializable {

    @FXML
    private TextField authCodeField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button navigateBackButton;

    @FXML
    void handleLoginButton(ActionEvent event) {
        errorLabel.setText("");
        if(User.currentUser.getTotpKey().isEmpty()) {
            errorLabel.setText("Something went wrong, please try again later!");
            return;
        }
        String authCodeText = authCodeField.getText();
        if(authCodeText.isEmpty()) {
            errorLabel.setText("Enter code!");
            return;
        }
        if(authCodeText.length() != 6) {
            errorLabel.setText("Code must contains 6 numbers!");
            return;
        }
        int enteredCode = Integer.parseInt(authCodeText);
        boolean isCorrect = GoogleAuth.checkAuthCode(User.currentUser.getTotpKey(), enteredCode);
        if(isCorrect) {
            if(Utils.gAuthCheckState == 0) {
                Token.currentToken = new Token(User.currentUser.getID());
                try {
                    Utils.changeScene(event, "main-page");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else if(Utils.gAuthCheckState == 1) {
                try {
                    PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET totpEnabled = false, totpKey = '' WHERE id = ?");
                    preparedStatement.setInt(1, User.currentUser.getID());
                    preparedStatement.executeUpdate();
                    Utils.changeScene(event, "settings-page");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    errorLabel.setText("Can't unlink Google Authenticator (2FA) from account. Try again later!");
                }

            }
        } else {
            errorLabel.setText("Code is incorrect!");
        }
    }

    @FXML
    void handleLostAccessbutton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Google 2FA Authentication");
        alert.setHeaderText("Lost access to google 2FA code");
        alert.setContentText("If you lost access - contact support:\nsocnetjavafx@gmail.com");

        if(alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
    }

    @FXML
    void navigateToBack(ActionEvent event) {
        try {
            if(Utils.gAuthCheckState == 1) Utils.changeScene(event, "settings-page");
            else Utils.changeScene(event, "login-page");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // state 0 - After login
        // state 1 - if deleting
        if(Utils.gAuthCheckState == 1){
            loginButton.setText("Next");
            navigateBackButton.setText("Settings");
        }
    }
}
