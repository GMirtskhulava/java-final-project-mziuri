package org.example.finalproject;

import java.io.Serial;
import java.io.Serializable;

public class MySQLConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 3306L;

    private String port;
    private String username;
    private String password;

    public MySQLConfig(String username, String password) {
        this("3306", username, password);
    }

    public MySQLConfig(String port, String username, String password) {
        setPort(port);
        setUsername(username);
        setPassword(password);
    }

    public String getPort() {
        return port == null || port.isEmpty() ? "3306" : port;
    }
    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
