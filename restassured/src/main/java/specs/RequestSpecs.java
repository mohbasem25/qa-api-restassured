package specs;

import config.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.PrintStream;

/**
 * Reusable {@link RequestSpecification} builders.
 * <p>
 * Every request built through this class automatically carries the base URI,
 * base path, the {@code x-api-key} header required by reqres.in's free tier,
 * sane connection/socket timeouts, and request/response logging that is only
 * emitted when a test fails (keeps CI logs readable on the happy path).
 */
public final class RequestSpecs {

    private RequestSpecs() {
        // static utility class
    }

    /**
     * Base spec shared by all requests: base URI/path, API key header,
     * JSON content type, timeouts, and failure-only logging.
     */
    public static RequestSpecification baseSpec() {
        PrintStream logStream = System.out;

        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", ApiConfig.connectionTimeoutMs())
                        .setParam("http.socket.timeout", ApiConfig.socketTimeoutMs()));

        return new RequestSpecBuilder()
                .setBaseUri(ApiConfig.baseUri())
                .setBasePath(ApiConfig.basePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader(ApiConfig.apiKeyHeader(), ApiConfig.apiKeyValue())
                .setConfig(config)
                // Logging filters fire only when a test assertion fails.
                .addFilter(new RequestLoggingFilter(LogDetail.ALL, logStream))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL, logStream))
                .build();
    }

    /**
     * Same as {@link #baseSpec()} but without any auth header, used to
     * exercise/verify the API's behaviour when the API key is missing.
     */
    public static RequestSpecification specWithoutApiKey() {
        return new RequestSpecBuilder()
                .setBaseUri(ApiConfig.baseUri())
                .setBasePath(ApiConfig.basePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
