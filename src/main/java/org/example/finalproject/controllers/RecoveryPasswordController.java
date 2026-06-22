package org.example.finalproject.controllers;

import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import org.example.finalproject.Mailer;
import org.example.finalproject.MySQL;
import org.example.finalproject.Utils;
import org.example.finalproject.exceptions.MailerException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class RecoveryPasswordController {

    @FXML private VBox emailStepContainer;
    @FXML private VBox codeStepContainer;
    @FXML private VBox passwordStepContainer;

    @FXML private TextField recoveryEmailField;
    @FXML private TextField confirmationCodeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField repeatPasswordField;

    @FXML private Button sendCodeButton;

    @FXML private Label statusLabel;

    private String tmpCode;
    private int recoveryUserID = -1;

    @FXML
    private void handleSendCode(ActionEvent event) throws InterruptedException {
        String email = recoveryEmailField.getText();
        if(email == null || email.trim().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter a valid email address.");
            return;
        }

        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id FROM users WHERE contactInfo = ?");
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                recoveryUserID = resultSet.getInt("id");
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("User with this email address not found");
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        sendCodeButton.setText("Sending...");
        sendCodeButton.setDisable(true);

        Random random = new Random();
        tmpCode = "" + random.nextInt(100000, 1000000);
        System.out.println(tmpCode);

        Thread thread = new Thread(() -> {
            try {
                String htmlTemplate = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "    <title>Password Recovery</title>" +
                        "</head>" +
                        "<body style='margin: 0; padding: 0; background-color: #f7faf8; font-family: \"Segoe UI\", Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;'>" +
                        "    <table border='0' cellpadding='0' cellspacing='0' width='100%%' style='background-color: #f7faf8; padding: 40px 0;'>" +
                        "        <tr>" +
                        "            <td align='center'>" +
                        "                " +
                        "                <table border='0' cellpadding='0' cellspacing='0' width='100%%' style='max-width: 520px; background-color: #ffffff; border: 1px solid #e6ece8; border-radius: 10px; box-shadow: 0 5px 10px rgba(0,0,0,0.03); overflow: hidden;'>" +
                        "                    " +
                        "                    " +
                        "                    <tr>" +
                        "                        <td bgcolor='#1b4d3e' style='padding: 25px 30px; border-bottom: 1px solid #0f2e24; text-align: left;'>" +
                        "                            <span style='color: #ffffff; font-size: 22px; font-weight: bold; letter-spacing: 0.5px;'>SocNet</span>" +
                        "                        </td>" +
                        "                    </tr>" +
                        "                    " +
                        "                    " +
                        "                    <tr>" +
                        "                        <td style='padding: 40px 30px; text-align: left;'>" +
                        "                            <h1 style='margin: 0 0 20px 0; color: #1b4d3e; font-size: 26px; font-weight: bold;'>Password Recovery</h1>" +
                        "                            <p style='margin: 0 0 25px 0; color: #2d3732; font-size: 14px; line-height: 1.6;'>" +
                        "                                We received a request to reset your password. Use the verification code below to complete the process. This code is temporary and will expire shortly." +
                        "                            </p>" +
                        "                            " +
                        "                            " +
                        "                            <table border='0' cellpadding='0' cellspacing='0' width='100%%' style='margin: 30px 0;'>" +
                        "                                <tr>" +
                        "                                    <td align='center' bgcolor='#f4f7f5' style='padding: 15px 0; border: 1px solid #e0e6e2; border-radius: 5px;'>" +
                        "                                        <span style='font-family: Consolas, Monaco, monospace; font-size: 32px; font-weight: bold; color: #2ec4b6; letter-spacing: 6px; padding-left: 6px;'>" + tmpCode + "</span>" +
                        "                                    </td>" +
                        "                                </tr>" +
                        "                            </table>" +
                        "                            " +
                        "                            <p style='margin: 0 0 10px 0; color: #718078; font-size: 13px;'>" +
                        "                                If you did not request this change, you can safely ignore this email. Your password will remain unchanged." +
                        "                            </p>" +
                        "                        </td>" +
                        "                    </tr>" +
                        "                    " +
                        "                    " +
                        "                    <tr>" +
                        "                        <td style='padding: 20px 30px; background-color: #f4f7f5; border-top: 1px solid #e6ece8; text-align: center;'>" +
                        "                            <p style='margin: 0; color: #718078; font-size: 12px;'>&copy; 2026 SocNet. All rights reserved.</p>" +
                        "                        </td>" +
                        "                    </tr>" +
                        "                </table>" +
                        "            </td>" +
                        "        </tr>" +
                        "    </table>" +
                        "</body>" +
                        "</html>";

                Mailer.sendEmail(email, "Password Recovery", htmlTemplate);
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Verification code sent to " + email);
                    //
                    emailStepContainer.setVisible(false);
                    emailStepContainer.setManaged(false);

                    codeStepContainer.setVisible(true);
                    codeStepContainer.setManaged(true);
                });
            } catch(MessagingException | MailerException e) {
                System.out.println(e.getMessage());
                statusLabel.setText("Something went wrong while sending email. Try again later!");
            }
        });
        thread.start();



    }

    @FXML
    private void handleConfirmCode(ActionEvent event) {
        String code = confirmationCodeField.getText();
        if(code == null || code.trim().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Verification code field cannot be empty.");
            return;
        }
        if(!code.equals(tmpCode)) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Code is incorrect!");
            return;
        }

        statusLabel.setText("");

        codeStepContainer.setVisible(false);
        codeStepContainer.setManaged(false);

        // pass
        passwordStepContainer.setVisible(true);
        passwordStepContainer.setManaged(true);
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        if(recoveryUserID == -1) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Invalid phone number/email address! Try again!");
            return ;
        }

        String password = newPasswordField.getText();
        String repeatPassword = repeatPasswordField.getText();


        if(password.isEmpty() || repeatPassword.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Password fields cannot be empty.");
            return;
        }
        if(password.length() < 6 || password.length() > 20) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Password must be between 6 and 20 characters long!");
            return;
        }
        boolean passwordContainsCapital = false;
        for (int i = 0; i < password.length(); i++) {
            if(password.charAt(i) >= 'A' && password.charAt(i) <= 'Z') {
                passwordContainsCapital = true;
                break;
            }
        }
        if(!passwordContainsCapital) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Password must contain minimum 1 Capital Letter!");
            return;
        }

        if(!password.equals(repeatPassword)) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Passwords mismatch! Re-type configurations.");
            return;
        }

        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET password=MD5(?) WHERE id=?");
            preparedStatement.setString(1, password);
            preparedStatement.setInt(2, recoveryUserID);
            int rows = preparedStatement.executeUpdate();
            if(rows > 0) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Success! Your password has been updated.");
                passwordStepContainer.setDisable(true);
            }
            else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("User not found with this email");
                passwordStepContainer.setDisable(true);
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Something went wrong, please try again later!");
        }


    }

    @FXML
    private void navigateToLogin(ActionEvent event) throws IOException {
        Utils.changeScene(event, "login-page");
    }
}