# Scaling Your App with Backend Caching Strategies Workshop
Workshop Materials: https://github.com/MuhammadAttia/caching-workshop

Muhammad Attia - Lead Software Engineer @ ELM

## What You Will Do

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

   - Visit [Spring Initializer](https://start.spring.io/) / open intellij
   - Choose project type: Maven Project.
   - Language: Java.
   - Spring Boot version: Latest stable version.
   - Group: com.example.
   - Artifact: product-management.
   - Add dependencies: "Spring Web," "Spring Data JPA," "H2 Database," and "Spring Data Redis."

2. **Generate Project:**

   Click the "Generate" button and download the generated project ZIP file.

3. **Extract the Project:**

   Extract the downloaded ZIP file to a directory of your choice.

## Step 3: Configure Redis for Caching

1. **Install Redis:**

   Download and install Redis on your system. Follow installation instructions for your platform on the [official Redis website](https://redis.io/download).

2. **Configure Redis for Your Spring Boot Application:**

   Open your project's `src/main/resources/application.properties` file and add the following Redis configuration:

   ```properties
   spring.redis.host=localhost
   spring.redis.port=6379
   ```
## step 4: update the pom file

```xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.2.1.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.attia</groupId>
	<artifactId>product-management</artifactId>
	<version>1.0.1-SNAPSHOT</version>
	<name>product-management</name>
	<description>user-management</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- JPA Data (We are going to use Repositories, Entities, Hibernate, etc...) -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.openapitools</groupId>
				<artifactId>openapi-generator-maven-plugin</artifactId>
				<version>6.0.1</version>
				<executions>
					<execution>
						<goals>
							<goal>generate</goal>
						</goals>
						<configuration>
							<inputSpec>${project.basedir}/OpenAPI/emApi.yaml</inputSpec>
							<generatorName>spring</generatorName>
							<!-- Source generation only -->
							<generateApiTests>false</generateApiTests>
							<generateModelTests>false</generateModelTests>
							<generateModelDocumentation>true</generateModelDocumentation>
							<generateApiDocumentation>true</generateApiDocumentation>

							<supportingFilesToGenerate>
								ApiUtil.java
							</supportingFilesToGenerate>
							<configOptions>
								<delegatePattern>false</delegatePattern>
								<interfaceOnly>true</interfaceOnly>
							</configOptions>
						</configuration>
					</execution>
				</executions>
			</plugin>

		</plugins>
	</build>

</project>

```

## Step 5: Create the Product Entity

Create a Product entity that represents the product details:

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean available;

    // Getters and setters
}
```
## Step 6: Create a Product Repository
Create a repository interface for accessing the product data:

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
}
Step 7: Implement Product Service
Create a ProductService that handles both read and write operations. We will use write-through caching for product updates.

java
Copy code
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Product> redisTemplate;

    public List<Product> getAllProducts() {
        String cacheKey = "allProducts";
        List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedProducts != null) {
            return cachedProducts;
        }

        List<Product> products = productRepository.findAll();

        // Cache the product listings with an expiration time
        redisTemplate.opsForValue().set(cacheKey, products, Duration.ofMinutes(10));

        return products;
    }

    public Product updateProduct(Product product) {
        // Update the product in the database
        Product updatedProduct = productRepository.save(product);

        // Update the product in the cache
        redisTemplate.opsForValue().set("product:" + updatedProduct.getId(), updatedProduct, Duration.ofMinutes(10));

        return updatedProduct;
    }
}
```
## Step 8: Create REST API Endpoints
Create REST API endpoints to retrieve product listings and update product details:

```java
@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PutMapping
    public Product updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }
}
```
## Step 9: build and Run the Application
Start your Spring Boot application, and it will be accessible at http://localhost:8080.

## Testing
Use tools like Postman or curl to make GET requests to retrieve product listings and PUT requests to update product details.

Monitor the Redis cache to see how data is cached, and observe that changes are reflected in the cache and the database.

This example demonstrates a simplified implementation of an e-commerce product catalog using Spring Boot and Redis with read-aside and write-through caching.
