package org.example.finalproject;

import java.io.Serial;
import java.io.Serializable;
import java.sql.*;

public class MySQL {


    private static String port = "3306";
    private static String username = "root";
    private static String password = "password";
    public static Connection connection = null;

    public static void ConnectToDatabase() throws SQLException {
        String url = "jdbc:mysql://localhost:" + port + "/sys";
        connection = DriverManager.getConnection(url, username, password);
    }
    public static int[] InitDB() throws SQLException {
        Statement st = connection.createStatement();

        st.addBatch("CREATE DATABASE IF NOT EXISTS socnet_javafx;");
        st.addBatch("USE socnet_javafx;");
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
                "messagesFriendsOnly BOOLEAN NOT NULL DEFAULT FALSE," +
                "totalPongScores INT DEFAULT 0," +
                "totpEnabled BOOLEAN DEFAULT false," +
                "totpKey VARCHAR(33) DEFAULT ''" +
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

        st.addBatch("CREATE TABLE IF NOT EXISTS pinned_chats (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "userID INT NOT NULL," +
                "receiverID INT NOT NULL," +
                "CONSTRAINT fKey_pins_userID FOREIGN KEY(userID) " +
                "REFERENCES users(id)," +
                "CONSTRAINT fKey_pins_receiverID FOREIGN KEY(receiverID) " +
                "REFERENCES users(id)" +
                ");");


        int[] result = st.executeBatch();
//
//        try {
//            Statement st2 = connection.createStatement();
////            st2.execute("ALTER TABLE users ADD COLUMN totpEnabled BOOLEAN DEFAULT false AFTER messagesFriendsOnly;");
//            st2.execute("ALTER TABLE users ADD COLUMN totpKey VARCHAR(33) DEFAULT '' AFTER totpEnabled;");
//        } catch (SQLException e) {
//            System.out.print(e.getMessage());
//        }

        return result;
    }

    public static void initTestData() throws SQLException {
        Statement statement = MySQL.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users;");
        if(!resultSet.next()) {
            statement.executeUpdate("INSERT INTO users (firstname, lastname, password, contactInfo, gender, birthDate, bio, totalPongScores) VALUES " +
                    "('Giorgi', 'Test', MD5('123456'), 'giorgi@test.com', 'Male', '2006-01-01', 'testt', 1), " +
                    "('Nino', 'Player', MD5('123456'), 'nino@yahoo.com', 'Female', '2006-02-02', 'Pong pro player', 4), " +
                    "('Luka', 'Beridze', MD5('123456'), 'luka@gmail.com', 'Male', '2005-03-03', '', 2), " +
                    "('Mariam', 'Mariam', MD5('123456'), '555123321', 'Female', '2005-04-04', '', 0);");
        }

        resultSet = statement.executeQuery("SELECT * FROM posts;");
        if(!resultSet.next()) {
            statement.executeUpdate("INSERT INTO posts (authorID, content, createdDate) VALUES " +
                    "(1, 'Hello everyone! How are you?', '2026-06-16 21:08:13'), " +
                    "(4, 'შევიძენ მანქანას\nდამიკავშირდით: 555 123 321', '2026-06-21 14:33:39'), " +
                    "(3, 'Vedzeb developers romelic amiwyobs ecommerce aplikacias!', '2026-06-18 17:24:48');");
        }

    }



    public static String getPort() {
        return port;
    }
    public static void setPort(String port) {
        MySQL.port = port == null || port.isEmpty() ? "3306" : port;
    }

    public static void setUsername(String username) {
        MySQL.username = username;
    }
    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
    public static void setPassword(String password) {
        MySQL.password = password;
    }
}
