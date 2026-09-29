package az.crm.api.tests.leads;

import az.crm.api.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Leads - Read")
class LeadsGetTests extends BaseTest {

    @Test
    @DisplayName("Returns a paged list with the expected envelope shape")
    void getLeads_returnsPagedList() {
        given()
                .spec(authSpec())
                .queryParam("Page", 1)
                .queryParam("PageSize", 5)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.items", notNullValue())
                .body("data.pageNumber", equalTo(1))
                .body("data.pageSize", equalTo(5))
                .body("data.items.size()", lessThanOrEqualTo(5));
    }

    @Test
    @DisplayName("Called with no Page or PageSize at all still returns a sensible default page")
    void getLeads_withNoPagingParams_returnsDefaults() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1))
                .body("data.pageSize", greaterThan(0));
    }

    @Test
    @DisplayName("Without auth returns 401")
    void getLeads_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Filtering by statusId only returns leads in that status")
    void getLeads_filteredByStatus_returnsOnlyMatchingStatus() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "statusId=8")
                .queryParam("PageSize", 50)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.items.status.id", everyItem(equalTo(8)));
    }

    @Test
    @DisplayName("Filtering by a statusId that no lead currently has returns an empty list, not an error")
    void getLeads_filteredByStatusWithNoMatches_returnsEmptyList() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "statusId=9999")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.totalCount", equalTo(0))
                .body("data.items", hasSize(0));
    }

    @Test
    @DisplayName("Fetching by an existing id returns that lead")
    void getLeadById_existingId_returnsLead() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", 20)
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("data.id", equalTo(20));
    }

    @Test
    @DisplayName("Fetching by a non-existent id returns 404")
    void getLeadById_nonExistentId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", 999999)
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("id=0 returns 404 rather than a server error")
    void getLeadById_zeroId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", 0)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("A negative id returns 404 rather than a server error")
    void getLeadById_negativeId_returns404() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", -1)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("A non-numeric id segment is rejected with 400, not routed as not-found")
    void getLeadById_nonNumericId_returns400() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", "abc")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("An id beyond the 32-bit integer range is rejected with 400, not a server error")
    void getLeadById_idBeyondIntRange_returns400() {
        given()
                .spec(authSpec())
        .when()
                .get("/api/Leads/{id}", "2147483648")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Without auth returns 401")
    void getLeadById_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
        .when()
                .get("/api/Leads/{id}", 20)
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Page=0 is treated as page 1 rather than erroring")
    void getLeads_pageZero_isClampedToFirstPage() {
        given()
                .spec(authSpec())
                .queryParam("Page", 0)
                .queryParam("PageSize", 5)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1));
    }

    @Test
    @DisplayName("A negative Page is treated as page 1 rather than erroring")
    void getLeads_negativePage_isClampedToFirstPage() {
        given()
                .spec(authSpec())
                .queryParam("Page", -1)
                .queryParam("PageSize", 5)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1));
    }

    @Test
    @DisplayName("PageSize=0 is clamped to a sane minimum rather than erroring")
    void getLeads_pageSizeZero_isClampedToMinimum() {
        given()
                .spec(authSpec())
                .queryParam("Page", 1)
                .queryParam("PageSize", 0)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.pageSize", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("An excessively large PageSize is capped rather than returning unbounded rows")
    void getLeads_hugePageSize_isCappedToMaximum() {
        given()
                .spec(authSpec())
                .queryParam("Page", 1)
                .queryParam("PageSize", 100_000)
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("data.pageSize", lessThanOrEqualTo(1000));
    }

    @Test
    @DisplayName("OrderBy=firstName is accepted and returns 200")
    void getLeads_orderByFirstName_returns200() {
        given()
                .spec(authSpec())
                .queryParam("OrderBy", "firstName")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("A descending OrderBy shorthand should not crash the request with 500")
    void getLeads_orderByDescendingShorthand_shouldNotReturn500() {
        given()
                .spec(authSpec())
                .queryParam("OrderBy", "-firstName")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("The documented Filter syntax works, e.g. statusId=8")
    void getLeads_withDocumentedFilterSyntax_returns200() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "statusId=8")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("A malformed Filter expression should return 400, not crash with 500")
    void getLeads_withMalformedFilter_shouldNotReturn500() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "#$garbage")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("The Gridify '==' equality operator on a valid field should not crash with 500")
    void getLeads_withDoubleEqualsFilter_shouldNotReturn500() {
        given()
                .spec(authSpec())
                .queryParam("Filter", "firstName==Test")
        .when()
                .get("/api/Leads")
        .then()
                .statusCode(lessThan(500));
    }
}
