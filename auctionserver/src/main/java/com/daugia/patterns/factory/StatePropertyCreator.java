package com.daugia.patterns.factory;

import com.daugia.models.Item;
import com.daugia.models.StateProperty;

/**
 * Concrete Factory - State Property Creator
 * 
 * @author Design Patterns Team
 */
public class StatePropertyCreator implements ItemCreator {
    
    @Override
    public Item createItem(int id, String name, String description, double startingPrice, Object... extraParams) {
        if (extraParams.length < 2) {
            throw new IllegalArgumentException("StateProperty cần ít nhất 2 tham số: agency, auctionDate");
        }
        return new StateProperty(id, name, description, startingPrice,
            (String) extraParams[0], (String) extraParams[1]);
    }
    
    @Override
    public boolean supportsCategory(String category) {
        return "Tài sản nhà nước".equals(category);
    }
    
    @Override
    public String getItemTypeName() {
        return "State Property (Tài sản nhà nước)";
    }
}
