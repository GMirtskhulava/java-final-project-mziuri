package org.example.finalproject;

import java.io.Serial;
import java.io.Serializable;

public class MySQLConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 3306L;

    private String username;
    private String password;

    public MySQLConfig(String username, String password) {
        setUsername(username);
        setPassword(password);
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
