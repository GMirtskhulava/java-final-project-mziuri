package org.example.finalproject.controllers;

import jakarta.mail.MessagingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.finalproject.*;
import org.example.finalproject.exceptions.AuthUserNotFoundException;
import org.example.finalproject.exceptions.MailerException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class RegisterController {
    private boolean buttonClicked = false;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField contactInfoField;
    @FXML
    private DatePicker birthDateField;
    @FXML
    private RadioButton maleGenderRB;
    @FXML
    private RadioButton femaleGenderRB;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;


    @FXML
    private void handleRegisterButton(ActionEvent event) {
        if(buttonClicked) return;
        buttonClicked = true;
        errorLabel.setText("");
        if(validateForm()) {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id FROM users WHERE contactInfo=?;");
                preparedStatement.setString(1, contactInfoField.getText());
                ResultSet resultSet = preparedStatement.executeQuery();
                if(!resultSet.next()) {
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String contactInfo = contactInfoField.getText();
                    LocalDate birthDate = birthDateField.getValue();
                    String gender = maleGenderRB.isSelected() ? "male" : "female";
                    String password = passwordField.getText();

                    preparedStatement = MySQL.connection.prepareStatement("INSERT INTO users (firstname, lastname, password, contactInfo, gender, birthdate) VALUES (?, ?, MD5(?), ?, ?, ?);");
                    preparedStatement.setString(1, firstName);
                    preparedStatement.setString(2, lastName);
                    preparedStatement.setString(3, password);
                    preparedStatement.setString(4, contactInfo);
                    preparedStatement.setString(5, gender);
                    preparedStatement.setObject(6, birthDate);

                    int row = preparedStatement.executeUpdate();
                    if(row > 0) {
                        User checkUser = User.checkUser(contactInfo, password);

                        Token.currentToken = new Token(checkUser.getID());
                        User.currentUser = checkUser;

                        Utils.changeScene(event, "main-page");

                        if(Mailer.isEmail(contactInfo)) {
                            new Thread(() -> {
                                try {
                                    String htmlContent = "<!DOCTYPE html>\n" +
                                            "<html>\n" +
                                            "<body style=\"margin: 0; padding: 0; background-color: #f7faf8; font-family: 'Segoe UI', Helvetica, Arial, sans-serif;\">\n" +
                                            "    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f7faf8; padding: 40px 20px;\">\n" +
                                            "        <tr>\n" +
                                            "            <td align=\"center\">\n" +
                                            "                <!-- Card Container -->\n" +
                                            "                <table width=\"100%\" max-width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 10px rgba(0,0,0,0.03); border: 1px solid #e6ece8;\">\n" +
                                            "                    \n" +
                                            "                    <!-- Header (Deep Forest Green) -->\n" +
                                            "                    <tr>\n" +
                                            "                        <td style=\"background: linear-gradient(135deg, #1b4d3e, #0f2e24); padding: 40px 30px; text-align: center;\">\n" +
                                            "                            <h1 style=\"margin: 0; color: #ffffff; font-size: 28px; font-weight: bold; letter-spacing: 0.5px;\">Welcome Aboard!</h1>\n" +
                                            "                            <p style=\"margin: 10px 0 0 0; color: #a3e2c9; font-size: 14px;\">Your registration was successful</p>\n" +
                                            "                        </td>\n" +
                                            "                    </tr>\n" +
                                            "\n" +
                                            "                    <!-- Body Content -->\n" +
                                            "                    <tr>\n" +
                                            "                        <td style=\"padding: 40px 30px;\">\n" +
                                            "                            <h2 style=\"margin: 0 0 20px 0; color: #1b4d3e; font-size: 22px; font-weight: bold;\">Hello there, " + firstName + "," + "</h2>\n" +
                                            "                            <p style=\"margin: 0 0 20px 0; color: #2d3732; font-size: 15px; line-height: 1.6;\">\n" +
                                            "                                Thank you for creating an account with us. We are thrilled to have you here! Your profile is officially set up and ready to use. Explore your new personalized main dashboard to get started.\n" +
                                            "                            </p>\n" +
                                            "\n" +
                                            "                            <p style=\"margin: 30px 0 0 0; color: #718078; font-size: 13px; border-top: 1px solid #e6ece8; padding-top: 20px;\">\n" +
                                            "                                If you did not create this account, please ignore this email or contact support.\n" +
                                            "                            </p>\n" +
                                            "                        </td>\n" +
                                            "                    </tr>\n" +
                                            "\n" +
                                            "                    <!-- Footer -->\n" +
                                            "                    <tr>\n" +
                                            "                        <td style=\"background-color: #f1f6f3; padding: 20px 30px; text-align: center; font-size: 12px; color: #4a5a51;\">\n" +
                                            "                            &copy; 2026 Your Brand Name. All rights reserved.\n" +
                                            "                        </td>\n" +
                                            "                    </tr>\n" +
                                            "                </table>\n" +
                                            "            </td>\n" +
                                            "        </tr>\n" +
                                            "    </table>\n" +
                                            "</body>\n" +
                                            "</html>\n";
                                    Mailer.sendEmail(contactInfo, "Welcome to SocNet!", htmlContent);
//                                    System.out.println("Welcome email sending...");
//                                    throw new MessagingException();
                                } catch (MessagingException e) {
                                    System.out.println("Email sending failed: " + e.getMessage());
                                } catch (MailerException e) {
                                    System.out.println("[Mailer Exception]: " + e.getMessage());
                                }
                            }).start();
                        }

                    } else {
                        errorLabel.setText("Can't create account. Please, try again later...");
                    }
                }
                else {
                    errorLabel.setText("User with this email/phone already exist.");
                }
            } catch (SQLException err) {
                System.out.println(err.getMessage());
                errorLabel.setText("Something went wrong... Please, try again later.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                errorLabel.setText("Something went wrong while redirecting... Please, reload application");
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
                errorLabel.setText("Something went wrong while serializing Token... Try again later!");
            } catch (AuthUserNotFoundException e) {
                System.out.println("User with this credentials not found");
                errorLabel.setText("Something went wrong while verifying user. Please, try again");
            }

        }
        buttonClicked = false;
    }

    @FXML
    private void navigateToLogin(ActionEvent event) throws IOException {
        Utils.changeScene(event, "login-page");
    }


//
    private boolean validateForm() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        if(firstName.isEmpty() || lastName.isEmpty()) {
            errorLabel.setText("Enter your first and last name!");
            return false;
        }
        if(firstName.length() < 2 || lastName.length() < 2 ||
            firstName.length() > 31 || lastName.length() > 31){
            errorLabel.setText("First and last names must be between 2 and 31 characters long");
            return false;
        }

        String contactInfo = contactInfoField.getText();
        if(contactInfo.isEmpty()) {
            errorLabel.setText("Enter contact info (email/phone)!");
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

        if(birthDateField.getValue() == null) {
            errorLabel.setText("Select birth date!");
            return false;
        }
        if(!maleGenderRB.isSelected() && !femaleGenderRB.isSelected()) {
            errorLabel.setText("Select your gender!");
            return false;
        }

        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if(password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please enter & confirm your password!");
            return false;
        }
        if(password.length() < 6 || password.length() > 20) {
            errorLabel.setText("Password must be between 6 and 20 characters long!");
            return false;
        }
        boolean passwordContainsCapital = false;
        for (int i = 0; i < password.length(); i++) {
            if(password.charAt(i) >= 'A' && password.charAt(i) <= 'Z') {
                passwordContainsCapital = true;
                break;
            }
        }
        if(!passwordContainsCapital) {
            errorLabel.setText("Password must contain minimum 1 Capital Letter!");
            return false;
        }
        if(!password.equals(confirmPassword)) {
            errorLabel.setText("Password and Confirm Password do not match!");
            return false;
        }
        return true;
    }
}
