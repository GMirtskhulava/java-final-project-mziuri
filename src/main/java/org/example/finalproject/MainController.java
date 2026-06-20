package org.example.finalproject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

//(SELECT id FROM blocks b WHERE (b.blockerID = ? AND b.blockedID = users.id) OR (b.blockerID = users.id AND b.blockedID = ?))
public class MainController implements Initializable {
    private boolean logoutButtonClicked = false, postButtonPressed = false;

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private Label postErrorLabel;

    @FXML
    private TextArea postTextArea;

    @FXML
    private VBox suggestionsBox;

    @FXML
    private VBox postsFeedContainer;

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
    private void handlePostButton(ActionEvent event) {
        if(postButtonPressed) return;
        postButtonPressed = true;
        postErrorLabel.setText("");
//
        try {
            Post tmpPost = new Post(User.currentUser.getID(), postTextArea.getText(), LocalDateTime.now());
            Post createdPost = Post.createUserPost(User.currentUser, tmpPost);

            if(createdPost != null) {
                postTextArea.clear();
                postErrorLabel.setText("Post created successfully!");
                loadFeedPosts();
            } else {
                postErrorLabel.setText("Post was not created.");
            }

        } catch (SQLException e) {
            System.out.println("[SQL ERR | POST]" + e.getMessage());
            postErrorLabel.setText("Something went wrong... Please try again later");
        } catch (Exception e) {
            postErrorLabel.setText(e.getMessage());
        } finally {
            postButtonPressed = false;
        }
    }

    @FXML
    void handlePostClearButton() {
        if(!postTextArea.getText().isEmpty()) postTextArea.setText("");
        if(!postErrorLabel.getText().isEmpty()) postErrorLabel.setText("");
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
    private void loadSuggestions() {
        if(suggestionsBox == null) return;

        Thread thread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT userID, friendID FROM friends WHERE userID = ? OR friendID= ? ORDER BY createdDate DESC LIMIT 5;");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();
                int[] myfriendsarr = new int[5];
                int mfidx = 0;
                while(resultSet.next()) {
                    int tmpUID = resultSet.getInt("userID");
                    int tmpFriendID = resultSet.getInt("friendID");
                    if(tmpUID == User.currentUser.getID()) {
                        myfriendsarr[mfidx] = tmpFriendID;
                    } else {
                        myfriendsarr[mfidx] = tmpUID;
                    }
                    mfidx++;
                }

//
                ArrayList<String[]> sugUsers = new ArrayList<>();
                for (int i = 0; i < myfriendsarr.length; i++) {
                    if(myfriendsarr[i] == 0) continue;
                    PreparedStatement preparedStatement1 = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM friends f LEFT JOIN blocks b ON ( " +
                                    "(b.blockerID = ? AND b.blockedID = f.userID) OR (b.blockerID = f.userID AND b.blockedID = ?) OR (b.blockerID = ? AND b.blockedID = f.friendID) OR (b.blockerID = f.friendID AND b.blockedID = ?) ) " +
                                    "JOIN users u ON (u.id = f.userID OR u.id = f.friendID) WHERE f.status = 'accepted' AND (f.userID = ? OR f.friendID = ?) AND b.id IS NULL AND u.id != ? AND u.id != ? LIMIT 2");

                    for (int j = 1; j <= 4; j++) {
                        if(j <= 3) preparedStatement1.setInt(4+j, myfriendsarr[i]);
                        preparedStatement1.setInt(j, User.currentUser.getID());
                    }
                    preparedStatement1.setInt(8, User.currentUser.getID());


                    ResultSet resultSet1 = preparedStatement1.executeQuery();

                    while(resultSet1.next()) {
                        sugUsers.add(new String[]{ "" + resultSet1.getInt("id"), resultSet1.getString("firstname"), resultSet1.getString("lastname") });
                    }
                }

                Platform.runLater(() -> {
                    suggestionsBox.getChildren().clear();

                    if(sugUsers.size() == 0) {
                        Label label = new Label("No suggestions yet.");
                        label.getStyleClass().add("label-light");
                        suggestionsBox.getChildren().add(label);
                    } else {
                        for (String[] su : sugUsers) {
                            suggestionsBox.getChildren().add(makeSuggestionRow( Integer.parseInt(su[0]), su[1], su[2]));
                        }
                    }
                });


            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });

