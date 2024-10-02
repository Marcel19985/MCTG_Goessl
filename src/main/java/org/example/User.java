package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("Username") //"Username" im JSON-Body wird username
    public String username;

    @JsonProperty("Password") //"Password" im JSON-Body wird password
    public String password;

    @JsonProperty("Token") //"Token" im JSON-Body wird token
    public String token;

    // Default Konstruktor:
    public User() {}

    //Konstruktor überladen:
    public User(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}