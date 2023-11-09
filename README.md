# Scaling Your App with Backend Caching Strategies Workshop
Workshop Materials: https://github.com/MuhammadAttia/caching-workshop

Muhammad Attia - Lead Software Engineer @ ELM

## Doing the Workshop on Your Own

### problem Statment 
In an e-commerce platform product catalog. often experience heavy read traffic as users browse and search for products, and product details may be updated periodically. so we need to use the cache alongside other techniques to enhance the Service performance.


### Solution

In this scenario, read-aside caching is used to optimize the read-heavy operations, serving users with frequently accessed data quickly from the cache. Write-through caching ensures that data consistency is maintained when product updates occur, preventing the cache from serving stale information.
This combination allows the e-commerce platform to deliver a responsive user experience while keeping the product catalog data up to date. It strikes a balance between optimizing read performance and maintaining data integrity, making it a suitable strategy for this real-life use case.

This guide will help you set up a Spring Boot project that uses Spring Data, Redis for caching, and H2 as an in-memory database. We'll create a simple product catalog management application and apply caching strategies. 

The guide covers creating a Spring Boot project, configuring Redis, building entities and repositories, implementing caching strategies, and testing the application.

I'll walk you through building an e-commerce product catalog application using Spring Boot and Redis for caching. This example will cover read-aside and write-through caching for product listings and updates. Make sure you have Redis installed and running on your system.

## Prerequisites
Everyone will need:

- Basic knowledge of Java, and Spring boot (Basics ).

- [JDK 8 or higher](https://openjdk.java.net/install/index.html) installed. **Ensure you have a JDK installed and not just a JRE**
- [docker](https://docs.docker.com/install/) installed.
- [redis](https://hub.docker.com/_/redis) installed.
- [maven](https://maven.apache.org/install.html) installed.


## Step 1: Set Up the Development Environment

- **Install Java:** Ensure you have Java 8 or later installed on your system.

- **Install Maven:** Install Apache Maven as your build tool. Download it from the [Apache Maven website](https://maven.apache.org/download.cgi).

## Step 2: Create a Spring Boot Project

1. **Use Spring Initializer:**

   - Visit [Spring Initializer](https://start.spring.io/) / open IntelliJ
   - Choose project type: Maven Project.
   - Language: Java.
   - Spring Boot version: Latest stable version.
   - Group: com.example.
   - Artifact: product-management.
   - Add dependencies: "Spring Web," "Spring Data JPA," "H2 Database," Lombok ", and "Spring Data Redis."

2. **Generate Project:**

   Click the "Generate" button and download the generated project ZIP file.

3. **Extract the Project:**

   Extract the downloaded ZIP file to a directory of your choice.

## Step 3: Configure Redis for Caching

1. **Install Redis:**

   Download and install Redis on your system. Follow installation instructions for your platform on the [official Redis website](https://redis.io/download).

2. **Configure Redis for Your Spring Boot Application:**

   Open your project's `src/main/resources/application.properties` file and add the following Redis and H2 configuration:

   ```properties
	spring.data.redis.host=localhost
	spring.data.redis.port=6379
	spring.datasource.url=jdbc:h2:mem:testdb
	spring.datasource.driverClassName=org.h2.Driver
	spring.datasource.username=sa
	spring.datasource.password=password
	spring.h2.console.enabled=true
	spring.h2.console.path=/h2-console
   ```
   3. *** Add Redis Config
	create config package and on this package add the Redis config class
	```java
	 @Configuration
	public class RedisConfig {
	
	    @Bean
	    public RedisTemplate<String, Product> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
	        RedisTemplate<String, Product> redisTemplate = new RedisTemplate<>();
	        redisTemplate.setConnectionFactory(redisConnectionFactory);
	        // Configure key and value serializers if necessary
	        redisTemplate.setKeySerializer(new StringRedisSerializer());
	        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Product.class));
	        return redisTemplate;
	    }
	
	}
	```
## Step 4: update the pom file

```xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.attia</groupId>
    <artifactId>caching-poc</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>caching-poc</name>
    <description>caching-poc</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>


```

## Step 5: Create the Product Entity

Create a Product entity that represents the product details:

```java
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean available;

}
```
## Step 6: Create a Product Repository
Create a repository interface for accessing the product data:

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```
## Step 7: Implement Product Service
Create a ProductService that handles both read and write operations. We will use write-through caching for product updates.

```java

@Service
public class ProductService {
    private final ProductRepository productRepository;

    private final RedisTemplate<String, Product> redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "products";

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Product> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    // cache aside -> read from cache if there is a miss go to DB
    public List<Product> getAllProducts() {
        List<Product> cachedProducts = redisTemplate.opsForList().range(PRODUCT_CACHE_KEY, 0, -1);

        // check cache
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            return cachedProducts;
        }

        // get products from DB
        List<Product> products = getProductsFromDB();

        // Cache the product listings with an expiration time
        setProductsInCache(products);

        return products;
    }

    public void setProductsInCache(List<Product> products) {
        // Set the list of products in the cache with a specified key and expiration time
        redisTemplate.opsForList().rightPushAll(PRODUCT_CACHE_KEY, products);
        redisTemplate.expire(PRODUCT_CACHE_KEY, Duration.ofMinutes(10));
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

    // Write through
    public Product updateProduct(Product product) {
        // Update the product in the database
        Product updatedProduct = productRepository.save(product);

        // Update the product in the cache
        List<Product> cachedProducts = redisTemplate.opsForList().range(PRODUCT_CACHE_KEY, 0, -1);

        if (cachedProducts != null) {
            cachedProducts.removeIf(p -> p.getId().equals(updatedProduct.getId()));
            cachedProducts.add(updatedProduct);

            // Clear the existing cache
            redisTemplate.delete(PRODUCT_CACHE_KEY);

            // Update the cache with the modified list
            redisTemplate.opsForList().rightPushAll(PRODUCT_CACHE_KEY, cachedProducts);
            redisTemplate.expire(PRODUCT_CACHE_KEY, Duration.ofMinutes(10));
        }

        return updatedProduct;
    }


    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }
}

```
## Step 8: Create REST API Endpoints
Create REST API endpoints to retrieve product listings and update product details:

```java
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product updatedProduct) {
        Optional<Product> productOptional = productService.getProductById(productId);

        if (productOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();

        // Update the product details
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setAvailable(updatedProduct.isAvailable());

        // Save the updated product
        Product updated = productService.updateProduct(product);

        return ResponseEntity.ok(updated);
    }

}
```
## Step 9 : Update the main class by adding some product to DB for testing purposes 

```java
	@SpringBootApplication
	public class CachingPocApplication implements CommandLineRunner {
	
	    private final ProductRepository productRepository;
	
	    public CachingPocApplication(ProductRepository productRepository) {
		this.productRepository = productRepository;
	    }
	
	    public static void main(String[] args) {
		SpringApplication.run(CachingPocApplication.class, args);
	    }
	
	    @Override
	    public void run(String... args) throws Exception {
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
```
## Step 10: build and Run the Application
Start your Spring Boot application, and it will be accessible at http://localhost:8080, and DB on http://localhost:8080/h2-console

## Testing
Use tools like Postman or curl to make GET requests to retrieve product listings and PUT requests to update product details.

Monitor the Redis cache to see how data is cached (you can use redis CLI or another redis software), and observe that changes are reflected in the cache and the database.

This example demonstrates a simplified implementation of an e-commerce product catalog using Spring Boot and Redis with read-aside and write-through caching.
