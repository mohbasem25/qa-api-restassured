package specs;

import config.ApiConfig;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

/**
 * Reusable {@link ResponseSpecification} builders encapsulating common
 * response-level expectations (status code, content type, response time SLA)
 * so individual tests stay focused on business assertions.
 */
public final class ResponseSpecs {

    private ResponseSpecs() {
        // static utility class
    }

    public static ResponseSpecification expectStatus(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectResponseTime(org.hamcrest.Matchers.lessThan(ApiConfig.responseTimeSlaMs()))
                .build();
    }

    public static ResponseSpecification expectJsonStatus(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(org.hamcrest.Matchers.lessThan(ApiConfig.responseTimeSlaMs()))
                .build();
    }
}
