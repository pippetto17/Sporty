package model.domain;

public class Field {
    private int id;
    private String name;
    private String city;
    private String address;
    private double pricePerHour;
    private Sport sport;
    private int managerId;

    public Field() {
    }

    public Field(int id, String name, String city, Sport sport, int managerId) {
        this(id, name, city, null, 0.0, sport, managerId);
    }

    public Field(int id, String name, String city, String address, double pricePerHour, Sport sport, int managerId) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.pricePerHour = pricePerHour;
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

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public double getPricePerPerson() {
        if (sport == null || sport.getRequiredPlayers() <= 0) {
            return 0.0;
        }
        return pricePerHour / sport.getRequiredPlayers();
    }
}