package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single user resource as returned by
 * {@code GET /api/users/{id}} and within the {@code data} array of
 * {@code GET /api/users?page=}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;
    private String email;
    private String first_name;
    private String last_name;
    private String avatar;
}
