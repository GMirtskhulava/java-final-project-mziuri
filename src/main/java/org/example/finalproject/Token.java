package org.example.finalproject;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

public class Token implements Serializable {
    @Serial
    private static final long serialVersionUID = 62025072L;

    private String accessToken = null;
    private LocalDateTime expiration;
    private LocalDateTime created;
    public static Token currentToken = null;

    protected Token(int userID) {
        String tmpToken = generateToken(userID);
        setAccessToken(tmpToken);
        LocalDateTime now = LocalDateTime.now();
        setCreated(now);
        LocalDateTime expire = now.plusHours(2);
        setExpiration(expire);

        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO access_tokens (userID, token, expirationDate, createdDate) VALUES (?, ?, ?, ?);");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, tmpToken);
            preparedStatement.setObject(3, expire);
            preparedStatement.setObject(4, now);
            int rows = preparedStatement.executeUpdate();
            if(rows > 0) {
                serializeToken(this);
            }
            else System.out.println("--");

        } catch (SQLException e) {
            System.out.println("[Token | MySQL]: Exception");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("[Token | Serialization]: Exception");
            System.out.println(e.getMessage());
        }
    }


    public String getAccessToken() {
        return accessToken;
    }

    private void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }
    private void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public LocalDateTime getCreated() {
        return created;
    }
    private void setCreated(LocalDateTime created) {
        this.created = created;
    }

//

    private static String generateToken(int ID){
        StringBuilder accessToken = new StringBuilder("acSN");

        Random random = new Random();
        for (int i = 0; i < random.nextInt(3, 9); i++) {
            if(i % 3 == 0) {
                accessToken.append((int) ID/2);
            }
            int type = random.nextInt(0, 3);
            char ch = type == 0 ? (char) random.nextInt((int) 'A', (int) 'Z') :
                    type == 1 ? (char) random.nextInt((int) 'a', (int) 'z') : (char) random.nextInt((int) '0', (int) '1');

            accessToken.append(ch);
        }
//
        for (int i = 0; i < random.nextInt(12, 25); i++) {

            int type = random.nextInt(0, 4);
            char tmpCh;
            switch(type) {
                case 0: {
                    tmpCh = (char) random.nextInt((int) 'A', (int) 'Z');
                    break;
                }
                case 1: {
                    tmpCh = (char) random.nextInt((int) '0', (int) '1');
                    break;
                }
                case 2: {
                    tmpCh = (char) random.nextInt((int) 'a', (int) 'z');
                    break;
                }
                case 3: {
                    tmpCh = (char) ('#' + (ID%2));
                    break;
                }
                default: {
                    tmpCh = '!';
                }
            }

            accessToken.append(tmpCh);
        }

        System.out.println(accessToken.toString());
        return accessToken.toString();
    }
    protected static boolean validateToken(Token token) {
        if(token == null || token.getAccessToken() == null ||  token.getAccessToken().isEmpty()) return false;
        try {
            PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT id, expirationDate FROM access_tokens WHERE token=?");
            preparedStatement.setString(1, token.getAccessToken());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                LocalDateTime expiration = (LocalDateTime) resultSet.getObject("expirationDate");
                if(expiration.isBefore(LocalDateTime.now())) {
                    preparedStatement = MySQL.connection.prepareStatement("DELETE FROM access_tokens WHERE id=?;");
                    preparedStatement.setInt(1, resultSet.getInt("id"));
                    preparedStatement.execute();
                    serializeToken(null);
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("[MYSQL]: Exception while validating Token!");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("[Token-IO]: Exception");
            System.out.println(e.getMessage());
        }
        return false;
    }


    protected static void serializeToken(Token token) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("data\\session.ser");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(token);
        objectOutputStream.close();
    }
    protected static Token deserializeToken() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("data\\session.ser");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        return (Token) objectInputStream.readObject();
    }


    @Override
    public String toString() {
        return "Token{" +
                "accessToken='" + accessToken + '\'' +
                ", expiration=" + expiration +
                ", created=" + created +
                '}';
    }
}
