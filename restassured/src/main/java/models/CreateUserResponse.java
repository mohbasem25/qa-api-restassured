package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned by {@code POST /users/add} (simulated create -
 * dummyjson.com echoes the submitted fields and assigns a fake new id but
 * does not persist the record).
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResponse {
    private int id;
    private String firstName;
    private String lastName;
}
