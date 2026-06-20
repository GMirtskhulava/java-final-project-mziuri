package org.example.finalproject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SearchController implements Initializable {
    private boolean logoutButtonClicked = false;

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private VBox peopleResultsContainer;

    @FXML
    private VBox searchPostsResultsContainer;

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
    private void showSettings(ActionEvent event) throws IOException {
        Utils.changeScene(event, "settings-page");
    }

    @FXML
    private void showSearch() {
        Utils.searchText = searchField.getText();
        loadSearchResults();
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

        String firstName = user.getFirstname() == null ? "" : user.getFirstname().trim();
        String lastName = user.getLastname() == null ? "" : user.getLastname().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }

    private void loadSearchResults() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        String search = "%" + text + "%";

        Thread thread = new Thread(() -> {
            ArrayList<String[]> people = new ArrayList<>();
            ArrayList<String[]> posts = new ArrayList<>();

            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname, contactInfo, hideContactInfo FROM users WHERE firstname LIKE ? OR lastname LIKE ? OR contactInfo LIKE ? ORDER BY firstname, lastname LIMIT 15");

                String tmpSearch = "%" + search + "%";
                preparedStatement.setString(1, tmpSearch);
                preparedStatement.setString(2, tmpSearch);
                preparedStatement.setString(3, tmpSearch);

                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    int userID = resultSet.getInt("id");
                    String status = getFriendStatus(userID);
                    String contactInfo = resultSet.getString("contactInfo");
                    if(resultSet.getBoolean("hideContactInfo")) contactInfo = "Contact info hidden";

                    people.add(new String[]{ "" + userID, resultSet.getString("firstname"), resultSet.getString("lastname"), contactInfo, status });
                }


                preparedStatement = MySQL.connection.prepareStatement("SELECT p.id, p.content, p.createdDate, u.firstname, u.lastname FROM posts p JOIN users u ON p.authorID = u.id WHERE p.content LIKE ? ORDER BY p.createdDate DESC LIMIT 30");
                preparedStatement.setString(1, "%" + search + "%");
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    posts.add(new String[]{
                            resultSet.getString("firstname"), resultSet.getString("lastname"), resultSet.getString("createdDate"), resultSet.getString("content") });
                }
                Platform.runLater(() -> showResults(people, posts));

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    peopleResultsContainer.getChildren().clear();
                    searchPostsResultsContainer.getChildren().clear();
                    Label label = new Label("Search failed");
                    label.getStyleClass().add("label-light");
                    peopleResultsContainer.getChildren().add(label);
                });
            }
        });

        thread.start();
    }

    private void showResults(ArrayList<String[]> people, ArrayList<String[]> posts) {
        peopleResultsContainer.getChildren().clear();
        searchPostsResultsContainer.getChildren().clear();

        if(people.size() == 0) {
            Label label = new Label("People not found");
            label.getStyleClass().add("label-light");
            peopleResultsContainer.getChildren().add(label);
        }

        for(String[] user : people) {
            peopleResultsContainer.getChildren().add(makeUserRow(Integer.parseInt(user[0]), user[1], user[2], user[3], user[4] ));
        }

        if(posts.size() == 0) {
            Label label = new Label("No posts found.");
            label.getStyleClass().add("label-light");
            searchPostsResultsContainer.getChildren().add(label);
        }

        for(String[] post : posts) {
            searchPostsResultsContainer.getChildren().add(makePostBox(post[0], post[1], post[2], post[3]));
        }
    }

    private HBox makeUserRow(int userID, String firstname, String lastname, String contactInfo, String status) {
        HBox row = new HBox();
        row.setSpacing(15);
        row.getStyleClass().add("result-row");
        row.setPadding(new Insets(15, 15, 15, 15));

        Circle circle = new Circle();
        circle.setRadius(22);
        circle.setStyle("-fx-fill: #2ec4b6;");

        VBox textBox = new VBox();
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label nameLabel = new Label((firstname + " " + lastname).trim());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1b4d3e;");

        Label infoLabel = new Label(contactInfo);
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(infoLabel);

        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setOnAction(event -> {
            try {
                Utils.profileUserID = userID;
                Utils.changeScene(event, "profile-page");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        Button addButton = new Button();
        addButton.getStyleClass().add("btn-primary");
        addButton.setStyle("-fx-padding: 6 15 6 15; -fx-font-size: 12px;");

        if("accepted".equals(status)) {
            addButton.setText("Friends");
            addButton.setDisable(true);
        } else if("pending".equals(status)) {
            addButton.setText("Request sent");
            addButton.setDisable(true);
        } else {
            addButton.setText("Add Friend");
            addButton.setOnAction(event -> sendFriendRequest(userID, addButton));
        }

        row.getChildren().add(circle);
        row.getChildren().add(textBox);
        row.getChildren().add(viewButton);
        row.getChildren().add(addButton);

        return row;
    }

    private VBox makePostBox(String firstname, String lastname, String createdDate, String content) {
        VBox box = new VBox();
        box.setSpacing(12);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(15, 20, 15, 20));

        HBox top = new HBox();
        top.setSpacing(12);

        Circle circle = new Circle();
        circle.setRadius(18);
        circle.setStyle("-fx-fill: #0f2e24;");

        VBox nameBox = new VBox();
        Label nameLabel = new Label((firstname + " " + lastname).trim());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b4d3e;");
        Label dateLabel = new Label(createdDate == null ? "" : createdDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        nameBox.getChildren().add(nameLabel);
        nameBox.getChildren().add(dateLabel);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("post-text");
        contentLabel.setWrapText(true);

        HBox buttons = new HBox();
        buttons.setSpacing(15);
        Button likeButton = new Button("Like");
        likeButton.getStyleClass().add("btn-secondary");
        buttons.getChildren().add(likeButton);

        top.getChildren().add(circle);
        top.getChildren().add(nameBox);
        box.getChildren().add(top);
        box.getChildren().add(contentLabel);
        box.getChildren().add(new Separator());
        box.getChildren().add(buttons);

        return box;
    }

    private String getFriendStatus(int userID) throws SQLException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT status FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?) LIMIT 1");
        preparedStatement.setInt(1, User.currentUser.getID());
        preparedStatement.setInt(2, userID);
        preparedStatement.setInt(3, userID);
        preparedStatement.setInt(4, User.currentUser.getID());
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            return resultSet.getString("status");
        }
        return "none";
    }

    private void sendFriendRequest(int userID, Button button) {
        try {
            String status = getFriendStatus(userID);
            if("accepted".equals(status)) {
                button.setText("Friends");
                button.setDisable(true);
                return;
            }
            if("pending".equals(status)) {
                button.setText("Request sent");
                button.setDisable(true);
                return;
            }

            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO friends (userID, friendID, status, createdDate) VALUES (?, ?, ?, ?)");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, userID);
            preparedStatement.setString(3, "pending");
            preparedStatement.setObject(4, LocalDateTime.now());
            preparedStatement.executeUpdate();

            button.setText("Request sent");
            button.setDisable(true);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            button.setText("Error");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoutButtonClicked = false;
        topUsernameLabel.setText(getDisplayName());
        Utils.showFriendRequestsMark(navFriendsBtn);
        searchField.setText(Utils.searchText == null ? "" : Utils.searchText);
        loadSearchResults();
    }
}
