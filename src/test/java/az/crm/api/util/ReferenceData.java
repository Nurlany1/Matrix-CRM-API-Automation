package az.crm.api.util;

import az.crm.api.config.ConfigProvider;
import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class ReferenceData {

    private static List<Map<String, Object>> leadStatuses;
    private static List<Map<String, Object>> consultationStatuses;
    private static List<Map<String, Object>> contactTypes;
    private static List<Map<String, Object>> contactSources;
    private static List<Map<String, Object>> employees;
    private static List<Map<String, Object>> trainings;

    private ReferenceData() {
    }

    private static JsonPath authGet(String path) {
        return given()
                .baseUri(ConfigProvider.baseUrl())
                .header("Authorization", "Bearer " + TokenManager.getAccessToken())
                .when()
                .get(path)
                .then()
                .extract().jsonPath();
    }

    private static synchronized void ensureLeadStatuses() {
        if (leadStatuses == null) {
            leadStatuses = authGet("/api/LookupValues/dropdown/LeadStatus").getList("data");
        }
    }

    private static synchronized void ensureConsultationStatuses() {
        if (consultationStatuses == null) {
            consultationStatuses = authGet("/api/LookupValues/dropdown/ConsultationStatus").getList("data");
        }
    }

    private static synchronized void ensureContactTypes() {
        if (contactTypes == null) {
            contactTypes = authGet("/api/LookupValues/dropdown/ContactType").getList("data");
        }
    }

    private static synchronized void ensureContactSources() {
        if (contactSources == null) {
            contactSources = authGet("/api/LookupValues/dropdown/ContactSource").getList("data");
        }
    }

    private static synchronized void ensureEmployees() {
        if (employees == null) {
            employees = authGet("/api/Employees/dropdown").getList("data");
        }
    }

    private static synchronized void ensureTrainings() {
        if (trainings == null) {
            trainings = authGet("/api/Trainings/dropdown").getList("data");
        }
    }

    @SuppressWarnings("unchecked")
    private static int firstIdWithBehavior(List<Map<String, Object>> items, String behaviorCode) {
        return items.stream()
                .filter(i -> i.get("behaviorCodes") != null && ((List<String>) i.get("behaviorCodes")).contains(behaviorCode))
                .map(i -> (Integer) i.get("id"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No lookup value found with behaviorCode=" + behaviorCode));
    }

    @SuppressWarnings("unchecked")
    private static int firstIdWithoutBehavior(List<Map<String, Object>> items, String behaviorCode) {
        return items.stream()
                .filter(i -> i.get("behaviorCodes") == null || !((List<String>) i.get("behaviorCodes")).contains(behaviorCode))
                .map(i -> (Integer) i.get("id"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No lookup value found without behaviorCode=" + behaviorCode));
    }

    public static int leadStatusIdWithBehavior(String behaviorCode) {
        ensureLeadStatuses();
        return firstIdWithBehavior(leadStatuses, behaviorCode);
    }

    public static int leadStatusIdWithoutBehavior(String behaviorCode) {
        ensureLeadStatuses();
        return firstIdWithoutBehavior(leadStatuses, behaviorCode);
    }

    public static int consultationStatusIdWithoutBehavior(String behaviorCode) {
        ensureConsultationStatuses();
        return firstIdWithoutBehavior(consultationStatuses, behaviorCode);
    }

    public static int anyContactTypeId() {
        ensureContactTypes();
        return (int) contactTypes.get(0).get("id");
    }

    public static int anotherContactTypeId() {
        ensureContactTypes();
        return (int) contactTypes.get(1).get("id");
    }

    public static int anySourceId() {
        ensureContactSources();
        return (int) contactSources.get(0).get("id");
    }

    public static int anyEmployeeId() {
        ensureEmployees();
        return (int) employees.get(0).get("id");
    }

    public static int anyTrainingId() {
        ensureTrainings();
        return (int) trainings.get(0).get("id");
    }
}
