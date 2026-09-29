package az.crm.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LeadPayloads {

    private LeadPayloads() {
    }

    public static Map<String, Object> validCreate(String firstName, String lastName) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("applicationDate", "2026-09-18T00:00:00Z");
        body.put("contacts", List.of(contact(ReferenceData.anyContactTypeId(), "+994501112233")));
        body.put("trainingIds", List.of(ReferenceData.anyTrainingId()));
        body.put("otherTrainingName", null);
        body.put("sourceIds", List.of(ReferenceData.anySourceId()));
        body.put("additionalNote", "created by REST Assured automation");
        return body;
    }

    public static Map<String, Object> contact(int contactTypeId, String value) {
        Map<String, Object> contact = new HashMap<>();
        contact.put("contactTypeId", contactTypeId);
        contact.put("value", value);
        return contact;
    }

    public static Map<String, Object> missingContacts(String firstName, String lastName) {
        Map<String, Object> body = validCreate(firstName, lastName);
        body.put("contacts", new ArrayList<>());
        return body;
    }

    public static Map<String, Object> missingSources(String firstName, String lastName) {
        Map<String, Object> body = validCreate(firstName, lastName);
        body.put("sourceIds", new ArrayList<>());
        return body;
    }

    public static Map<String, Object> update(int id, String firstName, String lastName) {
        Map<String, Object> body = validCreate(firstName, lastName);
        body.put("id", id);
        return body;
    }

    public static Map<String, Object> statusUpdate(int leadStatusId, String statusNote) {
        Map<String, Object> body = new HashMap<>();
        body.put("leadStatusId", leadStatusId);
        body.put("statusNote", statusNote);
        return body;
    }
}
