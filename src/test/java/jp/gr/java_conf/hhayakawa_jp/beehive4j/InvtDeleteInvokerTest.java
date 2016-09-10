package jp.gr.java_conf.hhayakawa_jp.beehive4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import jp.gr.java_conf.hhayakawa_jp.beehive4j.BeehiveApiDefinitions;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.BeehiveContext;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.BeehiveResponse;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.InvtCreateInvoker;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.InvtDeleteInvoker;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.exception.Beehive4jException;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.exception.BeehiveApiFaultException;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.BeeId;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.ChangeStatus;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.MeetingCreator;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.MeetingParticipantUpdater;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.MeetingParticipantUpdaterOperation;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.MeetingUpdater;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.OccurrenceParticipantStatus;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.OccurrenceStatus;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.OccurrenceType;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.Priority;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.TimedTrigger;
import jp.gr.java_conf.hhayakawa_jp.beehive4j.model.Transparency;

public class InvtDeleteInvokerTest {

    private static final String calendar_id =
            "334B:3BF0:clnd:38893C00F42F38A1E0404498C8A6612B000B1A7E0450";

    private BeehiveContext context = null;

    private String invitation_id = null;

    @Before
    public void setUp() throws Exception {
        context = TestUtils.setUpContext();

        // BeeId
        BeeId calendar = new BeeId(calendar_id, null);

        // MeetingUpdater
        ZonedDateTime start = ZonedDateTime.of(
                2016, 6, 5, 13, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
        ZonedDateTime end = ZonedDateTime.of(
                2016, 6, 5, 14, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
        String name = "Test String of name.";
        ChangeStatus changeStatus = null;
        ZonedDateTime userCreatedOn = null;
        ZonedDateTime userModifiedOn = null;
        boolean includeOnlineConference = false;
        OccurrenceParticipantStatus inviteeParticipantStatus =
                OccurrenceParticipantStatus.ACCEPTED;
        TimedTrigger inviteePrimaryClientReminderTrigger = null;
        Priority inviteePriority = Priority.MEDIUM;
        Transparency inviteeTransparency = Transparency.TRANSPARENT;
        String locationName = "JP-OAC-CONF-17006_17M1";
        List<MeetingParticipantUpdater> participantUpdaters = 
                new ArrayList<MeetingParticipantUpdater>(1);
        participantUpdaters.add(new MeetingParticipantUpdater(
                "mailto:JP-OAC-CONF-17006_17M1@oracle.com", null,
                MeetingParticipantUpdaterOperation.ADD,
                new BeeId("334B:3BF0:bkrs:38893C00F42F38A1E0404498C8A6612B0001DDD86644", null)));
        OccurrenceStatus status = OccurrenceStatus.TENTATIVE;
        String textDescription = "Test String of testDescription.";
        String xhtmlFragmentDescription = null;
        MeetingUpdater meetingUpdater = new MeetingUpdater(
                name, changeStatus, userCreatedOn, userModifiedOn,
                end,
                includeOnlineConference, inviteeParticipantStatus,
                inviteePrimaryClientReminderTrigger, inviteePriority,
                inviteeTransparency, locationName, participantUpdaters,
                start,
                status, textDescription, xhtmlFragmentDescription);
        
        // OccurenceType
        OccurrenceType type = OccurrenceType.MEETING;

        MeetingCreator meetingCreater =
                new MeetingCreator(calendar, meetingUpdater, type);
        InvtCreateInvoker invoker =
                context.getInvoker(BeehiveApiDefinitions.TYPEDEF_INVT_CREATE);
        invoker.setRequestPayload(meetingCreater);
        try {
            ResponseEntity<BeehiveResponse> response = invoker.invoke();
            JsonNode json = response.getBody().getJson();
            invitation_id = json.get("collabId").get("id").asText();
        } catch (BeehiveApiFaultException e) {
            System.out.println(e.getCause().getMessage());
            fail(e.getMessage());
        }
    }

    @Test
    public void test() {
        InvtDeleteInvoker invoker =
                context.getInvoker(BeehiveApiDefinitions.TYPEDEF_INVT_DELETE);
        invoker.setPathValue(invitation_id);
        try {
            ResponseEntity<BeehiveResponse> response = invoker.invoke();
            assertEquals("Response code is expected to be 204 (No Content)",
                    HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull("Response body is expected to be null.",
                    response.getBody());
        } catch (Beehive4jException e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        }
    }

}
