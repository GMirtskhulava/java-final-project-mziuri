package org.example.finalproject;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {
    public static String searchText = "";
    public static int profileUserID = 0;
    public static int friendsUserID = 0;
    public static int messageUserID = 0;

    public static void showFriendRequestsMark(Button friendsButton) {
        if(friendsButton == null || User.currentUser == null) return;

        String normalText = friendsButton.getText();
        friendsButton.setText(normalText);

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM friends WHERE friendID = ? AND status = 'pending'");
                preparedStatement.setInt(1, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    int count = resultSet.getInt(1);

                    Platform.runLater(() -> {
                        if(count > 0) {
                            friendsButton.setText(normalText + " ●"); // •
                        }
                    });
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });

        thread.start();
    }

    public static void changeScene(ActionEvent event, String fxmlName) throws IOException {
        changeSceneFromNode((Node) event.getSource(), fxmlName);
    }

    public static void changeSceneFromNode(Node node, String fxmlName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Utils.class.getResource(fxmlName + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
        StringBuilder winTitle = new StringBuilder("SocNet");
        switch(fxmlName) {
            case "login-page":
                winTitle.append(" | Login");
                break;
            case "register-page":
                winTitle.append(" | Registration");
                break;
            case "main-page":
                winTitle.append(" | Feed");
                break;
            case "profile-page":
                winTitle.append(" | Profile");
                break;
            case "messages-page":
                winTitle.append(" | Messages");
                break;
            case "friends-page":
                winTitle.append(" | Friends");
                break;
            case "games-page":
                winTitle.append(" | Games");
                break;
            case "settings-page":
                winTitle.append(" | Settings");
                break;
            case "search-page":
                winTitle.append(" | Search");
                break;
            case "pong-game":
                winTitle.append(" | Pong");
                break;
            case "mysql-connect":
                winTitle.append(" | MySQL");
                break;
            default:
                break;
        }
        stage.setTitle(winTitle.toString());
    }

    // public static void changeScene(ActionEvent event, String fxmlName) throws IOException {
    //     FXMLLoader fxmlLoader = new FXMLLoader(Utils.class.getResource(fxmlName + ".fxml"));
    //     Scene scene = new Scene(fxmlLoader.load());
    //     Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    //     stage.setScene(scene);
    //     StringBuilder winTitle = new StringBuilder("SocNet");
    //     switch(fxmlName) {
    //         case "login-page":
    //             winTitle.append(" | Login");
    //             break;
    //         case "register-page":
    //             winTitle.append(" | Registration");
    //             break;
    //         case "main-page":
    //             winTitle.append(" - Connect with world");
    //             break;
    //         default:
    //             break;
    //     }
    //     stage.setTitle(winTitle.toString());
    // }

    public static void serializeObject(Object object, String path) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("data\\"+path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }
    protected static Object deserializeObject(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("data\\"+path);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        return objectInputStream.readObject();
    }
}
