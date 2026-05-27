package com.daugia.patterns.factory;

import com.daugia.models.Item;
import com.daugia.models.Vehicle;

/**
 * Concrete Factory - Vehicle Creator
 * 
 * @author Design Patterns Team
 */
public class VehicleCreator implements ItemCreator {
    
    @Override
    public Item createItem(int id, String name, String description, double startingPrice, Object... extraParams) {
        if (extraParams.length < 3) {
            throw new IllegalArgumentException("Vehicle cần ít nhất 3 tham số: brand, year, mileage");
        }
        return new Vehicle(id, name, description, startingPrice,
            (String) extraParams[0], (int) extraParams[1], (int) extraParams[2]);
    }
    
    @Override
    public boolean supportsCategory(String category) {
        return "Phương tiện - xe cộ".equals(category);
    }
    
    @Override
    public String getItemTypeName() {
        return "Vehicle (Phương tiện - xe cộ)";
    }
}
