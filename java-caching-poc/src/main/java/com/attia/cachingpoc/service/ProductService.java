package com.attia.cachingpoc.service;

import com.attia.cachingpoc.entity.Product;
import com.attia.cachingpoc.repository.ProductRepository;
import org.redisson.api.RMapCache;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final RMapCache<Long, Product> productRMapCache;


    public ProductService(ProductRepository productRepository , RMapCache<Long, Product> productRMapCache) {
        this.productRepository = productRepository;
        this.productRMapCache = productRMapCache;
    }

    // Cache-Aside -> Read from cache; if there is a miss, go to DB
    public List<Product> getAllProducts() {
        List<Product> cachedProducts = (List<Product>) productRMapCache.readAllValues();

        // Check cache
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            return cachedProducts;
        }

        // Get products from DB
        List<Product> products = getProductsFromDB();

        // Cache the product listings with an expiration time
        setProductsInCache(products);

        return products;
    }

    public void setProductsInCache(List<Product> products) {
        // Set the list of products in the cache with a specified key and expiration time
        products.forEach(product -> {
            productRMapCache.put(product.getId(), product);

            // Add slight jitter to the expiration time (e.g., within 10% of the original duration)
            // Calculate jitter in milliseconds (e.g., within 10% of the original duration)
            Duration originalDuration = Duration.ofMinutes(10);
            Duration jitter = Duration.ofMinutes((long) (originalDuration.toMinutes() * 0.1 * Math.random()));
            Duration duration = originalDuration.plus(jitter);

            // Set the product expiration with jitter
            productRMapCache.expire(Instant.now().plus(duration));


        });
    }

    public List<Product> getProductsFromDB() {
        // Simulate fetching products from the database
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return productRepository.findAll();
    }

    // Write-Through -> Write on cache then write to DB
    public Product updateProduct(Product product) {
        updateProductInCache(product);
        return product;
    }

    private void updateProductInCache(Product product) {
        // This will automatically trigger the MapWriter to write to the database after updating cache.
        productRMapCache.put(product.getId(), product);
    }

    public Optional<Product> getProductById(Long productId) {
        return Optional.ofNullable(productRMapCache.get(productId));
    }
}