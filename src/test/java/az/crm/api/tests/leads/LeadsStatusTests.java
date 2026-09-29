package az.crm.api.tests.leads;

import az.crm.api.base.BaseTest;
import az.crm.api.util.LeadPayloads;
import az.crm.api.util.ReferenceData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Leads - Status transitions")
class LeadsStatusTests extends BaseTest {

    private static final int EXISTING_LEAD_ID = 20;

    @Test
    @DisplayName("Moving to a status without NoteRequired behavior succeeds without a note")
    void updateStatus_toStatusWithoutNoteRequired_succeedsWithoutNote() {
        int contactedStatusId = ReferenceData.leadStatusIdWithoutBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(contactedStatusId, null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("Moving to a NoteRequired status without a note is rejected with 422")
    void updateStatus_toNoteRequiredStatus_withoutNote_returns422() {
        int cancelledStatusId = ReferenceData.leadStatusIdWithBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(cancelledStatusId, null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(422)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Sending an empty string as the note for a NoteRequired status is treated the same as no note")
    void updateStatus_toNoteRequiredStatus_withBlankNote_returns422() {
        int cancelledStatusId = ReferenceData.leadStatusIdWithBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(cancelledStatusId, ""))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(422)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Moving to a NoteRequired status with a note succeeds")
    void updateStatus_toNoteRequiredStatus_withNote_succeeds() {
        int cancelledStatusId = ReferenceData.leadStatusIdWithBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(cancelledStatusId, "Cancelled by REST Assured automation"))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("Once a lead reaches a Final-behavior status, it can still be moved back to a non-final one")
    void updateStatus_fromFinalStatus_canStillTransitionBack() {
        int finalStatusId = ReferenceData.leadStatusIdWithBehavior("Final");
        int initialStatusId = ReferenceData.leadStatusIdWithoutBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(finalStatusId, "moved to a final state by automation"))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(200);

        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(initialStatusId, null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    @DisplayName("Updating the status of a non-existent lead returns 404")
    void updateStatus_nonExistentLead_returns404() {
        int anyStatusId = ReferenceData.leadStatusIdWithoutBehavior("NoteRequired");

        given()
                .spec(authSpec())
                .pathParam("id", 999999)
                .body(LeadPayloads.statusUpdate(anyStatusId, null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("Updating to a non-existent statusId returns 404")
    void updateStatus_nonExistentStatusId_returns404() {
        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(9_999_999, null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    @DisplayName("An empty body with no leadStatusId is treated as an unknown status and returns 404")
    void updateStatus_withEmptyBody_returns404() {
        given()
                .spec(authSpec())
                .pathParam("id", EXISTING_LEAD_ID)
                .body("{}")
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Without auth returns 401")
    void updateStatus_withoutAuth_returns401() {
        given()
                .spec(anonymousSpec)
                .pathParam("id", EXISTING_LEAD_ID)
                .body(LeadPayloads.statusUpdate(ReferenceData.leadStatusIdWithoutBehavior("NoteRequired"), null))
        .when()
                .patch("/api/Leads/{id}/status")
        .then()
                .statusCode(401);
    }
}
