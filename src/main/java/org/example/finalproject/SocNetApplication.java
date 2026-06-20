package org.example.finalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class SocNetApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {

        try {
            MySQLConfig config = (MySQLConfig) Utils.deserializeObject("mysql.ser");

            while(config == null) {
                Scanner input = new Scanner(System.in);
                System.out.print("Enter MySQL username: ");
                String username = input.nextLine();

                System.out.print("Enter MySQL password: ");
                String password = input.nextLine();

                if(username.isEmpty() || password.isEmpty()) {
                    System.out.println("Enter Username and password!!!");
                    return;
                }

                MySQL.setUsername(username);
                MySQL.setPassword(password);

                try {
                    MySQL.ConnectToDatabase();

                    config = new MySQLConfig(username, password);
                    Utils.serializeObject(config, "mysql.ser");

                    System.out.println("MySQL setup saved successfully.");
                } catch (SQLException e) {
                    System.out.println("Cannot connect to MySQL:\n\t" + e.getMessage());
                    System.out.println("Try again");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            MySQL.setUsername(config.getUsername());
            MySQL.setPassword(config.getPassword());

            MySQL.ConnectToDatabase();
            MySQL.InitDB();

        } catch (ClassNotFoundException | IOException e) {
            System.out.println(e.getMessage());
            Utils.serializeObject(null, "mysql.ser");
            System.out.println("!!!!! RESTART APPLICATION !!!!!");
            return;
        } catch (SQLException e) {
            System.out.println("[MySQL] DB error: " + e.getMessage());
        }


        new Mailer();
        //
        FXMLLoader fxmlLoader = new FXMLLoader(SocNetApplication.class.getResource("login-page.fxml"));
        stage.setTitle("SocNet | Login");

        try {
            Token tmpToken = Token.deserializeToken();
            System.out.println(tmpToken);
            if(Token.validateToken(tmpToken)) {
                Token.currentToken = tmpToken;
                PreparedStatement preparedStatement = MySQL.connection.prepareStatement("SELECT * FROM access_tokens AS at LEFT JOIN users AS u ON at.userID = u.id WHERE at.token = ? LIMIT 1;");
                preparedStatement.setString(1, tmpToken.getAccessToken());
                ResultSet resultSet = preparedStatement.executeQuery();

                if(resultSet.next()) {
                    int ID = resultSet.getInt("userID");
                    String firstName = resultSet.getString("firstname");
                    String lastName = resultSet.getString("lastname");
                    String contactInfo1 = resultSet.getString("contactInfo");
                    User.currentUser = new User(ID, firstName, lastName, contactInfo1);
                    fxmlLoader = new FXMLLoader(SocNetApplication.class.getResource("main-page.fxml"));
                    stage.setTitle("SocNet | Main");
                }
                else {
                    Token.currentToken = null;
                    fxmlLoader = new FXMLLoader(SocNetApplication.class.getResource("login-page.fxml"));
                    stage.setTitle("SocNet | Login");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[Start | Token reading]: Exception!");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("[Start | Token reading]: Exception while reading session file!");
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("[Start | SQL Check]: Exception while checking user & token in DB");
            System.out.println(e.getMessage());
        } finally {
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            stage.setScene(scene);
            stage.show();
        }
    }
}
