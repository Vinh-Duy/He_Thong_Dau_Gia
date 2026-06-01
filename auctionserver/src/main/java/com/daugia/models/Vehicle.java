package com.daugia.models;

public class Vehicle extends Item {
    private String brand;
    private int year;
    private int mileage; 

    public Vehicle(int id, String name, String description, double startingPrice, 
                   String brand, int year, int mileage) {
        super(id, name, description, startingPrice, "Phương tiện - xe cộ");
        this.brand = brand;
        this.year = year;
        this.mileage = mileage;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Phương tiện [Hãng: %s, Năm SX: %d, ODO: %d km]", 
                             brand, year, mileage);
    }
}