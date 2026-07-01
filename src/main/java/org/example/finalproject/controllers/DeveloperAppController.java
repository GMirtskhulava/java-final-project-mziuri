package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.example.finalproject.MySQL;
import org.example.finalproject.User;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DeveloperAppController implements Initializable {

    @FXML
    private TextField appNameField;

    @FXML
    private TextArea javaCodeArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private void handleLogOutButton(ActionEvent event) {

        try {
            User.logoutActiveUser();
            Utils.changeScene(event, "login-page");
        } catch (SQLException | IOException err) {
            System.out.println(err.getMessage());
        }
    }

    @FXML
    void handlePublishButton(ActionEvent event) {
        String appName = appNameField.getText();
        if(appName.isEmpty()) {
            messageLabel.setText("Enter APP name!!");
            return;
        }
        String javaCode = javaCodeArea.getText();
        if(javaCode.isEmpty()) {
            messageLabel.setText("App code is empty!!");
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE developer_apps SET status = 2 WHERE id = ?");
                preparedStatement.setInt(1, Utils.appID);
                preparedStatement.executeUpdate();

                Platform.runLater(() -> {
                    messageLabel.setText("Successfully sent on reviewing!");
                    loadApp();
                });
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> messageLabel.setText("Can't send on review:("));
            }
        });
        thread.start();
    }

    @FXML
    void handleSaveButton(ActionEvent event) {
        String appName = appNameField.getText();
        if(appName.isEmpty()) {
            messageLabel.setText("Enter APP name!!");
            return;
        }
        String javaCode = javaCodeArea.getText();

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE developer_apps SET appName = ?, code = ? WHERE id = ?");
                preparedStatement.setString(1, appName);
                preparedStatement.setString(2, javaCode.isEmpty() ? "" : javaCode);
                preparedStatement.setInt(3, Utils.appID);
                preparedStatement.executeUpdate();

                Platform.runLater(() -> messageLabel.setText("Successfully saved!") );
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> messageLabel.setText("Can't save:("));
            }
        });
        thread.start();
    }

    @FXML
    void openMyProfile(MouseEvent event) throws IOException {
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeSceneFromNode((javafx.scene.Node) event.getSource(), "profile-page");
    }

    @FXML
    void showDashboard(ActionEvent event) throws IOException{
        Utils.changeScene(event, "developer-page");
    }

    @FXML
    private void showProfile(ActionEvent event) throws IOException {
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeScene(event, "profile-page");
    }


    public void loadApp() {
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT * FROM developer_apps WHERE id = ? AND userID = ?");
                preparedStatement.setInt(1, Utils.appID);
                preparedStatement.setInt(2, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()) {
                    String appName1 = resultSet.getString("appName");
                    int appstatus = resultSet.getInt("status");
                    String code = resultSet.getString("code");

                    String statusStr;
                    switch(appstatus) {
                        case 0: {
                            statusStr = "draft";
                            break;
                        }
                        case 1: {
                            statusStr = "confirmed";
                            break;
                        }
                        case 2: {
                            statusStr = "reviewing";
                            javaCodeArea.setDisable(true);
                            appNameField.setDisable(true);
                            break;
                        }
                        default: {
                            statusStr = "undefined";
                            break;
                        }
                    }
                    Platform.runLater(() -> {
                        appNameField.setText(appName1);
                        statusLabel.setText(statusStr);
                        javaCodeArea.setText(code);
                    });
                }
                else {
                    Utils.changeSceneFromNode(appNameField, "developer-page");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                try {
                    Utils.changeSceneFromNode(appNameField, "developer-page");
                } catch (IOException ex) {
                    System.out.println(e.getMessage());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        });
        thread.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topUsernameLabel.setText(User.currentUser.getFullName());
        loadApp();

    }

}
