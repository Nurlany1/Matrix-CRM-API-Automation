package az.crm.api.util;

import az.crm.api.config.ConfigProvider;
import io.restassured.response.Response;

import java.time.Instant;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;

public final class TokenManager {

    private static final long EXPIRY_SAFETY_MARGIN_SECONDS = 30;

    private static String accessToken;
    private static Instant expiresAt = Instant.EPOCH;

    private TokenManager() {
    }

    public static synchronized String getAccessToken() {
        if (accessToken == null || Instant.now().isAfter(expiresAt.minusSeconds(EXPIRY_SAFETY_MARGIN_SECONDS))) {
            login();
        }
        return accessToken;
    }

    private static void login() {
        Response response = given()
                .baseUri(ConfigProvider.baseUrl())
                .contentType("application/json")
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
                .when()
                .post("/api/Auth/Login")
                .then()
                .extract().response();

        if (response.statusCode() != 200 || !Boolean.TRUE.equals(response.path("isSuccess"))) {
            throw new IllegalStateException("Login failed for automation user. Status=" + response.statusCode()
                    + " body=" + response.asString());
        }

        accessToken = response.path("data.accessToken");
        String expiration = response.path("data.accessTokenExpiration");
        expiresAt = OffsetDateTime.parse(expiration).toInstant();
    }

    public static synchronized void invalidate() {
        accessToken = null;
        expiresAt = Instant.EPOCH;
    }
}
