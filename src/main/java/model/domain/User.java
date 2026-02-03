package model.domain;

public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private String surname;
    private Role role;

    public User() {
    }

    public User(int id, String username, String password, String name, String surname, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isPlayer() {
        return role == Role.PLAYER;
    }

    public boolean isOrganizer() {
        return role == Role.ORGANIZER;
    }

    public boolean isFieldManager() {
        return role == Role.FIELD_MANAGER;
    }
}