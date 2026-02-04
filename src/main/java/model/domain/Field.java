package model.domain;

public class Field {
    private int id;
    private String name;
    private String city;
    private String address;
    private double pricePerHour;
    private Sport sport;
    private User manager;

    public Field() {
    }

    public Field(int id, String name, String city, Sport sport, User manager) {
        this(id, name, city, null, 0.0, sport, manager);
    }

    public Field(int id, String name, String city, String address, double pricePerHour, Sport sport, User manager) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.pricePerHour = pricePerHour;
        this.sport = sport;
        this.manager = manager;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public double getPricePerPerson() {
        if (sport == null || sport.getRequiredPlayers() <= 0) {
            return 0.0;
        }
        return pricePerHour / sport.getRequiredPlayers();
    }
}