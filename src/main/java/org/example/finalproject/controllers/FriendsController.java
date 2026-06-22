package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;
import org.example.finalproject.MySQL;
import org.example.finalproject.User;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FriendsController implements Initializable {
    private boolean logoutButtonClicked = false;
    private int friendsUserID;
    private String friendsUserName = "";

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private VBox friendsContainer;

    @FXML
    private Label friendsTitleLabel;



    @FXML
    private void showFeed(ActionEvent event) throws IOException {
        Utils.changeScene(event, "main-page");
    }

    @FXML
    private void openMyProfile(MouseEvent event) throws IOException {
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeSceneFromNode((javafx.scene.Node) event.getSource(), "profile-page");
    }

    @FXML
    private void showProfile(ActionEvent event) throws IOException {
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeScene(event, "profile-page");
    }

    @FXML
    private void showMessages(ActionEvent event) throws IOException {
        Utils.changeScene(event, "messages-page");
    }

    @FXML
    private void showFriends(ActionEvent event) throws IOException {
        Utils.friendsUserID = User.currentUser.getID();
        Utils.changeScene(event, "friends-page");
    }

    @FXML
    private void showGames(ActionEvent event) throws IOException {
        Utils.changeScene(event, "games-page");
    }

    @FXML
    private void showSettings(ActionEvent event) throws IOException {
        Utils.changeScene(event, "settings-page");
    }

    @FXML
    private void showSearch(ActionEvent event) throws IOException {
        Utils.searchText = searchField.getText();
        Utils.changeScene(event, "search-page");
    }

    private void showProfileByID(ActionEvent event, int userID) {
        try {
            Utils.profileUserID = userID;
            Utils.changeScene(event, "profile-page");
        } catch (IOException e) {
            System.out.println(e.getMessage());;
        }
    }


    @FXML
    private void handleLogOutButton(ActionEvent event) {
        if(logoutButtonClicked) return;
        logoutButtonClicked = true;
        try {
            User.logoutActiveUser();
            Utils.changeScene(event, "login-page");
        } catch (SQLException | IOException err) {
            System.out.println(err.getMessage());
        } finally {
            logoutButtonClicked = false;
        }
    }


    private String getDisplayName() {
        User user = User.currentUser;
        if(user == null) {
            return "undefined";
        }

        return user.getFullName();
    }

    private void loadFriends() {
        Utils.showFriendRequestsMark(navFriendsBtn);

        Thread thread = new Thread(() -> {
            ArrayList<String[]> friends = new ArrayList<>();
            ArrayList<String[]> requests = new ArrayList<>();

            try {
                if(Utils.friendsUserID == 0) {
                    friendsUserID = User.currentUser.getID();
                } else friendsUserID = Utils.friendsUserID;

                PreparedStatement userStatement = MySQL.connection.prepareStatement("SELECT firstname, lastname, friendListPrivate FROM users WHERE id = ?");
                userStatement.setInt(1, friendsUserID);
                ResultSet userResult = userStatement.executeQuery();

                boolean privateFriends = false;
                if(userResult.next()) {
                    friendsUserName = (userResult.getString("firstname") + " " + userResult.getString("lastname")).trim();
                    privateFriends = userResult.getBoolean("friendListPrivate");
                }

                if(privateFriends && friendsUserID != User.currentUser.getID()) {
                    Platform.runLater(() -> {
                        friendsContainer.getChildren().clear();
                        friendsTitleLabel.setText("Friends of " + friendsUserName);
                        Label label = new Label("This friend list is private.");
                        label.getStyleClass().add("label-light");
                        friendsContainer.getChildren().add(label);
                    });
                    return;
                }

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname, u.contactInfo, u.hideContactInfo FROM friends f " +
                                "JOIN users u ON (u.id = f.friendID AND f.userID = ?) OR (u.id = f.userID AND f.friendID = ?) WHERE f.status = 'accepted' ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, friendsUserID);
                preparedStatement.setInt(2, friendsUserID);
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    int friendID = resultSet.getInt("id");
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");
                    String contactInfo = resultSet.getString("contactInfo");
                    if(resultSet.getBoolean("hideContactInfo") && friendID != User.currentUser.getID()) {
                        contactInfo = "Contact info hidden";
                    }

                    friends.add(new String[]{ "" + friendID, firstname, lastname, contactInfo});
                }

                if(friendsUserID == User.currentUser.getID()) {
                    preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname, u.contactInfo, u.hideContactInfo FROM friends f JOIN users u ON u.id = f.userID WHERE f.friendID = ? AND f.status = 'pending' ORDER BY f.createdDate DESC");
//                    preparedStatement = MySQL.connection.prepareStatement("SELECT userID, friendID FROM friends WHERE friendID = ?");
                    preparedStatement.setInt(1, User.currentUser.getID());
                    resultSet = preparedStatement.executeQuery();

                    while(resultSet.next()) {
                        String contactInfo = resultSet.getString("contactInfo");
                        if(resultSet.getBoolean("hideContactInfo")) {
                            contactInfo = "Contact info hidden";
                        }
                        requests.add(new String[]{ "" + resultSet.getInt("id"), resultSet.getString("firstname"), resultSet.getString("lastname"), contactInfo });
                    }
                }

                Platform.runLater(() -> {
                    friendsContainer.getChildren().clear();
                    if(friendsUserID == User.currentUser.getID()) {
                        friendsTitleLabel.setText("All Friends (" + friends.size() + ")");
                    } else {
                        friendsTitleLabel.setText("Friends of " + friendsUserName + " (" + friends.size() + ")");
                    }

                    if(requests.size() > 0) {
                        Label requestLabel = new Label("Friend Requests");
                        requestLabel.getStyleClass().add("section-title");
                        friendsContainer.getChildren().add(requestLabel);

                        for(String[] request : requests) {
                            friendsContainer.getChildren().add(makeRequestRow(Integer.parseInt(request[0]), request[1], request[2], request[3]));
                        }
                    }

                    Label friendsLabel = new Label("Friends");
                    friendsLabel.getStyleClass().add("section-title");
                    friendsContainer.getChildren().add(friendsLabel);

                    if(friends.size() == 0) {
                        Label label = new Label("Friends not found");
                        label.getStyleClass().add("label-light");
                        friendsContainer.getChildren().add(label);
                    }

                    for(String[] friend : friends) {
                        friendsContainer.getChildren().add(makeFriendRow(Integer.parseInt(friend[0]),  friend[1], friend[2], friend[3]));
                    }
                });

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    friendsContainer.getChildren().clear();
                    friendsTitleLabel.setText("All Friends");
                    Label label = new Label("Friends could not be loaded");
                    label.getStyleClass().add("label-light");
                    friendsContainer.getChildren().add(label);
                });
            }
        });

        thread.start();
    }

    private HBox makeFriendRow(int friendID, String firstname, String lastname, String contactInfo) {
        HBox row = new HBox();
        row.setSpacing(15);
        row.getStyleClass().add("friend-row");
        row.setPadding(new Insets(15, 15, 15, 15));

        Circle circle = new Circle();
        circle.setRadius(22);
        circle.setStyle("-fx-fill: #2ec4b6;");

        VBox textBox = new VBox();
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label((firstname + " " + lastname).trim());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1b4d3e;");

        Label infoLabel = new Label(contactInfo);
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(infoLabel);

        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setOnAction(event -> showProfileByID(event, friendID));

        Button unfriendButton = new Button("Unfriend");
        unfriendButton.getStyleClass().add("btn-logout");
        unfriendButton.setStyle("-fx-padding: 6 12 6 12; -fx-font-size: 12px;");
        unfriendButton.setOnAction(event -> deleteFriend(friendID));

        row.getChildren().add(circle);
        row.getChildren().add(textBox);
        row.getChildren().add(viewButton);
        if(friendsUserID == User.currentUser.getID()) {
            row.getChildren().add(unfriendButton);
        }

        return row;
    }

    private HBox makeRequestRow(int userID, String firstname, String lastname, String contactInfo) {
        HBox row = makeFriendRow(userID, firstname, lastname, contactInfo);
        row.getChildren().remove(row.getChildren().size() - 1);

        Button approveButton = new Button("Approve");
        approveButton.getStyleClass().add("btn-primary");
        approveButton.setStyle("-fx-padding: 6 12 6 12; -fx-font-size: 12px;");
        approveButton.setOnAction(event -> approveFriend(userID));

        Button declineButton = new Button("Decline");
        declineButton.getStyleClass().add("btn-logout");
        declineButton.setStyle("-fx-padding: 6 12 6 12; -fx-font-size: 12px;");
        declineButton.setOnAction(event -> deleteFriend(userID));

        row.getChildren().add(approveButton);
        row.getChildren().add(declineButton);

        return row;
    }

    private void approveFriend(int userID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE friends SET status = 'accepted' WHERE userID = ? AND friendID = ? AND status = 'pending'");
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();
            loadFriends();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteFriend(int friendID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?)");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, friendID);
            preparedStatement.setInt(3, friendID);
            preparedStatement.setInt(4, User.currentUser.getID());
            preparedStatement.executeUpdate();
            loadFriends();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoutButtonClicked = false;
        topUsernameLabel.setText(getDisplayName());
        friendsContainer.getChildren().clear();
        Utils.showFriendRequestsMark(navFriendsBtn);
        loadFriends();

    }

}
