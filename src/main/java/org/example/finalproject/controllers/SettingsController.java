package org.example.finalproject.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.finalproject.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private boolean logoutButtonClicked = false;
    private boolean saveButtonClicked = false;
    private boolean deleteButtonClicked = false;

    @FXML
    private Label topUsernameLabel;

    @FXML
    private javafx.scene.control.Button navFriendsBtn;

    @FXML
    private TextField firstnameField;

    @FXML
    private TextField lastnameField;

    @FXML
    private RadioButton maleGenderRB;

    @FXML
    private RadioButton femaleGenderRB;

    @FXML
    private DatePicker birthDateField;

    @FXML
    private TextField contactInfoField;

    @FXML
    private TextField bioField;

    @FXML
    private CheckBox hideContactInfoCheckBox;

    @FXML
    private CheckBox friendListPrivateCheckBox;

    @FXML
    private CheckBox messagesFriendsOnlyCheckBox;

    @FXML
    private CheckBox google2FAcheckbox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label settingsMessageLabel;

    @FXML
    private VBox blockedUsersContainer;


    @FXML Button setup2FAButton;
    private String Google2FAKey;


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
    private void loadMySettings() {
        Thread settingsThread = new Thread(() -> {
            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT firstname, lastname, contactInfo, gender, birthDate, bio, hideContactInfo, friendListPrivate, messagesFriendsOnly, totpEnabled, totpKey FROM users WHERE id = ?");
                preparedStatement.setInt(1, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");
                    String contactInfo = resultSet.getString("contactInfo");
                    String gender = resultSet.getString("gender");
                    String birthDate = resultSet.getString("birthDate");
                    String bio = resultSet.getString("bio");
                    boolean hideContactInfo = resultSet.getBoolean("hideContactInfo");
                    boolean friendListPrivate = resultSet.getBoolean("friendListPrivate");
                    boolean messagesFriendsOnly = resultSet.getBoolean("messagesFriendsOnly");
                    boolean enabled2FA = resultSet.getBoolean("totpEnabled");
                    String secretKey = resultSet.getString("totpKey");

                    Google2FAKey = secretKey;

                    Platform.runLater(() -> {
                        firstnameField.setText(firstname);
                        lastnameField.setText(lastname);
                        contactInfoField.setText(contactInfo);
                        bioField.setText(bio == null ? "" : bio);
                        hideContactInfoCheckBox.setSelected(hideContactInfo);
                        friendListPrivateCheckBox.setSelected(friendListPrivate);
                        messagesFriendsOnlyCheckBox.setSelected(messagesFriendsOnly);
                        google2FAcheckbox.setSelected(enabled2FA);
                        if("Female".equalsIgnoreCase(gender)) {
                            femaleGenderRB.setSelected(true);
                        } else {
                            maleGenderRB.setSelected(true);
                        }

                        if(Google2FAKey.isEmpty()) google2FAcheckbox.setVisible(false);
                        else setup2FAButton.setText("Delete 2FA");

                        if(birthDate != null && !birthDate.isEmpty()) {
                            birthDateField.setValue(LocalDate.parse(birthDate));
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> settingsMessageLabel.setText("Something went wrong while loadingsettings"));
            }
        });

        settingsThread.start();
    }

    @FXML
    private void handleSaveButton() {
        if(saveButtonClicked) return;
        saveButtonClicked = true;
        settingsMessageLabel.setText("");

        String firstname = firstnameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String gender = "";
        if(maleGenderRB.isSelected()) {
            gender = "Male";
        }
        if(femaleGenderRB.isSelected()) {
            gender = "Female";
        }
        String contactInfo = contactInfoField.getText().trim();
        String bio = bioField.getText().trim();
        boolean hideContactInfo = hideContactInfoCheckBox.isSelected();
        boolean friendListPrivate = friendListPrivateCheckBox.isSelected();
        boolean messagesFriendsOnly = messagesFriendsOnlyCheckBox.isSelected();
        boolean enabled2FA = google2FAcheckbox.isSelected();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate birthDate = birthDateField.getValue();

        if(firstname.isEmpty() || lastname.isEmpty() || gender.isEmpty() || contactInfo.isEmpty() || birthDate == null) {
            settingsMessageLabel.setText("Please fill all profile fields.");
            saveButtonClicked = false;
            return;
        }

        if(!Google2FAKey.isEmpty() && !contactInfo.equals(User.currentUser.getContactInfo())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR While Saving!");
            alert.setContentText("Can't change contact information while 2FA authenticator is linked to account!\nTo unlink Google 2FA send request to support - socnetjavafx@gmail.com");
            contactInfoField.setText(User.currentUser.getContactInfo());

            if(alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            return;
        }

        if(!password.isEmpty() || !confirmPassword.isEmpty()) {
            if(password.length() < 6 || password.length() > 20) {
                settingsMessageLabel.setText("Password length must be 6-20 characters.");
                saveButtonClicked = false;
                return;
            }

            if(!password.equals(confirmPassword)) {
                settingsMessageLabel.setText("Passwords are not same.");
                saveButtonClicked = false;
                return;
            }
        }

        try {
            PreparedStatement preparedStatement;

            if(password.isEmpty()) {
                preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET firstname = ?, lastname = ?, gender = ?, birthDate = ?, contactInfo = ?, bio = ?, hideContactInfo = ?, friendListPrivate = ?, messagesFriendsOnly = ?, totpEnabled = ? WHERE id = ?");
                preparedStatement.setString(1, firstname);
                preparedStatement.setString(2, lastname);
                preparedStatement.setString(3, gender);
                preparedStatement.setObject(4, birthDate);
                preparedStatement.setString(5, contactInfo);
                preparedStatement.setString(6, bio);
                preparedStatement.setBoolean(7, hideContactInfo);
                preparedStatement.setBoolean(8, friendListPrivate);
                preparedStatement.setBoolean(9, messagesFriendsOnly);
                preparedStatement.setBoolean(10, enabled2FA);
                preparedStatement.setInt(11, User.currentUser.getID());
            } else {
                preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET firstname = ?, lastname = ?, gender = ?, birthDate = ?, contactInfo = ?, bio = ?, hideContactInfo = ?, friendListPrivate = ?, messagesFriendsOnly = ?, totpEnabled = ?, password = MD5(?) WHERE id = ?");
                preparedStatement.setString(1, firstname);
                preparedStatement.setString(2, lastname);
                preparedStatement.setString(3, gender);
                preparedStatement.setObject(4, birthDate);
                preparedStatement.setString(5, contactInfo);
                preparedStatement.setString(6, bio);
                preparedStatement.setBoolean(7, hideContactInfo);
                preparedStatement.setBoolean(8, friendListPrivate);
                preparedStatement.setBoolean(9, messagesFriendsOnly);
                preparedStatement.setBoolean(10, enabled2FA);
                preparedStatement.setString(11, password);
                preparedStatement.setInt(12, User.currentUser.getID());
            }

            int rows = preparedStatement.executeUpdate();

            if(rows > 0) {
                User.currentUser.setFirstname(firstname);
                User.currentUser.setLastname(lastname);
                User.currentUser.setGender(gender);
                User.currentUser.setBirthDate(birthDate.toString());
                User.currentUser.setContactInfo(contactInfo);
                User.currentUser.setBio(bio);

                topUsernameLabel.setText(getDisplayName());
                passwordField.clear();
                confirmPasswordField.clear();
                settingsMessageLabel.setText("Settings saved successfully");
            } else {
                settingsMessageLabel.setText("Settings were not saved!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            settingsMessageLabel.setText("Something went wrong. Please try again later...");
        } finally {
            saveButtonClicked = false;
        }
    }

    @FXML
    private void handleDeleteAccountButton(ActionEvent event) {
        if(deleteButtonClicked) return;

//        Alert alert = new Alert(Alert.AlertType.ERROR);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete account");
        alert.setHeaderText("Delete account?");
        alert.setContentText("This will delete your account and posts");

        if(alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        deleteButtonClicked = true;

        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM posts WHERE authorID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM friends WHERE userID = ? OR friendID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM messages WHERE senderID = ? OR receiverID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM blocks WHERE blockerID= ? OR blockedID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, User.currentUser.getID());
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM access_tokens WHERE userID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.executeUpdate();

            preparedStatement = MySQL.connection.prepareStatement("DELETE FROM users WHERE id = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.executeUpdate();

            Token.serializeToken(null);
            User.currentUser = null;
            Token.currentToken = null;
            Utils.changeScene(event, "login-page");

        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
            settingsMessageLabel.setText("Eror rwhile deleting account");
        } finally {
            deleteButtonClicked = false;
        }
    }

    private void loadBlockedUsers() {
        Thread thread = new Thread(() -> {
            ArrayList<String[]> blockedUsers = new ArrayList<>();

            try {
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT u.id, u.firstname, u.lastname FROM blocks b JOIN users u ON u.id = b.blockedID WHERE b.blockerID = ? ORDER BY u.firstname, u.lastname");
                preparedStatement.setInt(1, User.currentUser.getID());
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()) {
                    blockedUsers.add(new String[]{
                            "" + resultSet.getInt("id"),
                            resultSet.getString("firstname"),
                            resultSet.getString("lastname")
                    });
                }

                Platform.runLater(() -> {
                    blockedUsersContainer.getChildren().clear();

                    if(blockedUsers.size() == 0) {
                        Label label = new Label("You have no blocked users.");
                        label.getStyleClass().add("label-light");
                        blockedUsersContainer.getChildren().add(label);
                        return;
                    }

                    for(String[] user : blockedUsers) {
                        blockedUsersContainer.getChildren().add(makeBlockedUserRow(Integer.parseInt(user[0]), user[1], user[2]));
                    }
                });
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                Platform.runLater(() -> {
                    blockedUsersContainer.getChildren().clear();
                    Label label = new Label("Blocked users could not be loaded.");
                    label.getStyleClass().add("label-light");
                    blockedUsersContainer.getChildren().add(label);
                });
            }
        });

        thread.start();
    }

    private HBox makeBlockedUserRow(int userID, String firstname, String lastname) {
        HBox row = new HBox();
        row.setSpacing(15);
        row.getStyleClass().add("friend-row");
        row.setPadding(new Insets(15, 15, 15, 15));

        Circle circle = new Circle();
        circle.setRadius(20);
        circle.setStyle("-fx-fill: #2ec4b6;");

        VBox textBox = new VBox();
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label nameLabel = new Label(getFullName(firstname, lastname));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1b4d3e;");

        Label infoLabel = new Label("Blocked");
        infoLabel.getStyleClass().add("label-light");

        textBox.getChildren().add(nameLabel);
        textBox.getChildren().add(infoLabel);

        Button unblockButton = new Button("Unblock");
        unblockButton.getStyleClass().add("btn-secondary");
        unblockButton.setOnAction(event -> unblockUser(userID));

        row.getChildren().add(circle);
        row.getChildren().add(textBox);
        row.getChildren().add(unblockButton);

        return row;
    }

    private void unblockUser(int userID) {
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM blocks WHERE blockerID = ? AND blockedID = ?");
            preparedStatement.setInt(1, User.currentUser.getID());
            preparedStatement.setInt(2, userID);
            preparedStatement.executeUpdate();
            loadBlockedUsers();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            settingsMessageLabel.setText("User was not unblocked.");
        }
    }

    @FXML public void handle2FAButton(ActionEvent event) {
        if(User.currentUser == null) return;
        if(!Mailer.isEmail(User.currentUser.getContactInfo())) {
            settingsMessageLabel.setText("To set up Google Auth 2FA, you need to link an email to your profile");
            return;
        }

        try {
            if(!Google2FAKey.isEmpty()) {
                System.out.println("Shemodis?");
                Utils.gAuthCheckState = 1;
                User.currentUser.setTotpKey(Google2FAKey);
                Utils.changeScene(event, "gauth-check");
            }
            else {
                Utils.changeScene(event, "gauth-setup");
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
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

    private String getFullName(String firstname, String lastname) {
        String firstName = firstname == null ? "" : firstname.trim();
        String lastName = lastname == null ? "" : lastname.trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }

    private String getDisplayName() {
        User user = User.currentUser;
        if(user == null)
            return "undefined";

        return user.getFullName();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logoutButtonClicked = false;
        saveButtonClicked = false;
        deleteButtonClicked = false;
        topUsernameLabel.setText(getDisplayName());
        Utils.showFriendRequestsMark(navFriendsBtn);

        loadMySettings();
        loadBlockedUsers();
    }
}
