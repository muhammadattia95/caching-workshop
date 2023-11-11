package com.attia.cachingpoc;

import com.attia.cachingpoc.entity.Product;
import com.attia.cachingpoc.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableCaching
public class CachingPocApplication implements CommandLineRunner {

    private final ProductRepository productRepository;

    public CachingPocApplication(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CachingPocApplication.class, args);
    }

    @Override
    public void run(String... args) {
        insertSampleProducts();
    }

    private void insertSampleProducts() {
        // Create 10 sample products
        List<Product> sampleProducts = Arrays.asList(
                new Product(1L, "Laptop", "Powerful laptop with high performance", 1200.0, true),
                new Product(2L, "Smartphone", "Latest smartphone with advanced features", 800.0, true),
                new Product(3L, "Headphones", "Wireless over-ear headphones with noise cancellation", 150.0, true),
                new Product(4L, "Smartwatch", "Fitness and health tracking smartwatch", 200.0, true),
                new Product(5L, "Tablet", "Lightweight and portable tablet", 400.0, true),
                new Product(6L, "Gaming Console", "Next-gen gaming console for immersive gaming experience", 500.0, true),
                new Product(7L, "Camera", "High-resolution digital camera for professional photography", 1000.0, true),
                new Product(8L, "Wireless Router", "High-speed wireless router for seamless internet connectivity", 80.0, true),
                new Product(9L, "Bluetooth Speaker", "Portable Bluetooth speaker with rich sound quality", 50.0, true),
                new Product(10L, "External Hard Drive", "Large capacity external hard drive for data storage", 120.0, true)
        );
        // Insert some products into the database
        productRepository.saveAll(sampleProducts);
    }
}
