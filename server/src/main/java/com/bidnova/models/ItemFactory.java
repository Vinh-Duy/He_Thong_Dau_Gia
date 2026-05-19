package com.bidnova.models;

public class ItemFactory {
    
    public static Item createItem(String category, int id, String name, String description, double startingPrice, Object... extraParams) {
        switch (category) {
            case "Bất động sản":
                return new RealEstate(id, name, description, startingPrice, 
                    (double) extraParams[0], (String) extraParams[1], (String) extraParams[2]);
                    
            case "Phương tiện - xe cộ":
                return new Vehicle(id, name, description, startingPrice, 
                    (String) extraParams[0], (int) extraParams[1], (int) extraParams[2]);
                    
            case "Sưu tầm - nghệ thuật":
                return new ArtCollectible(id, name, description, startingPrice, 
                    (String) extraParams[0], (int) extraParams[1], (String) extraParams[2]);
                    
            case "Tài sản nhà nước":
                return new StateProperty(id, name, description, startingPrice, 
                    (String) extraParams[0], (String) extraParams[1]);
                    
            default:
                throw new IllegalArgumentException("Danh mục không hợp lệ: " + category);
        }
    }
}