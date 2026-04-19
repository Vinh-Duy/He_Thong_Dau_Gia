package com.bidnova.utils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.List;

import com.bidnova.models.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

// đọc products từ file produxts.json
public class ProductLoader {
    public static List<Product> loadProducts() {
        try {
            // khởi tạo Gson và đăng ký Type Adapter: cái này để chuyển đổi file JSON sang
            // một đối tượng Java để dễ xử lý
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            // đọc file từ thư mục resources/data/products.json
            Reader reader = new InputStreamReader(
                    ProductLoader.class.getResourceAsStream("/data/products.json"));
            // TypeToken<kiểu trả về> giúp Gson hiểu rằng nó cần tạo ra một List các đối
            // tượng Product chứ không phải chỉ một đối tượng đơn lẻ.
            return gson.fromJson(reader, new TypeToken<List<Product>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
