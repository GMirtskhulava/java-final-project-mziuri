package org.example.finalproject;

import org.example.finalproject.exceptions.AuthUserNotFoundException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    private int ID;
    private String username;
    private String firstname;
    private String lastname;
    private String contactInfo;
    private String gender;
    private String birthDate;
    private String bio;
    private int totalPongScores;

    private boolean totpEnabled;
    private String totpKey;

    public static User currentUser = null;


    public User(int ID, String firstname, String lastname, String contactInfo, String bio, int totalPongScores) {
        setID(ID);
//        setUsername(username);
        setFirstname(firstname);
        setLastname(lastname);
        setContactInfo(contactInfo);
        setBio(bio);
        setTotalPongScores(totalPongScores);

    }
    public User(int ID, String firstname, String lastname, String contactInfo, String bio, int totalPongScores, boolean totpEnabled, String totpKey) {
        setID(ID);
//        setUsername(username);
        setFirstname(firstname);
        setLastname(lastname);
        setContactInfo(contactInfo);
        setBio(bio);
        setTotalPongScores(totalPongScores);
        setTotpEnabled(totpEnabled);
        setTotpKey(totpKey);

    }

    public User(int ID, String firstname, String lastname, String password) {
        setID(ID);
        setFirstname(firstname);
        setLastname(lastname);

    }
    public User(int ID, String firstname, String lastname, int totalPongScores) {
        setID(ID);
        setFirstname(firstname);
        setLastname(lastname);
        setTotalPongScores(totalPongScores);
    }


    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getContactInfo() {
        return contactInfo;
    }
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getTotalPongScores() {
        return totalPongScores;
    }
    public void setTotalPongScores(int totalPongScores) {
        this.totalPongScores = totalPongScores;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }
    public void setTotpEnabled(boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    public String getTotpKey() {
        return totpKey;
    }
    public void setTotpKey(String totpKey) {
        this.totpKey = totpKey;
    }

    public String getFullName() {
        String firstName = firstname == null ? "" : firstname.trim();
        String lastName = lastname == null ? "" : lastname.trim();

        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "undefined" : fullName;
    }

    //
    public static User checkUser(String contactInfo, String password) throws SQLException, ClassNotFoundException, IOException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, firstname, lastname, contactInfo, bio, totalPongScores, totpEnabled, totpKey FROM users WHERE contactInfo = ? AND password = MD5(?);");
        preparedStatement.setString(1, contactInfo);
        preparedStatement.setString(2, password);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            int ID = resultSet.getInt("id");
            String firstName = resultSet.getString("firstname");
            String lastName = resultSet.getString("lastname");
            String contactInfo1 = resultSet.getString("contactInfo");
            String bio = resultSet.getString("bio");
            int totalPongScores = resultSet.getInt("totalPongScores");
            boolean totpStatus = resultSet.getBoolean("totpEnabled");
            if(totpStatus) {
                String totpSecretKey = resultSet.getString("totpKey");
                return new User(ID, firstName, lastName, contactInfo1, bio, totalPongScores, totpStatus, totpSecretKey);
            }
            // System.out.println(totalPongScores);
            return new User(ID, firstName, lastName, contactInfo1, bio, totalPongScores);

        } else {
            throw new AuthUserNotFoundException("User with this credentials not found");
        }
    }

    public static void logoutActiveUser() throws SQLException, IOException {
        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("DELETE FROM access_tokens WHERE userID = ? AND token = ?");
        preparedStatement.setInt(1, currentUser.getID());
        preparedStatement.setString(2, Token.currentToken.getAccessToken());
        preparedStatement.execute();
        Token.serializeToken(null);
    }
}
