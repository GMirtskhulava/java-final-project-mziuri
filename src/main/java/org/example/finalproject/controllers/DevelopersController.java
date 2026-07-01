package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.finalproject.MySQL;
import org.example.finalproject.User;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DevelopersController implements Initializable {

    @FXML
    private Label topUsernameLabel;

    @FXML private Label messageLabel;
    @FXML
    private VBox appListNode;


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
    void handleCreateNewGameButton(ActionEvent event) {
        messageLabel.setText("");

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM developer_apps WHERE userID = ?;");
                preparedStatement.setInt(1, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();
                int count = 0;
                if(resultSet.next()) {
                    count = resultSet.getInt(1);
                }
                if(count == 2) {
                    Platform.runLater(() -> {
                        messageLabel.setText("You have App limit! Maximum 2");
                    });
                    return;
                }
                else {
//                    Generated keys not requested. You need to specify Statement.RETURN_GENERATED_KEYS to Statement.executeUpdate(), Statement.executeLargeUpdate() or Connection.prepareStatement().
                    preparedStatement = MySQL.connection.prepareStatement("INSERT INTO developer_apps (userID) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    preparedStatement.setInt(1, User.currentUser.getID());
                    preparedStatement.executeUpdate();
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if(generatedKeys.next()) {
                        Utils.appID = generatedKeys.getInt(1);
                        Platform.runLater(() -> {
                            try {
                                    Utils.changeScene(event, "developer-app");
                            } catch (IOException e){
                                System.out.println(e.getMessage());
                                messageLabel.setText("New app created successfully. Refresh page!");
                            }
                        });
                    } else {
                        messageLabel.setText("Can't create new app!");
                    }


                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        });
        thread.start();
    }


    private void loadAppList () {
        if(appListNode == null) return;
        Map<Integer, String> appList = new HashMap<>();

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, appName FROM developer_apps WHERE userID = ?");
                preparedStatement.setInt(1, User.currentUser.getID());
                ResultSet result = preparedStatement.executeQuery();
                while(result.next()) {
                    appList.put(result.getInt("id"), result.getString("appName"));
                }

                Platform.runLater(() -> {
                    appList.forEach((id, name) -> {
                        Button btn = new Button();
                        btn.setText(name);
                        btn.getStyleClass().add("developer-applist-item");
                        btn.setPrefWidth(240);
                        btn.setPrefHeight(150);
                        btn.setOnAction((event -> {
                            Utils.appID = id;
                            try {
                                Utils.changeScene(event, "developer-app");
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }));

                        appListNode.getChildren().add(btn);
                    });

                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }


    @FXML
    private void handleLogOutButton(ActionEvent event) {
        try {
            User.logoutActiveUser();
            Utils.changeScene(event, "login-page");
        } catch (SQLException | IOException err) {
            System.out.println(err.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topUsernameLabel.setText(User.currentUser.getFullName());

        loadAppList();

    }
}
