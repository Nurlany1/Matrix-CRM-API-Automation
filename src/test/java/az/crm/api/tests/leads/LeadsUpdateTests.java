package az.crm.api.tests.leads;

import az.crm.api.base.BaseTest;
import az.crm.api.util.LeadPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@DisplayName("Leads - Update")
class LeadsUpdateTests extends BaseTest {

    private static final int EXISTING_LEAD_ID = 20;

    @Test
    @DisplayName("Updating an existing lead should return 200 but returns 405 Method Not Allowed")
    void updateLead_withValidPayload_returns200() {
        given()
                .spec(authSpec())
                .body(LeadPayloads.update(EXISTING_LEAD_ID, "Nurlan", "Yunusov (updated by automation)"))
        .when()
                .put("/api/Leads")
        .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Updating a non-existent lead should return 404 but returns 405 Method Not Allowed")
    void updateLead_nonExistentId_returns404() {
        given()
                .spec(authSpec())
                .body(LeadPayloads.update(999999, "Ghost", "Lead"))
        .when()
                .put("/api/Leads")
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Even a completely empty body is still blocked at 405 before any validation runs")
    void updateLead_withEmptyBody_isBlockedAt405() {
        given()
                .spec(authSpec())
                .body("{}")
        .when()
                .put("/api/Leads")
        .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("The 405 block happens even without auth - it happens before the app's own auth check")
    void updateLead_withoutAuth_isBlockedBeforeAuthCheck() {
        given()
                .spec(anonymousSpec)
                .body(LeadPayloads.update(EXISTING_LEAD_ID, "NoAuth", "User"))
        .when()
                .put("/api/Leads")
        .then()
                .statusCode(405)
                .header("Allow", containsString("OPTIONS"));
    }
}
