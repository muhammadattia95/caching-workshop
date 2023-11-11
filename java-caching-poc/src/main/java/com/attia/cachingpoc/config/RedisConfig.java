package com.attia.cachingpoc.config;

import com.attia.cachingpoc.entity.Product;
import com.attia.cachingpoc.repository.ProductRepository;
import org.redisson.Redisson;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapWriter;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Map;

@Configuration
public class RedisConfig {

    private static final String PRODUCT_CACHE_KEY = "products";

    private final ProductRepository productRepository;


    public RedisConfig(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Bean
    public RMapCache<Long, Product> productRMapCache(RedissonClient redissonClient) {
        return redissonClient.getMapCache(PRODUCT_CACHE_KEY,
                MapOptions.<Long, Product>defaults()
                        .writer(getMapWriter())
                        .writeMode(MapOptions.WriteMode.WRITE_THROUGH));
    }
    @Bean
    public RedissonClient redissonClient() {
        final Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        config.useSingleServer()
                .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }


    private MapWriter<Long, Product> getMapWriter() {
        return new MapWriter<>() {
            @Override
            public void write(final Map<Long, Product> map) {
                map.forEach((k, v) -> {
                    // Update the database here
                    updateDatabase(v);
                });
            }

            @Override
            public void delete(Collection<Long> keys) {
                // Delete from the database here
                keys.forEach(productRepository::deleteById);
            }
        };
    }

    private void updateDatabase(Product product) {
        // Perform the necessary database update logic here
        productRepository.save(product);
    }
}