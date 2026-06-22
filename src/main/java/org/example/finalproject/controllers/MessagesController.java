package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.finalproject.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MessagesController implements Initializable {
    private boolean logoutButtonClicked = false;
    private int selectedUserID = 0;
    private String selectedUserName = "";

    @FXML
    private TextField searchField;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private Button navFriendsBtn;

    @FXML
    private VBox userListContainer;

    @FXML
    private Label conversationNameLabel;

    @FXML
    private Label conversationStatusLabel;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

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
        Utils.messageUserID = 0;
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
    private void handleSendMessage() {
        if(selectedUserID == 0 || User.currentUser == null) return;

        String text = messageField.getText();
        if(text == null || text.isEmpty()) return;
        if(text.length() > 512) {
            showSystemMessage("Very long message!");
            sendButton.setDisable(false);
            return;
        }
        //


        int receiverID = selectedUserID;
        String receiverName = selectedUserName;
        //
        messageField.clear();
        sendButton.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                if(!canMessage(receiverID)) {
                    Platform.runLater(() -> {
                        showSystemMessage("You cannot message this user.");
                        sendButton.setDisable(false);
                    });
                    return;
                }

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO messages (senderID, receiverID, content, createdDate) VALUES (?, ?, ?, ?)");
                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, receiverID);
                preparedStatement.setString(3, text);
                preparedStatement.setObject(4, LocalDateTime.now());
                preparedStatement.executeUpdate();

                Platform.runLater(() -> {
                    if(selectedUserID == receiverID) {
                        loadMessages(receiverID, receiverName);
                    }
                    loadUserList();


                    sendButton.setDisable(false);
                    messageField.requestFocus();
                });
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    showSystemMessage("Message was not sent!!");
                    sendButton.setDisable(false);
                });
            }
        });

        thread.start();
    }

    private void loadUserList() {
        if(User.currentUser == null) return;

        Thread thread = new Thread(() -> {
            ArrayList<ChatUser> users = new ArrayList<>();

            try {
                int currentID = User.currentUser.getID();

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM friends as f JOIN users as u ON u.id = f.friendID WHERE f.userID = ? AND f.status = 'accepted' ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, currentID);
//                preparedStatement.setInt(1, User.currentUser.getID();
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    addChatUser(users, resultSet);
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM friends f JOIN users u ON u.id = f.userID WHERE f.friendID = ? AND f.status = 'accepted' ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, currentID);
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    addChatUser(users,resultSet);
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT senderID, receiverID FROM messages WHERE senderID = ? OR receiverID = ? ORDER BY createdDate DESC");
                preparedStatement.setInt(1, currentID);
                preparedStatement.setInt(2, currentID);
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    int senderID = resultSet.getInt("senderID");
                    int receiverID = resultSet.getInt("receiverID");
                    int secondUsrID = senderID == currentID ? receiverID : senderID;

                    if(!chatUserExists(users, secondUsrID)) {
                        addChatUserByID(users, secondUsrID);
                    }
                }

                Platform.runLater(() -> {
                    userListContainer.getChildren().clear();
                    int requestedUserID = Utils.messageUserID;

                    if(users.size() == 0) {
                        if(requestedUserID != 0) {
                            openUserWithload(requestedUserID);
                            Utils.messageUserID = 0;
                            return;
                        } else {
                            Label label = new Label("No chats yet");
                            label.getStyleClass().add("label-light");
                            userListContainer.getChildren().add(label);
                            return;
                        }
                    }

                    for(ChatUser user : users) {
                        userListContainer.getChildren().add(makeUserBox(user));
                    }

                    if(selectedUserID == 0 && requestedUserID != 0) {
                        for(ChatUser user : users) {
                            if(user.getId() == requestedUserID) {
                                openChat(user.getId(), user.getFullname());
                                Utils.messageUserID = 0;
                                return;
                            }
                        }

                        openUserWithload(requestedUserID);
                        Utils.messageUserID = 0;
                        return;
                    }

                    if(selectedUserID == 0) {
                        ChatUser firstUser = users.get(0);
                        openChat(firstUser.getId(), firstUser.getFullname());
                    }
                });
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    userListContainer.getChildren().clear();
                    Label label = new Label("Chats could not be loaded");
                    label.getStyleClass().add("label-light");
                    userListContainer.getChildren().add(label);
                });
            }
        });

        thread.start();
    }

    private void addChatUser(ArrayList<ChatUser> users, ResultSet resultSet) throws SQLException {
        int userID = resultSet.getInt("id");
        if(chatUserExists(users, userID)) return;

        String fullname = getFullName(resultSet.getString("firstname"), resultSet.getString("lastname"));
        String lastMessage = getLastMessage(userID);
        users.add(new ChatUser(userID, fullname, lastMessage));
    }

    private void addChatUserByID(ArrayList<ChatUser> users, int userID) throws SQLException {
        if(chatUserExists(users, userID)) return;

        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname FROM users WHERE id = ?");
        preparedStatement.setInt(1, userID);
        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()) {
            addChatUser(users, resultSet);
        }
    }

    private boolean chatUserExists(ArrayList<ChatUser> users, int userID) {
        for(ChatUser user : users) {
            if(user.getId() == userID) {
                return true;
            }
        }

        return false;
    }

    private String getLastMessage(int userID) throws SQLException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT content FROM messages WHERE (senderID = ? AND receiverID = ?) OR (senderID = ? AND receiverID = ?) ORDER BY createdDate DESC LIMIT 1");
        preparedStatement.setInt(1, User.currentUser.getID());
        preparedStatement.setInt(2, userID);
        preparedStatement.setInt(3, userID);
        preparedStatement.setInt(4, User.currentUser.getID());
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            String msg = resultSet.getString("content");
            return msg;
        }

        return null;
    }


    private HBox makeUserBox(ChatUser user) {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add(user.getId() == selectedUserID ? "chat-item-active" : "chat-item");

        Circle profilePhoto = new Circle();
        profilePhoto.setFill(Color.web(user.getId() == selectedUserID ? "#1b4d3e" : "#2ec4b6"));
        profilePhoto.setStroke(Color.TRANSPARENT);
        profilePhoto.setRadius(16);

        VBox textBox = new VBox();
        HBox.setHgrow(textBox, Priority.ALWAYS);
//        Priority.SOMETIMES

        Label nameLabel = new Label(user.getFullname());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #000000;");

        Label messageLabel = new Label( (user.getLastMessage() == null || user.getLastMessage().trim().isEmpty()) ? "No messages yet" : user.getLastMessage());
        messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        messageLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(messageLabel); //z//

        box.getChildren().add(profilePhoto);
        box.getChildren().add(textBox);
        box.setUserData(user.getId());
        box.setOnMouseClicked(event -> openChat(user.getId(), user.getFullname()));

        return box;
    }

    private void openChat(int userID, String fullname) {
        selectedUserID = userID;
        selectedUserName = fullname;
        conversationNameLabel.setText(fullname);
        conversationStatusLabel.setText("Conversation");
        messageField.setDisable(false);
        sendButton.setDisable(false);

        loadMessages(userID, fullname);
        loadUserListActiveOnly();
    }

    private void loadUserListActiveOnly() {
        for(javafx.scene.Node node : userListContainer.getChildren()) {
            node.getStyleClass().remove("chat-item");
            node.getStyleClass().remove("chat-item-active");
            if(node.getUserData() instanceof Integer && ((Integer) node.getUserData()) == selectedUserID) {
                node.getStyleClass().add("chat-item-active");
            } else {
                node.getStyleClass().add("chat-item");
            }
        }
    }

    private void openUserWithload(int userID) {
        Thread thread = new Thread(() -> {
            try {
                if(!canMessage(userID)) {
                    Platform.runLater(() -> {
                        Label label = new Label("You cannot message this user.");
                        label.getStyleClass().add("label-light");
                        userListContainer.getChildren().add(label);
                    });
                    return;
                }

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname FROM users WHERE id = ? AND id != ?");
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    ChatUser user = new ChatUser(resultSet.getInt("id"), getFullName(resultSet.getString("firstname"), resultSet.getString("lastname")), null);
                    Platform.runLater(() -> {
                        userListContainer.getChildren().add(makeUserBox(user));
                        openChat(user.getId(), user.getFullname());
                    });
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });

        thread.start();
    }

    private void loadMessages(int userID, String fullname) {


        Thread thread = new Thread(() -> {
            ArrayList<ChatMessage> messages = new ArrayList<>();
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT senderID, content, createdDate FROM messages WHERE (senderID=? AND receiverID=?) OR (senderID = ? AND receiverID =?) ORDER BY createdDate ASC;");

                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, userID);
                preparedStatement.setInt(3, userID);
                preparedStatement.setInt(4, User.currentUser.getID());

                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()) {
                    messages.add(new ChatMessage(resultSet.getInt("senderID"), resultSet.getString("content"), resultSet.getString("createdDate")));
                }

                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    conversationNameLabel.setText(fullname);

                    if(messages.size() == 0) {
                        showSystemMessage("Messages not found");
                        return;
                    }

                    for(ChatMessage message : messages) {
                        messagesContainer.getChildren().add(makeMessageBox(message));
                    }
//                    messagesScrollPane.setOnScroll();
                    messagesScrollPane.setVvalue(1.0);
                });

            } catch (SQLException e) {

                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    showSystemMessage("Messages not loaded. something went wrong");
                });
            }
        });

        thread.start();
    }

    private HBox makeMessageBox(ChatMessage message) {
        boolean myMessage = message.getSenderID() == User.currentUser.getID();

        HBox row = new HBox();
        row.setAlignment(myMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label label = new Label(message.getContent());
        label.setWrapText(true);
        label.setMaxWidth(430);
        label.getStyleClass().add(myMessage ? "message-out": "message-in");

        row.getChildren().add(label);
        return row;
    }

    private void showSystemMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-light");
        label.setPadding(new Insets(8, 0, 8, 0));
        messagesContainer.getChildren().add(label);
    }

    private boolean canMessage(int userID) throws SQLException {
        PreparedStatement blockStatement = MySQL.connection.prepareStatement("SELECT id FROM blocks WHERE (blockerID = ? AND blockedID = ?) OR (blockerID = ? AND blockerID = ?) LIMIT 1");
        blockStatement.setInt(1, User.currentUser.getID());
        blockStatement.setInt(2, userID);
        blockStatement.setInt(3, userID);
        blockStatement.setInt(4, User.currentUser.getID());
        ResultSet blockResult = blockStatement.executeQuery();
        if(blockResult.next()) {
            return false;
        }


        PreparedStatement userStatement = MySQL.connection.prepareStatement("SELECT messagesFriendsOnly FROM users WHERE id=?");
        userStatement.setInt(1, userID);
        ResultSet userResult = userStatement.executeQuery();
        if(userResult.next() && !userResult.getBoolean("messagesFriendsOnly")) return true;

        PreparedStatement friendStatement = MySQL.connection.prepareStatement("SELECT id FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID =?) AND status = 'accepted' LIMIT 1");
        friendStatement.setInt(1, User.currentUser.getID());
        friendStatement.setInt(2, userID);
        friendStatement.setInt(3, userID);
        friendStatement.setInt(4, User.currentUser.getID());
        ResultSet friendResult = friendStatement.executeQuery();
        if(friendResult.next()) {
            return true;
        }
        return false;
    }

    private String getFullName(String firstname, String lastname) {
        String firstName = firstname == null ? "" : firstname.trim();
        String lastName = lastname == null ? "" : lastname.trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }



    private String getDisplayName() {
        User user = User.currentUser;
        if(user == null) {
            return "undefined";
        }

        return user.getFullName();
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
        messageField.setDisable(true);
        sendButton.setDisable(true);

        Utils.showFriendRequestsMark(navFriendsBtn);
        loadUserList();
    }

}
