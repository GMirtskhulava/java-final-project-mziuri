package org.example.finalproject;

import java.io.Serial;
import java.io.Serializable;
import java.sql.*;

public class MySQL {


    private static final String url = "jdbc:mysql://localhost:3306/sys";
    private static String username = "root";
    private static String password = "password";
    public static Connection connection = null;

    static void ConnectToDatabase() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }
    static int[] InitDB() throws SQLException {
        Statement st = connection.createStatement();

        st.addBatch("CREATE DATABASE IF NOT EXISTS socnet;");
        st.addBatch("USE socnet;");
        st.addBatch("CREATE TABLE IF NOT EXISTS users (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "firstname VARCHAR(32) NOT NULL," +
                "lastname VARCHAR(32) NOT NULL," +
                "password VARCHAR(32) NOT NULL," +
                "contactInfo VARCHAR(32) NOT NULL UNIQUE," +
                "gender VARCHAR(6) NOT NULL," +
                "birthDate DATE," +
                "bio VARCHAR(128) DEFAULT ''," +
                "hideContactInfo BOOLEAN NOT NULL DEFAULT FALSE," +
                "friendListPrivate BOOLEAN NOT NULL DEFAULT FALSE," +
                "messagesFriendsOnly BOOLEAN NOT NULL DEFAULT FALSE" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS access_tokens (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "userID INT NOT NULL," +
                "token VARCHAR(64) NOT NULL," +
                "expirationDate DATETIME NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "CONSTRAINT fKey_userID FOREIGN KEY(userID) " +
                "REFERENCES users(id)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS posts (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "authorID INT NOT NULL," +
                "content VARCHAR(512) NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "CONSTRAINT fKey_authorID FOREIGN KEY(authorID) " +
                "REFERENCES users(id)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS post_likes (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "postID INT," +
                "shareID INT DEFAULT 0," +
                "userID INT NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "UNIQUE KEY unique_like (postID, shareID, userID)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS post_shares (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "postID INT NOT NULL," +
                "userID INT NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "UNIQUE KEY unique_share (postID, userID)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS blocks (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "blockerID INT NOT NULL," +
                "blockedID INT NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "UNIQUE KEY unique_block (blockerID, blockedID)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS friends (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "userID INT NOT NULL," +
                "friendID INT NOT NULL," +
                "status VARCHAR(16) NOT NULL DEFAULT 'pending'," +
                "createdDate DATETIME NOT NULL," +
                "UNIQUE KEY unique_friends (userID, friendID)," +
                "CONSTRAINT fKey_friends_userID FOREIGN KEY(userID) " +
                "REFERENCES users(id)," +
                "CONSTRAINT fKey_friends_friendID FOREIGN KEY(friendID) " +
                "REFERENCES users(id)" +
                ");");

        st.addBatch("CREATE TABLE IF NOT EXISTS messages (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "senderID INT NOT NULL," +
                "receiverID INT NOT NULL," +
                "content VARCHAR(512) NOT NULL," +
                "createdDate DATETIME NOT NULL," +
                "CONSTRAINT fKey_messages_senderID FOREIGN KEY(senderID) " +
                "REFERENCES users(id)," +
                "CONSTRAINT fKey_messages_receiverID FOREIGN KEY(receiverID) " +
                "REFERENCES users(id)" +
                ");");


        int[] result = st.executeBatch();
//
//        try {
//            Statement st2 = connection.createStatement();
//            st2.execute("ALTER TABLE friends ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'accepted';");
//        } catch (SQLException e) {
//            System.out.print(e.getMessage());
//        }



        return result;
    }

    public static String getUsername() {
        return username;
    }
    public static void setUsername(String username) {
        MySQL.username = username;
    }

    public static String getPassword() {
        return password;
    }
    public static void setPassword(String password) {
        MySQL.password = password;
    }
}
