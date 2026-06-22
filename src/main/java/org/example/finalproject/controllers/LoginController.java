package org.example.finalproject.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.finalproject.Token;
import org.example.finalproject.User;
import org.example.finalproject.Utils;
import org.example.finalproject.exceptions.AuthUserNotFoundException;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private boolean buttonClicked = false;

    @FXML
    private TextField contactInfoField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;


    @FXML
    private void handleLoginButton(ActionEvent event) {
        if(buttonClicked) return;
        buttonClicked = true;
        errorLabel.setText("");
        if(validateForm()) {
            String contactInfo = contactInfoField.getText();
            String password = passwordField.getText();
            try {
                User responseUser = User.checkUser(contactInfo, password);
                Token.currentToken = new Token(responseUser.getID());
                User.currentUser = responseUser;
                Utils.changeScene(event, "main-page");

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                errorLabel.setText("Something went wrong... Please, try again later");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                errorLabel.setText("Something went wrong while redirecting... Please, try again later!");
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
                errorLabel.setText("Something went wrong while serializing token. Try again later!");
            } catch (AuthUserNotFoundException e) {
                System.out.println("Login invalid credentials");
                errorLabel.setText("Invalid credentials!");
            }
        }
        buttonClicked = false;
    }


    @FXML
    private void navigateToRegister(ActionEvent event) throws IOException {
        Utils.changeScene(event, "register-page");
    }
    @FXML
    private void navigateToRecovery(ActionEvent event) throws IOException {
        Utils.changeScene(event, "recovery-password");
    }

    private boolean validateForm() {
        String contactInfo = contactInfoField.getText();
        if(contactInfo.isEmpty()) {
            errorLabel.setText("Enter Email or Phone number!");
            return false;
        }
        boolean isEmail = false;
        if(contactInfo.length() < 5) { // a@a.a | 5
            errorLabel.setText("Enter valid email or phone number!");
            return false;
        }
        else {
            for(int i = 0; i < contactInfo.length(); i++) {
                if(contactInfo.charAt(i) < '0' || contactInfo.charAt(i) > '9'){
                    isEmail = true;
                    break;
                }
            }
            if(!isEmail) {
                if(contactInfo.length() > 15) {
                    errorLabel.setText("Phone number must be between 5 and 15 digits");
                    return false;
                }
            }
            else {
                if(!contactInfo.contains("@") || !contactInfo.contains(".")) {
                    errorLabel.setText("Enter valid email address!");
                    return false;
                }
            }
        }

        String password = passwordField.getText();
        if(password.isEmpty()) {
            errorLabel.setText("Please enter password!");
            return false;
        }
        return true;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
