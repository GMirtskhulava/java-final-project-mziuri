package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.finalproject.GoogleAuth;
import org.example.finalproject.MySQL;
import org.example.finalproject.User;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SetupGAuthController implements Initializable {
    private boolean clickedSetup;
    @FXML
    private TextField authCodeField;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView qrCodeImageV;
    @FXML private Label secretKeyLabel;

    @FXML
    private Button setupButton;

    private String secretKey;

    @FXML
    public void handleSetupButton(ActionEvent event) {
        if(clickedSetup) return;
        if(authCodeField.getText().isEmpty()) {
            errorLabel.setText("Enter Code");
            return;
        }
        errorLabel.setText("");
        String codeText = authCodeField.getText();
        if(codeText.length() != 6) {
            errorLabel.setText("Code must contains 6 numbers");
            return;
        }

        clickedSetup = true;
        int code = Integer.parseInt(codeText);
        boolean isValid = GoogleAuth.checkAuthCode(secretKey, code);
        if(!isValid) {
            errorLabel.setText("Code is incorrect!");
        }
        else {
            errorLabel.setText("Successfully Setuped 2FA!");
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET totpEnabled = true, totpKey = ? WHERE id = ?");
                preparedStatement.setString(1, secretKey);
                preparedStatement.setInt(2, User.currentUser.getID());
                preparedStatement.executeUpdate();
                Utils.changeScene(event, "settings-page");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                errorLabel.setText("Something went wrong while saving data. Please try again later!");
                System.out.println(e.getMessage());
            }
        }

        clickedSetup = false;
    }

    @FXML
    void backToSettings(ActionEvent event) {
        try {
//            Utils.changeScene(event, "profile-page");
            Utils.changeScene(event, "settings-page");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GoogleAuth authG = GoogleAuth.generateGAuth(User.currentUser.getContactInfo());
        secretKey = authG.getSecretKey();

        Platform.runLater(() -> {
            qrCodeImageV.setImage(new Image(authG.getQrUrl()));
            secretKeyLabel.setText("Secret Key: " + authG.getSecretKey());
        });
        clickedSetup = false;

    }
}
