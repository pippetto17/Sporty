package model.bean;

import model.domain.Sport;

public class FieldBean {
    private int fieldId;
    private String name;
    private Sport sport;
    private String city;
    private String address;
    private double pricePerHour;
    private int managerId;

    public FieldBean() {
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
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

    public double getPricePerPerson() {
        if (sport == null || sport.getRequiredPlayers() <= 0) {
            return 0.0;
        }
        return pricePerHour / sport.getRequiredPlayers();
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }
}