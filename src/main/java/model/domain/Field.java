package model.domain;

public class Field {
    private int id;
    private String name;
    private String city;
    private Sport sport;
    private int managerId;

    public Field() {
    }

    public Field(int id, String name, String city, Sport sport, int managerId) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.sport = sport;
        this.managerId = managerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }
}
