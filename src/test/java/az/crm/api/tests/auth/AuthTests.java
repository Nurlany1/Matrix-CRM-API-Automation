package az.crm.api.tests.auth;

import az.crm.api.base.BaseTest;
import az.crm.api.config.ConfigProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Auth")
class AuthTests extends BaseTest {

    @Test
    @DisplayName("Login with valid credentials returns 200 and an access token")
    void login_withValidCredentials_returnsAccessToken() {
        given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Login with valid credentials but an uppercase-cased username still succeeds")
    void login_withUppercaseUsername_stillSucceeds() {
        given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username().toUpperCase() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("Login with wrong password is rejected")
    void login_withWrongPassword_isRejected() {
        given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"wrong-password-123\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Login with unknown username is rejected")
    void login_withUnknownUser_isRejected() {
        given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"no-such-user-" + System.currentTimeMillis() + "@example.com\",\"password\":\"123456\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Login with an empty body is rejected with a client error, not a 500")
    void login_withEmptyBody_isRejectedGracefully() {
        given()
                .spec(anonymousSpec)
                .body("{}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Login with the password field entirely omitted returns 400")
    void login_withoutPasswordField_returns400() {
        given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Protected endpoint without an Authorization header returns 401")
    void protectedCrmEndpoint_withoutToken_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Protected endpoint with a malformed Bearer token returns 401")
    void protectedCrmEndpoint_withGarbageToken_returns401() {
        given()
                .spec(anonymousSpec)
                .header("Authorization", "Bearer this.is.not.a.valid.jwt")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("RefreshToken with a valid refresh token issues a new access token")
    void refreshToken_withValidToken_issuesNewAccessToken() {
        String refreshToken = given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(200)
                .extract().path("data.refreshToken").toString();

        given()
                .spec(anonymousSpec)
                .body("{\"refreshToken\":\"" + refreshToken + "\"}")
        .when()
                .post("/api/Auth/RefreshToken")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.accessToken", notNullValue());
    }

    @Test
    @DisplayName("RefreshToken with an unknown token is rejected")
    void refreshToken_withUnknownToken_returns401() {
        given()
                .spec(anonymousSpec)
                .body("{\"refreshToken\":\"garbage-refresh-token-value\"}")
        .when()
                .post("/api/Auth/RefreshToken")
        .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Logout with a valid token returns 200")
    void logout_withValidToken_returns200() {
        given()
                .spec(authSpec())
        .when()
                .post("/api/Auth/Logout")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("Logout without a token returns 401")
    void logout_withoutToken_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .post("/api/Auth/Logout")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("After Logout, the same access token is immediately rejected on the next request")
    void afterLogout_sameAccessToken_isRejected() {
        String freshToken = given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(200)
                .extract().path("data.accessToken").toString();

        given()
                .spec(anonymousSpec)
                .header("Authorization", "Bearer " + freshToken)
        .when()
                .post("/api/Auth/Logout")
        .then()
                .statusCode(200);

        given()
                .spec(anonymousSpec)
                .header("Authorization", "Bearer " + freshToken)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("After Logout, the matching refresh token is also invalidated")
    void afterLogout_matchingRefreshToken_isInvalidated() {
        var login = given()
                .spec(anonymousSpec)
                .body("{\"userName\":\"" + ConfigProvider.username() + "\",\"password\":\"" + ConfigProvider.password() + "\"}")
        .when()
                .post("/api/Auth/Login")
        .then()
                .statusCode(200)
                .extract();

        String freshToken = login.path("data.accessToken");
        String freshRefresh = login.path("data.refreshToken");

        given()
                .spec(anonymousSpec)
                .header("Authorization", "Bearer " + freshToken)
        .when()
                .post("/api/Auth/Logout")
        .then()
                .statusCode(200);

        given()
                .spec(anonymousSpec)
                .body("{\"refreshToken\":\"" + freshRefresh + "\"}")
        .when()
                .post("/api/Auth/RefreshToken")
        .then()
                .statusCode(401);
    }
}
