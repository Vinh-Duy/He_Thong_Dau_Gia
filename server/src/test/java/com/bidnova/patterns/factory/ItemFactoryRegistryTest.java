package com.bidnova.patterns.factory;

import com.bidnova.models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Factory Pattern - ItemFactoryRegistry
 * 
 * @author Testing Team
 */
public class ItemFactoryRegistryTest {
    
    private ItemFactoryRegistry factoryRegistry;
    
    @BeforeEach
    void setUp() {
        factoryRegistry = ItemFactoryRegistry.getInstance();
    }
    
    @Test
    void testSingletonInstance() {
        // Arrange
        ItemFactoryRegistry instance1 = ItemFactoryRegistry.getInstance();
        ItemFactoryRegistry instance2 = ItemFactoryRegistry.getInstance();
        
        // Assert
        assertSame(instance1, instance2, "FactoryRegistry should be Singleton");
    }
    
    @Test
    void testCreateRealEstateItem() {
        // Arrange & Act
        Item item = factoryRegistry.createItem("Bất động sản", 1, 
            "Căn hộ cao cấp", "Căn hộ view biển", 5000000000.0, 
            120.5, "Nha Trang", "Căn hộ");
        
        // Assert
        assertNotNull(item, "Item should be created");
        assertEquals("RealEstate", item.getClass().getSimpleName());
    }
    
    @Test
    void testCreateVehicleItem() {
        // Arrange & Act
        Item item = factoryRegistry.createItem("Phương tiện - xe cộ", 2,
            "Lamborghini", "Siêu xe", 15000000000.0,
            "Lamborghini", 2022, 1000);
        
        // Assert
        assertNotNull(item, "Item should be created");
        assertEquals("Vehicle", item.getClass().getSimpleName());
    }
    
    @Test
    void testUnsupportedCategory() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            factoryRegistry.createItem("Không tồn tại", 3, "Test", "Test", 1000.0);
        }, "Should throw exception for unsupported category");
    }
    
    @Test
    void testSupportedCategories() {
        // Act
        var categories = factoryRegistry.getSupportedCategories();
        
        // Assert
        assertTrue(categories.contains("Bất động sản"), "Should support Real Estate");
        assertTrue(categories.contains("Phương tiện - xe cộ"), "Should support Vehicle");
        assertTrue(categories.contains("Sưu tầm - nghệ thuật"), "Should support Art");
        assertTrue(categories.contains("Tài sản nhà nước"), "Should support State Property");
    }
    
    @Test
    void testIsCategorySupported() {
        // Assert
        assertTrue(factoryRegistry.isCategorySupported("Bất động sản"));
        assertFalse(factoryRegistry.isCategorySupported("Không tồn tại"));
    }
}
