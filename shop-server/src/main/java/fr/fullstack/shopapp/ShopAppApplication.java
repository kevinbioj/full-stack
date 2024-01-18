package fr.fullstack.shopapp;

import fr.fullstack.shopapp.repository.ProductRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition
@SpringBootApplication
public class ShopAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner syncProducts(ProductRepository productRepository) {
        return (args) -> {
            // Hibernate Search va automatiquement synchroniser sur
            // ElasticSearch ce qui n'est pas encore peuplé en données.
            var products = productRepository.findAll();
            productRepository.saveAll(products);
        };
    }
}
