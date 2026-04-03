package com.daugia.models;

public class ArtCollectible extends Item {
    private String author;
    private int creationYear; 
    private String material; 

    public ArtCollectible(int id, String name, String description, double startingPrice, 
                          String author, int creationYear, String material) {
        super(id, name, description, startingPrice, "Sưu tầm - nghệ thuật");
        this.author = author;
        this.creationYear = creationYear;
        this.material = material;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Nghệ thuật [Tác giả: %s, Năm ST: %d, Chất liệu: %s]", 
                             author, creationYear, material);
    }
}