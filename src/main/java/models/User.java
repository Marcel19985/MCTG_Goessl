package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID; //für UUID (primary key als UUID anstatt fortlaufend)

public class User {
    public UUID id;

    @JsonProperty("Username") //"Username" im JSON-Body wird username
    public String username;

    @JsonProperty("Password") //"Password" im JSON-Body wird password
    public String password;

    @JsonProperty("Token") //"Token" im JSON-Body wird token
    public String token;

    @JsonProperty("Name")
    public String name;

    @JsonProperty("Bio")
    public String bio;

    @JsonProperty("Image")
    public String image;

    private Stack stack;
    private int coins;

    // Default Konstruktor:
    public User() {
        this.stack = new Stack();
        this.coins = 20;
    }

    //Konstruktor überladen:
    public User(UUID id, String username, String password, String token) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.stack = new Stack();
        this.coins = 20;
    }

    public User(UUID id, String username, String password, String token, String name, String bio, String image) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.stack = new Stack();
        this.coins = 20;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Getter for stack
    public Stack getStack() {
        return stack;
    }

    // Methods to manage cards within the user's stack
    public void addCardToStack(Card card) {
        stack.addCard(card);
    }

    public void removeCardFromStack(Card card) {
        stack.removeCard(card);
    }

    public void viewStack() {
        System.out.println("User's Stack: " + stack);
    }

    public boolean buyPackage(Package pkg) {
        if (coins < 5) {
            System.out.println("Insufficient coins to buy a package.");
            return false;
        }
        coins -= 5; //Package kostet 5 coins
        for (Card card : pkg.getCards()) {
            stack.addCard(card);
        }
        System.out.println("Package acquired and added to stack. Remaining coins: " + coins);
        return true;
    }
}