package org.example.finalproject;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    private boolean logoutButtonClicked = false;

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private Label profileName;
    @FXML private Label profileBio;
    @FXML private Label profileContactInfo;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button friendActionButton;

    @FXML
    private Button messageButton;

    @FXML
    private Button blockButton;

//
    @FXML
    private Label totalPostsCounter;

    @FXML private Label totalFriendsCounter;

    @FXML
    private VBox myPostsBox;

    private int profileUserID;
    private String profileDisplayName = "";
    private String friendStatus = "none";
    private boolean requestFromProfileUser = false;
    private boolean blockedByMe = false;
    private boolean blockedMe = false;


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
        if(event.getSource() == messageButton) {
            Utils.messageUserID = profileUserID;
        } else {
            Utils.messageUserID = 0;
        }
        Utils.changeScene(event, "messages-page");
    }

    @FXML
    private void showFriends(ActionEvent event) throws IOException {
        Utils.friendsUserID = User.currentUser.getID();
        Utils.changeScene(event, "friends-page");
    }

    @FXML
    private void openProfileFriends(ActionEvent event) throws IOException {
        Utils.friendsUserID = profileUserID;
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

    @FXML
    private void handleFriendActionButton() {
        if(blockedByMe || blockedMe) return;
        if(profileUserID == User.currentUser.getID()) return;

        try {
            if("accepted".equals(friendStatus) || "pending".equals(friendStatus)) {
                if("pending".equals(friendStatus) && requestFromProfileUser) {
                    PreparedStatement acceptStatement = MySQL.connection.prepareStatement("UPDATE friends SET status = 'accepted' WHERE userID = ? AND friendID = ? AND status = 'pending'");
                    acceptStatement.setInt(1, profileUserID);
                    acceptStatement.setInt(2, User.currentUser.getID());
                    acceptStatement.executeUpdate();

                    friendStatus = "accepted";
                    requestFromProfileUser = false;
                    setFriendButtonText();
                    loadFriendsCounter();
                    return;
                }

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?)");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, profileUserID);
                preparedStatement.setInt(3, profileUserID);
                preparedStatement.setInt(4, User.currentUser.getID());
                preparedStatement.executeUpdate();

                friendStatus = "none";
                setFriendButtonText();
                loadFriendsCounter();
                return;
            }

            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO friends (userID, friendID, status, createdDate) VALUES (?, ?, ?, ?)");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, profileUserID);
            preparedStatement.setString(3, "pending");
            preparedStatement.setObject(4, LocalDateTime.now());
            preparedStatement.executeUpdate();

            friendStatus = "pending";
            requestFromProfileUser = false;
            setFriendButtonText();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleBlockButton() {
        if(profileUserID == User.currentUser.getID()) return;

        try {
            if(blockedByMe) {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM blocks WHERE blockerID = ? AND blockedID = ?;");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, profileUserID);
                preparedStatement.executeUpdate();
                blockedByMe = false;
            } else {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO blocks (blockerID, blockedID, createdDate) VALUES (?, ?, ?)");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, profileUserID);
                preparedStatement.setObject(3, LocalDateTime.now());
                preparedStatement.executeUpdate();

                preparedStatement = MySQL.connection.prepareStatement("DELETE FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?);");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, profileUserID);
                preparedStatement.setInt(3, profileUserID);
                preparedStatement.setInt(4, User.currentUser.getID());
                preparedStatement.executeUpdate();

                blockedByMe = true;
            }

            loadProfileInfo();
            loadFriendsCounter();
            loadMyPosts();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private String getDisplayName() {
        User user = User.currentUser;
        if(user == null) return "undefined";

        String firstName = user.getFirstname() == null ? "" : user.getFirstname().trim();
        String lastName = user.getLastname() == null ? "" : user.getLastname().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }



    private void loadMyPosts() {
        if(blockedByMe || blockedMe) {
            myPostsBox.getChildren().clear();
            VBox box = new VBox();
            box.getStyleClass().add("card");
            box.setPadding(new Insets(15, 20, 15, 20));
            Label label = new Label("User blocked");
            label.getStyleClass().add("label-light");
            box.getChildren().add(label);
            myPostsBox.getChildren().add(box);
            totalPostsCounter.setText("-");
            return;
        }

        Thread postsThread = new Thread(() -> {
            ArrayList<String[]> posts = new ArrayList<>();

            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT * FROM posts WHERE authorID = ? ORDER BY createdDate DESC");
                preparedStatement.setInt(1, profileUserID);
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    Timestamp time = resultSet.getTimestamp("createdDate");
                    LocalDateTime createdDate = time.toLocalDateTime();

                    posts.add(new String[]{ "" + resultSet.getInt("id"), "" + resultSet.getInt("authorID"), "post", profileDisplayName, "", createdDate.toString(), resultSet.getString("content"), "0" });
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT s.id shareID, s.createdDate, p.id, p.authorID, p.content, u.firstname, u.lastname FROM post_shares s " +
                                "JOIN posts p ON s.postID = p.id JOIN users u ON p.authorID = u.id WHERE s.userID = ? ORDER BY s.createdDate DESC;");
                preparedStatement.setInt(1, profileUserID);
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    Timestamp time = resultSet.getTimestamp("createdDate");
                    LocalDateTime createdDate = time.toLocalDateTime();
                    String name = (resultSet.getString("firstname") + " " + resultSet.getString("lastname")).trim();
                    posts.add(new String[]{ "" + resultSet.getInt("id"), "" + resultSet.getInt("authorID"), "share", profileDisplayName, name, createdDate.toString(), resultSet.getString("content"), "" + resultSet.getInt("shareID") });
                }

                posts.sort((a, b) -> b[5].compareTo(a[5]));

                Platform.runLater(() -> {
                    myPostsBox.getChildren().clear();
                    totalPostsCounter.setText("" + posts.size());

                    if(posts.size() == 0) {
                        VBox emptyBox = new VBox();
                        emptyBox.getStyleClass().add("card");
                        emptyBox.setPadding(new Insets(15, 20, 15, 20));

                        Label label = new Label("Posts nt found");
                        label.getStyleClass().add("label-light");
                        emptyBox.getChildren().add(label);

                        myPostsBox.getChildren().add(emptyBox);
                    }

                    for(String[] post : posts) {
                        myPostsBox.getChildren().add(makePostBox(Integer.parseInt(post[0]), Integer.parseInt(post[1]), post[2], post[3], post[4], post[5], post[6], Integer.parseInt(post[7])));
                    }
                });

            } catch (SQLException e) {
                System.out.println(e.getMessage());

//                Platform.runLater(() -> {
//                    myPostsBox.getChildren().clear();
//                    totalPostsCounter.setText("-");
//
//                    VBox errorBox = new VBox();
//                    errorBox.getStyleClass().add("card");
//                    errorBox.setPadding(new Insets(15, 20, 15, 20));
//
//                    Label label = new Label("Posts could not be loaded");
//                    label.getStyleClass().add("label-light");
//                    errorBox.getChildren().add(label);
//
//                    myPostsBox.getChildren().add(errorBox);
//                });
            }
        });

        postsThread.start();
    }

    private VBox makePostBox(int postID, int authorID, String type, String displayName, String originalName, String createdDate, String content, int shareID) {
        VBox postBox = new VBox();
        postBox.setSpacing(12);
        postBox.getStyleClass().add("card");
        postBox.setPadding(new Insets(15, 20, 15, 20));

        HBox userLine = new HBox();
        userLine.setSpacing(12);

        Circle circle = new Circle();
        circle.setRadius(20);
        circle.setStyle("-fx-fill: #1b4d3e;");

        VBox nameBox = new VBox();

        Label nameLabel = new Label(getDisplayName());
        nameLabel.setText(displayName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b4d3e;");

        String dateText = "";
        dateText = createdDate.replace("T", " ");

        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

        nameBox.getChildren().add(nameLabel);
        nameBox.getChildren().add(dateLabel);

        userLine.getChildren().add(circle);
        userLine.getChildren().add(nameBox);

        Label shareLabel = new Label("");
        shareLabel.getStyleClass().add("label-light");
        if("share".equals(type)) {
            shareLabel.setText("SHARED [Original: " + originalName + "]");
        }

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("post-text");
        contentLabel.setWrapText(true);

        Label countersLabel = new Label(getPostCounters(postID, shareID));
        countersLabel.getStyleClass().add("label-light");

        Separator separator = new Separator();

        HBox buttons = new HBox();
        buttons.setSpacing(15);

        Button likeButton = new Button("👍 Like");
        if(userLikedPost(postID, shareID)) {
            likeButton.setText("👎 dislike");
        }
        likeButton.getStyleClass().add("btn-secondary");
        likeButton.setOnAction(event -> toggleLikePost(postID, shareID));

        buttons.getChildren().add(likeButton);

        if(("share".equals(type) && profileUserID == User.currentUser.getID()) || (!"share".equals(type) && authorID == User.currentUser.getID())) {
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("btn-logout");
            if("share".equals(type)) {
                deleteButton.setOnAction(event -> deleteShare(shareID));
            } else {
                deleteButton.setOnAction(event -> deletePost(postID));
            }
            buttons.getChildren().add(deleteButton);
        }

        postBox.getChildren().add(userLine);
        if("share".equals(type)) postBox.getChildren().add(shareLabel);
        postBox.getChildren().add(contentLabel);
        postBox.getChildren().add(countersLabel);
        postBox.getChildren().add(separator);
        postBox.getChildren().add(buttons);

        return postBox;
    }

    private String getPostCounters(int postID, int shareID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM post_likes WHERE postID = ? AND shareID = ?");
            preparedStatement.setInt(1, postID);
            preparedStatement.setInt(2, shareID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int likes = 0;
            if(resultSet.next()) {
                likes = resultSet.getInt(1);
            }

            PreparedStatement shareStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM post_shares WHERE postID = ?");
            shareStatement.setInt(1, postID);
            ResultSet shareResult = shareStatement.executeQuery();
            int shares = 0;
            if(shareResult.next()) {
                shares = shareResult.getInt(1);
            }

            if(shareID > 0) {
                return likes + " likes";
            }

            return likes + " likes | " + shares + " shares";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    private void deletePost(int postID) {
        try {
//            Statement statement = MySQL.connection.createStatement();

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

            loadMyPosts();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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

            loadMyPosts();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean userLikedPost(int postID, int shareID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id FROM post_likes WHERE postID=? AND shareID = ? AND userID = ? LIMIT 1");
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
            loadMyPosts();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadProfileInfo() {
        try {
            if(Utils.profileUserID == 0) {
                profileUserID = User.currentUser.getID();
            } else {
                profileUserID = Utils.profileUserID;
            }

            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT firstname, lastname, bio, contactInfo, hideContactInfo, messagesFriendsOnly FROM users WHERE id=?");
            preparedStatement.setInt(1, profileUserID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                String firstname = resultSet.getString("firstname");
                String lastname = resultSet.getString("lastname");
                String bio = resultSet.getString("bio");
                String contactInfo = resultSet.getString("contactInfo");
                boolean hideContactInfo = resultSet.getBoolean("hideContactInfo");
                boolean messagesFriendsOnly = resultSet.getBoolean("messagesFriendsOnly");
                profileDisplayName = ((firstname == null ? "" : firstname) + " " + (lastname == null ? "" : lastname)).trim();
                if(profileDisplayName.isEmpty()) {
                    profileDisplayName = "undefined";
                }
                profileName.setText(profileDisplayName);
                profileBio.setText(bio == null ? "" : bio);
                if(hideContactInfo && profileUserID != User.currentUser.getID()) {
                    profileContactInfo.setText("");
                } else {
                    profileContactInfo.setText(contactInfo == null ? "" : contactInfo);
                }
            }

            boolean myProfile = profileUserID == User.currentUser.getID();
            loadBlockStatus();

            editProfileButton.setVisible(myProfile);
            editProfileButton.setManaged(myProfile);
            blockButton.setVisible(!myProfile);
            blockButton.setManaged(!myProfile);
            blockButton.setText(blockedByMe ? "Unblock" : "Block");

            if(blockedByMe || blockedMe) {
                profileBio.setText("User blocked");
                profileContactInfo.setText("");
                friendActionButton.setVisible(false);
                friendActionButton.setManaged(false);
                messageButton.setVisible(false);
                messageButton.setManaged(false);
                return;
            }

            friendActionButton.setVisible(!myProfile);
            friendActionButton.setManaged(!myProfile);

            if(!myProfile) {
                friendStatus = getFriendStatus();
                setFriendButtonText();
                boolean canMessage = true;
                preparedStatement = MySQL.connection.prepareStatement("SELECT messagesFriendsOnly FROM users WHERE id = ?");
                preparedStatement.setInt(1, profileUserID);
                resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    canMessage = !resultSet.getBoolean("messagesFriendsOnly") || "accepted".equals(friendStatus);
                }
                messageButton.setVisible(canMessage);
                messageButton.setManaged(canMessage);
            } else {
                messageButton.setVisible(false);
                messageButton.setManaged(false);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadBlockStatus() throws SQLException {
        blockedByMe = false;
        blockedMe = false;

        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT blockerID FROM blocks WHERE (blockerID = ? AND blockedID = ?) OR (blockerID = ? AND blockedID = ?)");
        preparedStatement.setInt(1, User.currentUser.getID());
        preparedStatement.setInt(2, profileUserID);
        preparedStatement.setInt(3, profileUserID);
        preparedStatement.setInt(4, User.currentUser.getID());
        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            if(resultSet.getInt("blockerID") == User.currentUser.getID()) {
                blockedByMe = true;
            } else {
                blockedMe = true;
            }
        }
    }

    private String getFriendStatus() throws SQLException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT userID, status FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID = ?) LIMIT 1");
        preparedStatement.setInt(1, User.currentUser.getID());
        preparedStatement.setInt(2, profileUserID);
        preparedStatement.setInt(3, profileUserID);
        preparedStatement.setInt(4, User.currentUser.getID());
        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()) {
            requestFromProfileUser = resultSet.getInt("userID") == profileUserID;
            return resultSet.getString("status");
        }

        requestFromProfileUser = false;
        return "none";
    }

    private void setFriendButtonText() {
        if("accepted".equals(friendStatus)) {
            friendActionButton.setText("Remove friend");
            return;
        }

        if("pending".equals(friendStatus)) {
            if(requestFromProfileUser) {
                friendActionButton.setText("Approve request");
                return;
            }
            friendActionButton.setText("Cancel request");
            return;
        }

        friendActionButton.setText("Add friend");
    }

    private void loadFriendsCounter() {
        Thread friendsThread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(*) FROM friends WHERE status = 'accepted' AND (userID = ? OR friendID = ?)");
                preparedStatement.setInt(1, profileUserID);
                preparedStatement.setInt(2, profileUserID);
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    int count = resultSet.getInt(1);
                    Platform.runLater(() -> totalFriendsCounter.setText("" + count));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> totalFriendsCounter.setText("-"));
            }
        });

        friendsThread.start();
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoutButtonClicked = false;
        topUsernameLabel.setText(getDisplayName());
        totalPostsCounter.setText("Loading...");
        totalFriendsCounter.setText("Loading...");
        myPostsBox.getChildren().clear();
        Utils.showFriendRequestsMark(navFriendsBtn);

        loadProfileInfo();
        loadMyPosts();
        loadFriendsCounter();

    }
}
