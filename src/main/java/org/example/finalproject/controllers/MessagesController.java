package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private CheckBox pinCheckBox;

    private int lastMessageID = 0;
    private boolean pollingMessages = false;

    @FXML
    private void openMyProfile(MouseEvent event) throws IOException {
        stopGettingMessages();
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeSceneFromNode((javafx.scene.Node) event.getSource(), "profile-page");
    }

    @FXML
    private void openProfileByID(MouseEvent event, int userID) throws IOException {
        stopGettingMessages();
        Utils.profileUserID = userID;
        Utils.changeSceneFromNode((javafx.scene.Node) event.getSource(), "profile-page");
    }

    @FXML
    private void showFeed(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.changeScene(event, "main-page");
    }

    @FXML
    private void showProfile(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.profileUserID = User.currentUser.getID();
        Utils.changeScene(event, "profile-page");
    }

    @FXML
    private void showMessages(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.messageUserID = 0;
        Utils.changeScene(event, "messages-page");
    }

    @FXML
    private void showFriends(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.friendsUserID = User.currentUser.getID();
        Utils.changeScene(event, "friends-page");
    }

    @FXML
    private void showGames(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.changeScene(event, "games-page");
    }

    @FXML
    private void showSettings(ActionEvent event) throws IOException {
        stopGettingMessages();
        Utils.changeScene(event, "settings-page");
    }

    @FXML
    private void showSearch(ActionEvent event) throws IOException {
        stopGettingMessages();
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
                int canMessageStatus = canMessage(receiverID);
                if(canMessageStatus != 1) {
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

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM pinned_chats p JOIN users u ON u.id = p.receiverID WHERE p.userID = ? ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, currentID);
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    addChatUser(users, resultSet, true);
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM friends as f JOIN users as u ON u.id = f.friendID WHERE f.userID = ? AND f.status = 'accepted' ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, currentID);
//                preparedStatement.setInt(1, User.currentUser.getID();
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    addChatUser(users, resultSet, false);
                }

                preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM friends f JOIN users u ON u.id = f.userID WHERE f.friendID = ? AND f.status = 'accepted' ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, currentID);
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    addChatUser(users,resultSet, false);
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
                                openChat(user.getId(), user.getFullname(), user.isPinned());
                                Utils.messageUserID = 0;
                                return;
                            }
                        }

                        openUserWithload(requestedUserID);
                        Utils.messageUserID = 0;
                        return;
                    }

                    /*if(selectedUserID == 0) {
                        ChatUser firstUser = users.get(0);
                        openChat(firstUser.getId(), firstUser.getFullname());
                    }*/
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

    private void addChatUser(ArrayList<ChatUser> users, ResultSet resultSet, boolean pinned) throws SQLException {
        int userID = resultSet.getInt("id");
        if(chatUserExists(users, userID)) return;

        String fullname = getFullName(resultSet.getString("firstname"), resultSet.getString("lastname"));
        String lastMessage = getLastMessage(userID);
        int unreadCount = getUnreadMessages(userID);
        users.add(new ChatUser(userID, fullname, lastMessage, unreadCount, pinned));
    }

    private void addChatUserByID(ArrayList<ChatUser> users, int userID) throws SQLException {
        if(chatUserExists(users, userID)) return;

        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname FROM users WHERE id = ?");
        preparedStatement.setInt(1, userID);
        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()) {
            addChatUser(users, resultSet, false);
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
    private int getUnreadMessages(int userID) throws SQLException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT COUNT(seen) FROM messages WHERE senderID = ? AND receiverID = ? AND seen = false LIMIT 50;");
        preparedStatement.setInt(1, userID);
        preparedStatement.setInt(2, User.currentUser.getID());
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            int unreadMessages = resultSet.getInt(1);
//            System.out.println(unreadMessages);
            return unreadMessages;
        }

        return 0;
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

        int unreadMsgs = user.getUnreadMessages();
        Label nameLabel = new Label((user.isPinned() ? "📌 " : "") + user.getFullname());
        String tmpStyle = "-fx-font-weight: bold; -fx-font-size: 13px;" + (unreadMsgs > 0 ? "-fx-text-fill: #000000;" : "-fx-text-fill: #7a7a7a");
        nameLabel.setStyle(tmpStyle);

        Label messageLabel = new Label( (user.getLastMessage() == null || user.getLastMessage().isEmpty()) ? "No messages yet" : (unreadMsgs > 0 ? (unreadMsgs + " unread messages") : user.getLastMessage()));
        tmpStyle = "-fx-font-size: 11px; " + (unreadMsgs > 0 ? "-fx-text-fill: #252525; -fx-font-weight: bold;" : "-fx-text-fill: #666666;");
        messageLabel.setStyle(tmpStyle);
        messageLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(messageLabel); //z//

        box.getChildren().add(profilePhoto);
        box.getChildren().add(textBox);
        box.setUserData(user.getId());
        box.setOnMouseClicked(event -> openChat(user.getId(), user.getFullname(), user.isPinned()));

        return box;
    }

    private void openChat(int userID, String fullname, boolean pinned) {
        conversationStatusLabel.getStyleClass().clear();
        conversationStatusLabel.getStyleClass().add("status-online");
        selectedUserID = userID;
        selectedUserName = fullname;
        lastMessageID = 0;
        conversationNameLabel.setText(fullname);
        conversationNameLabel.setOnMouseClicked(event -> {
            try {
                openProfileByID(event, userID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        conversationStatusLabel.setText("Conversation");
        messageField.setPromptText("Type a message...");
//        if(conversationStatusLabel.getStyleClass().get(2).equalsIgnoreCase("status-blocked")) conversationStatusLabel.getStyleClass().remove(2);
        messageField.setDisable(false);
        sendButton.setDisable(false);

        pinCheckBox.setVisible(true);
        pinCheckBox.setManaged(true);
        pinCheckBox.setSelected(pinned);

        loadMessages(userID, fullname);
        loadUserListActiveOnly();

        try {
            int canMessageStatus = canMessage(userID);
            if(canMessageStatus != 1) {
                Platform.runLater(() -> {
                    if(canMessageStatus == 2) {
                        conversationStatusLabel.setText("Blocked");
                        conversationStatusLabel.getStyleClass().add("status-blocked");
                    }
                    else {
                        conversationStatusLabel.setText("Not available");
                        conversationStatusLabel.getStyleClass().add("status-notAvailable");
                    }
//                    conversationStatusLabel.setStyle("-fx-text-fill: #bd3131;");
                    messageField.setDisable(true);
                    sendButton.setDisable(true);
                    messageField.setPromptText("Sending a message is not available.");
                });
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
                int canMessageStatus = canMessage(userID);
                if(canMessageStatus != 1) {
                    Platform.runLater(() -> {
                        Label label = new Label("You cannot message this user.");
                        label.getStyleClass().add("label-light");
                        userListContainer.getChildren().add(label);
                        messageField.setDisable(true);
                    });
                    return;
                }

                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname FROM users WHERE id = ? AND id != ?");
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    ChatUser user = new ChatUser(resultSet.getInt("id"), getFullName(resultSet.getString("firstname"), resultSet.getString("lastname")), null, 0, false);
                    Platform.runLater(() -> {
                        userListContainer.getChildren().add(makeUserBox(user));
                        openChat(user.getId(), user.getFullname(), user.isPinned());
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
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, senderID, content, seen, createdDate FROM messages WHERE (senderID=? AND receiverID=?) OR (senderID = ? AND receiverID =?) ORDER BY createdDate ASC;");

                preparedStatement.setInt(1, User.currentUser.getID());
                preparedStatement.setInt(2, userID);
                preparedStatement.setInt(3, userID);
                preparedStatement.setInt(4, User.currentUser.getID());

                ResultSet resultSet = preparedStatement.executeQuery();
                int newestMessageID = 0;
                while(resultSet.next()) {
                    newestMessageID = Math.max(newestMessageID, resultSet.getInt("id"));
                    messages.add(new ChatMessage(resultSet.getInt("senderID"), resultSet.getString("content"), resultSet.getBoolean("seen"), resultSet.getString("createdDate")));
                }
                if(userID == selectedUserID) {
                    lastMessageID = newestMessageID;
                }

                preparedStatement = MySQL.connection.prepareStatement("UPDATE messages SET seen = true WHERE senderID = ? AND receiverID = ?;");
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, User.currentUser.getID());
                preparedStatement.executeUpdate();

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

                    //
                    loadUserList();
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

        if(myMessage) {
            VBox messageBox = new VBox();
            messageBox.setAlignment(Pos.CENTER_RIGHT);

            String status = message.getCreatedDate() + " • " + (message.isSeen() ? "Seen" : "Sent");
            Label statusLabel = new Label(status);
            statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #777777;");

            messageBox.getChildren().add(label);
            messageBox.getChildren().add(statusLabel);
            row.getChildren().add(messageBox);
        } else {
            row.getChildren().add(label);
        }
        return row;
    }

    private void showSystemMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-light");
        label.setPadding(new Insets(8, 0, 8, 0));
        messagesContainer.getChildren().add(label);
    }

    private int canMessage(int userID) throws SQLException {
        PreparedStatement blockStatement = MySQL.connection.prepareStatement("SELECT id FROM blocks WHERE (blockerID = ? AND blockedID = ?) OR (blockerID = ? AND blockedID = ?) LIMIT 1");
        blockStatement.setInt(1, User.currentUser.getID());
        blockStatement.setInt(2, userID);
        blockStatement.setInt(3, userID);
        blockStatement.setInt(4, User.currentUser.getID());
        ResultSet blockResult = blockStatement.executeQuery();
        if(blockResult.next()) {
            return 2;
        }


        PreparedStatement userStatement = MySQL.connection.prepareStatement("SELECT messagesFriendsOnly FROM users WHERE id=?");
        userStatement.setInt(1, userID);
        ResultSet userResult = userStatement.executeQuery();
        if(userResult.next() && !userResult.getBoolean("messagesFriendsOnly")) return 1;

        PreparedStatement friendStatement = MySQL.connection.prepareStatement("SELECT id FROM friends WHERE (userID = ? AND friendID = ?) OR (userID = ? AND friendID =?) AND status = 'accepted' LIMIT 1");
        friendStatement.setInt(1, User.currentUser.getID());
        friendStatement.setInt(2, userID);
        friendStatement.setInt(3, userID);
        friendStatement.setInt(4, User.currentUser.getID());
        ResultSet friendResult = friendStatement.executeQuery();
        if(friendResult.next()) {
            return 1;
        }
        return 0;
    }

    @FXML
    private void toggleChatPin(ActionEvent event) {
        boolean checked = pinCheckBox.isSelected();
        String query;
        if(checked) {
            query = "INSERT INTO pinned_chats (userID, receiverID) VALUES (?, ?);";
        } else {
            query = "DELETE FROM pinned_chats WHERE userID = ? AND receiverID = ?";
        }
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement(query);
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, selectedUserID);
            preparedStatement.executeUpdate();

            loadUserList();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void checkingNewMessages() {
        if(selectedUserID == 0) return;
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT MAX(id) FROM messages WHERE senderID = ? AND receiverID = ?");
            preparedStatement.setInt(1, selectedUserID);
            preparedStatement.setInt(2, User.currentUser.getID());
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) {
                int sqlMessageID = resultSet.getInt(1);
                if(lastMessageID < sqlMessageID) {
                    lastMessageID = sqlMessageID;
                    loadMessages(selectedUserID, selectedUserName);
                    loadUserList();
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void startGettingMessages() {
        pollingMessages = true;

        Thread thread = new Thread(() -> {
            while(pollingMessages) {
                try {
                    Thread.sleep(3500); // 3,5 wami
                    checkingNewMessages();
                } catch (InterruptedException e) {
                    pollingMessages = false;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void stopGettingMessages() {
        pollingMessages = false;
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
            stopGettingMessages();
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
        pinCheckBox.setVisible(false);
        pinCheckBox.setManaged(false);

        Utils.showFriendRequestsMark(navFriendsBtn);
        loadUserList();
        startGettingMessages();
    }

}
