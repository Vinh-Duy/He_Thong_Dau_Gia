package com.daugia.patterns.factory;

import com.daugia.models.ArtCollectible;
import com.daugia.models.Item;

/**
 * Concrete Factory - Art Collectible Creator
 * 
 * @author Design Patterns Team
 */
public class ArtCollectibleCreator implements ItemCreator {
    
    @Override
    public Item createItem(int id, String name, String description, double startingPrice, Object... extraParams) {
        if (extraParams.length < 3) {
            throw new IllegalArgumentException("ArtCollectible cần ít nhất 3 tham số: artist, yearCreated, artType");
        }
        return new ArtCollectible(id, name, description, startingPrice,
            (String) extraParams[0], (int) extraParams[1], (String) extraParams[2]);
    }
    
    @Override
    public boolean supportsCategory(String category) {
        return "Sưu tầm - nghệ thuật".equals(category);
    }
    
    @Override
    public String getItemTypeName() {
        return "Art Collectible (Sưu tầm - nghệ thuật)";
    }
}
