package com.daugia.patterns.factory;

import com.daugia.models.Item;
import com.daugia.models.RealEstate;

/**
 * Concrete Factory - Real Estate Creator
 * 
 * @author Design Patterns Team
 */
public class RealEstateCreator implements ItemCreator {
    
    @Override
    public Item createItem(int id, String name, String description, double startingPrice, Object... extraParams) {
        if (extraParams.length < 3) {
            throw new IllegalArgumentException("RealEstate cần ít nhất 3 tham số: area, location, propertyType");
        }
        return new RealEstate(id, name, description, startingPrice,
            (double) extraParams[0], (String) extraParams[1], (String) extraParams[2]);
    }
    
    @Override
    public boolean supportsCategory(String category) {
        return "Bất động sản".equals(category);
    }
    
    @Override
    public String getItemTypeName() {
        return "Real Estate (Bất động sản)";
    }
}
