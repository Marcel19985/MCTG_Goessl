package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import services.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID; //für UUID (primary key als UUID anstatt fortlaufend)
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {
    private UUID id;

    @JsonProperty("Username") //"Username" im JSON-Body wird username
    private String username;

    @JsonProperty("Password") //"Password" im JSON-Body wird password
    private String password;

    @JsonProperty("Token") //"Token" im JSON-Body wird token
    private String token;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Bio")
    private String bio;

    @JsonProperty("Image")
    private String image;

    private Stack stack;
    private int coins;
    private Deck deck;

    private int elo = 100;
    private int wins = 0;
    private int draws = 0;
    private int losses = 0;

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    // Default Konstruktor:
    public User() {
        this.stack = new Stack();
        this.coins = 20;
        this.deck = new Deck();
    }

    //Konstruktor überladen:
    public User(UUID id, String username, String password, String token) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.stack = new Stack();
        this.deck = new Deck();
        this.coins = 20;
    }

    public User(UUID id, String username, String password, String token, String name, String bio, String image, int coins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.coins = coins;
        this.stack = new Stack();
        this.deck = new Deck();
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
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

    public Stack getStack() {
        return stack;
    }

    public void addCardToStack(Card card) {
        stack.addCard(card);
    }

    public void removeCardFromStack(Card card) {
        stack.removeCard(card);
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public boolean buyPackage(Package pkg, UserService userService, Connection conn) throws SQLException {
        if (coins < 5) {
            throw new IllegalStateException("Not enough money.");
        }

        coins -= 5; // Reduzieren der Coins lokal

        userService.updateCoins(this, conn); //Reduzieren Coins Datenbank

        userService.assignCardsToUser(pkg, this, conn); //Weise die Karten dem Benutzer zu

        for (Card card : pkg.getCards()) { //Karten lokal hinzufügen
            stack.addCard(card);
        }

        return true;
    }

    public String toJson() throws JsonProcessingException {
        Map<String, String> userData = new HashMap<>();
        userData.put("Username", this.username);
        userData.put("Name", this.name);
        userData.put("Bio", this.bio);
        userData.put("Image", this.image);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(userData); //Map in JSON umwandeln
    }

}