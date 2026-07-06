package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a (partial) user resource as returned by
 * {@code GET /users/{id}} and within the {@code users} array of
 * {@code GET /users?limit=&skip=}.
 * <p>
 * dummyjson.com users have many more fields (address, bank, company, etc.)
 * than are modelled here; only the fields this suite actually asserts on are
 * declared, and {@link JsonIgnoreProperties} ensures the rest are silently
 * ignored during deserialization rather than breaking the mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String gender;
    private String image;
}
