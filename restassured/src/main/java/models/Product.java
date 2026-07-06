package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single product resource as returned by
 * {@code GET /products/{id}} and within the {@code products} array of
 * {@code GET /products?limit=}. Used in place of reqres.in's "colors"
 * resource to exercise a second resource type in this suite.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private int id;
    private String title;
    private double price;
    private String category;
}
