package org.example.finalproject.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class GamesController implements Initializable {
    private boolean logoutButtonClicked = false;

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private TableView<User> leaderboardTable;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, Integer> pongScoresColumn;

    private ObservableList<User> leadeboardList = FXCollections.observableArrayList();

    @FXML
    private void openMyProfile(MouseEvent event) throws IOException {
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeSceneFromNode((javafx.scene.Node) event.getSource(), "profile-page");
    }

    @FXML
    private void showFeed(ActionEvent event) throws IOException {
        Utils.changeScene(event, "main-page");
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

    @FXML
    private void openPongGame(ActionEvent event) throws IOException {
        Utils.changeScene(event, "pong-game");
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

    private void loadLeaderboard() {
        leadeboardList.clear();

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname, totalPongScores FROM users WHERE totalPongScores > 0 ORDER BY totalPongScores DESC, firstname ASC, lastname ASC LIMIT 12");
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    int userID = resultSet.getInt("id");
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");
                    int totalPongScores = resultSet.getInt("totalPongScores");
                    leadeboardList.add(new User(userID, firstname, lastname, totalPongScores));
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });
        thread.start();
    }

    private String getFullName(String firstname, String lastname) {
        String firstName = firstname == null ? "" : firstname.trim();
        String lastName = lastname == null ? "" : lastname.trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoutButtonClicked = false;
        topUsernameLabel.setText(getDisplayName());
        Utils.showFriendRequestsMark(navFriendsBtn);

        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        pongScoresColumn.setCellValueFactory(new PropertyValueFactory<>("totalPongScores"));
        leaderboardTable.setItems(leadeboardList);
        loadLeaderboard();
    }

}
