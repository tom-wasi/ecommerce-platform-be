package com.springprojects.productservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.springprojects.productservice.dto.ProductRequest;
import com.springprojects.productservice.dto.ProductResponse;
import com.springprojects.productservice.model.Product;
import com.springprojects.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, productRepository.findAll().size());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Create some test products in the database
        new Product();
        productRepository.save(Product.builder()
                .name("iPhone 13")
                .description("iPhone 13")
                .price(BigDecimal.valueOf(1200))
                .build());

        // Perform a GET request to retrieve all products
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/products"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1))) // Assuming there are two products in the response
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // You can use ObjectMapper to convert the JSON response to a list of ProductResponse (adjust accordingly)
        ProductResponse[] products = objectMapper.readValue(response, ProductResponse[].class);

        // Assert the correct number of products is retrieved
        Assertions.assertEquals(1, products.length);
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("iPhone 13")
                .description("iPhone 13")
                .price(BigDecimal.valueOf(1200))
                .build();
    }

}