        thread.start();
    }

    private void loadFeedPosts() {
        if(postsFeedContainer == null) return;

        Thread thread = new Thread(() -> {
            ArrayList<String[]> posts = new ArrayList<>();

            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT p.id, p.authorID, p.content, p.createdDate, u.firstname, u.lastname FROM posts p JOIN users u ON p.authorID = u.id " +
                            "WHERE NOT EXISTS (SELECT id FROM blocks b WHERE (b.blockerID = ? AND b.blockedID = p.authorID) OR (b.blockerID = p.authorID AND b.blockedID = ?)) " +
                            "ORDER BY p.createdDate DESC LIMIT 50");

//                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT p.id, p.authorID, p.content, p.createdDate, u.firstname, u.lastname FROM posts p JOIN users u ON p.authorID = u.id ORDER BY p.createdDate DESC LIMIT 50");

                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    posts.add(new String[]{ "" + resultSet.getInt("id"), "" + resultSet.getInt("authorID"), "post", resultSet.getString("firstname"), resultSet.getString("lastname"), resultSet.getString("createdDate"), resultSet.getString("content"), "", "0"});
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT s.id as shareID, s.createdDate as shareDate, s.userID as sharedUserID, su.firstname as sharedFirstName, su.lastname as sharedLastName, p.id, p.authorID, p.content, u.firstname, u.lastname FROM post_shares s " +
                                "JOIN posts as p ON s.postID = p.id JOIN users as u ON p.authorID = u.id JOIN users as su ON s.userID = su.id " +
                                "WHERE NOT EXISTS (SELECT id FROM blocks b WHERE (b.blockerID = ? AND (b.blockedID = p.authorID OR b.blockedID = s.userID)) OR ((b.blockerID = p.authorID OR b.blockerID = s.userID) AND b.blockedID = ?)) " +
                                "ORDER BY s.createdDate DESC LIMIT 50");

                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, User.currentUser.getID());
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
//                    String sharedBy = (resultSet.getString("sharedFirstName") + " " + resultSet.getString("sharedLastName")).trim();
                    posts.add(new String[]{ "" + resultSet.getInt("id"), "" + resultSet.getInt("sharedUserID"), "share", resultSet.getString("sharedFirstName"), resultSet.getString("sharedLastName"), resultSet.getString("shareDate"), resultSet.getString("content"), (resultSet.getString("firstname") + " " + resultSet.getString("lastname")).trim(), "" + resultSet.getInt("shareID") });
                }

                if(posts.size() > 50) {
                    posts.subList(50, posts.size()).clear();
                }

                Platform.runLater(() -> {
                    postsFeedContainer.getChildren().clear();

                    if(posts.size() == 0) {
                        Label label = new Label("No posts ფound");
                        label.getStyleClass().add("label-light");
                        postsFeedContainer.getChildren().add(label);
                    }

                    for(String[] post : posts)
                        postsFeedContainer.getChildren().add(makePostBox(Integer.parseInt(post[0]), Integer.parseInt(post[1]), post[2], post[3], post[4], post[5], post[6], post[7], Integer.parseInt(post[8])));
                });

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });

        thread.start();
    }

    private VBox makePostBox(int postID, int authorID, String type, String firstname, String lastname, String createdDate, String content, String sharedBy, int shareID) {
        VBox box = new VBox();
        box.setSpacing(12);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(15, 20, 15, 20));

        HBox top = new HBox();
        top.setSpacing(12);

        Circle circle = new Circle();
        circle.setRadius(20);
        circle.setStyle("-fx-fill: #1b4d3e;");

        VBox nameBox = new VBox();
        Label nameLabel = new Label((firstname + " " + lastname).trim());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b4d3e;");
        nameLabel.setOnMouseClicked(event -> {
            try {
                Utils.profileUserID = authorID;
                Utils.changeSceneFromNode(nameLabel, "profile-page");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
        Label dateLabel = new Label(createdDate == null ? "" : createdDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        nameBox.getChildren().add(nameLabel);
        nameBox.getChildren().add(dateLabel);

        top.getChildren().add(circle);
        top.getChildren().add(nameBox);

        Label shareLabel = new Label("");
        shareLabel.getStyleClass().add("label-light");
        if("share".equals(type)) {
            shareLabel.setText("SHARED [Original: " + sharedBy + "]");
        }

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("post-text");
        contentLabel.setWrapText(true);

        Label countersLabel = new Label(getPostCounters(postID, shareID));
        countersLabel.getStyleClass().add("label-light");

        HBox buttons = new HBox();
        buttons.setSpacing(15);

        Button likeButton = new Button(userLikedPost(postID, shareID) ? "👎 dislike" : "👍 Like");
        likeButton.getStyleClass().add("btn-secondary");
        likeButton.setOnAction(event -> toggleLikePost(postID, shareID));

        buttons.getChildren().add(likeButton);

        if(!"share".equals(type)) {
            Button shareButton = new Button("Share");
            shareButton.getStyleClass().add("btn-secondary");
            shareButton.setOnAction(event -> sharePost(postID));
            buttons.getChildren().add(shareButton);
        }

        if(authorID == User.currentUser.getID()) {
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("btn-logout");
            if("share".equals(type)) {
                deleteButton.setOnAction(event -> deleteShare(shareID));
            } else {
                deleteButton.setOnAction(event -> deletePost(postID));
            }
            buttons.getChildren().add(deleteButton);
        }

        box.getChildren().add(top);
        if("share".equals(type)) {
            box.getChildren().add(shareLabel);
        }
        box.getChildren().add(contentLabel);
        box.getChildren().add(countersLabel);
        box.getChildren().add(new Separator());
        box.getChildren().add(buttons);

        return box;
    }

    private String getPostCounters(int postID, int shareID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM post_likes WHERE postID = ? AND shareID = ?");
            preparedStatement.setInt(1, postID);
            preparedStatement.setInt(2, shareID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int likes = 0;
            if(resultSet.next()) likes = resultSet.getInt(1);

            preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM post_shares WHERE postID = ?");
            preparedStatement.setInt(1, postID);
            resultSet = preparedStatement.executeQuery();
            int shares = 0;
            if(resultSet.next()) shares = resultSet.getInt(1);

            return likes + " likes | " + shares + " shares";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    private boolean userLikedPost(int postID, int shareID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id FROM post_likes WHERE postID = ? AND shareID = ? AND userID = ? LIMIT 1");
            preparedStatement.setInt(1, postID);
            preparedStatement.setInt(2, shareID);
            preparedStatement.setInt(3, User.currentUser.getID());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void toggleLikePost(int postID, int shareID) {
        try {
            if(userLikedPost(postID, shareID)) {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM post_likes WHERE postID = ? AND shareID = ? AND userID = ?");
                preparedStatement.setInt(1, postID);
                preparedStatement.setInt(2, shareID);
                preparedStatement.setInt(3, User.currentUser.getID());
                preparedStatement.executeUpdate();
            } else {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO post_likes (postID, shareID, userID, createdDate) VALUES (?, ?, ?, ?)");
                preparedStatement.setInt(1, postID);
                preparedStatement.setInt(2, shareID);
                preparedStatement.setInt(3, User.currentUser.getID());
                preparedStatement.setObject(4, LocalDateTime.now());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadFeedPosts();
    }

    private void sharePost(int postID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO post_shares (postID, userID, createdDate) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, postID);
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.setObject(3, LocalDateTime.now());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadFeedPosts();
    }

    private void deletePost(int postID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM post_likes WHERE postID = ?");
            preparedStatement.setInt(1, postID);
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM post_shares WHERE postID = ?");
            preparedStatement.setInt(1, postID);
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM posts WHERE id = ? AND authorID = ?");
            preparedStatement.setInt(1, postID);
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadFeedPosts();
    }

    private void deleteShare(int shareID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM post_likes WHERE shareID = ?");
            preparedStatement.setInt(1, shareID);
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM post_shares WHERE id = ? AND userID = ?");
            preparedStatement.setInt(1, shareID);
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        loadFeedPosts();
    }

    private HBox makeSuggestionRow(int userID, String firstname, String lastname) {
        HBox row = new HBox();
        row.setSpacing(12);

        VBox textBox = new VBox();
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label nameLabel = new Label((firstname + " " + lastname).trim());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3732;");

        Label infoLabel = new Label("Friend of friend");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(infoLabel);

        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setOnAction(event -> showProfileByID(event, userID));


        Button addButton = new Button("Add");
        addButton.getStyleClass().add("btn-secondary");
        addButton.setOnAction(event -> sendFriendRequest(userID, addButton));

        row.getChildren().add(textBox);
        row.getChildren().add(viewButton);
        row.getChildren().add(addButton);

        return row;
    }

    private void sendFriendRequest(int userID, Button button) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO friends (userID, friendID, status, createdDate) VALUES (?, ?, ?, ?)");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, userID);
            preparedStatement.setString(3, "pending");
            preparedStatement.setObject(4, LocalDateTime.now());
            preparedStatement.executeUpdate();

            button.setText("Sent");
            button.setDisable(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            button.setText("Sent");
            button.setDisable(true);
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topUsernameLabel.setText(getDisplayName());
        logoutButtonClicked = false;
        postButtonPressed = false;
        Utils.showFriendRequestsMark(navFriendsBtn);

        if(suggestionsBox != null) {
            suggestionsBox.getChildren().clear();
            loadSuggestions();
        }

        loadFeedPosts();
    }

}
