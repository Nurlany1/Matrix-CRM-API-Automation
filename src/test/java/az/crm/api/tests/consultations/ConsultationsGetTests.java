package az.crm.api.tests.consultations;

import az.crm.api.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Consultations - Read")
class ConsultationsGetTests extends BaseTest {

    @Test
    @DisplayName("Returns a paged list with the expected envelope shape")
    void getConsultations_returnsPagedList() {
        given()
                .spec(authSpec())
                .queryParam("Page", 1)
                .queryParam("PageSize", 10)
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.items", notNullValue())
                .body("data.pageNumber", equalTo(1));
    }

    @Test
    @DisplayName("Called with no Page or PageSize at all still returns a sensible default page")
    void getConsultations_withNoPagingParams_returnsDefaults() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1))
                .body("data.pageSize", greaterThan(0));
    }

    @Test
    @DisplayName("Without auth returns 401")
    void getConsultations_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Fetching by a non-existent id returns 404")
    void getConsultationById_nonExistentId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Consultations/{id}", 999999)
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("id=0 returns 404 rather than a server error")
    void getConsultationById_zeroId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Consultations/{id}", 0)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Without auth returns 401")
    void getConsultationById_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .get("/api/Consultations/{id}", 999999)
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("The documented Filter syntax works, e.g. leadId=20")
    void getConsultations_withDocumentedFilterSyntax_returns200() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "leadId=20")
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("A malformed Filter expression should return 400, not crash with 500")
    void getConsultations_withMalformedFilter_shouldNotReturn500() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "#$garbage")
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("PageSize=0 is clamped to a sane minimum rather than erroring")
    void getConsultations_pageSizeZero_isClampedToMinimum() {
        given()
                .spec(authSpec())
                .queryParam("Page", 1)
                .queryParam("PageSize", 0)
        .when()
                .get("/api/Consultations")
        .then()
                .statusCode(200)
                .body("data.pageSize", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("An id beyond the 32-bit integer range is rejected with 400, not a server error")
    void getConsultationById_idBeyondIntRange_returns400() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Consultations/{id}", "2147483648")
        .then()
                .statusCode(400);
    }
}
