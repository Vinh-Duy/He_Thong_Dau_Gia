package com.daugia.models;

public class RealEstate extends Item {
    private double area;
    private String location;
    private String legalStatus;

    public RealEstate(int id, String name, String description, double startingPrice, 
                      double area, String location, String legalStatus) {
        super(id, name, description, startingPrice, "Bất động sản");
        this.area = area;
        this.location = location;
        this.legalStatus = legalStatus;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Bất động sản [Vị trí: %s, Diện tích: %.2f m2, Pháp lý: %s]", 
                             location, area, legalStatus);
    }
}