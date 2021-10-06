package org.sagebionetworks.bridge.sdk.integration;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.bridge.rest.api.ForAdminsApi;
import org.sagebionetworks.bridge.rest.api.ForDevelopersApi;
import org.sagebionetworks.bridge.rest.api.ForResearchersApi;
import org.sagebionetworks.bridge.rest.model.ForwardCursorPagedResourceList;
import org.sagebionetworks.bridge.rest.model.HealthDataDocumentation;
import org.sagebionetworks.bridge.rest.model.HealthDataDocumentationList;
import org.sagebionetworks.bridge.rest.model.Role;
import org.sagebionetworks.bridge.user.TestUserHelper;
import org.sagebionetworks.bridge.user.TestUserHelper.TestUser;

import java.io.EOFException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class HealthDataDocumentationTest {
    private static TestUser researcher;
    private static TestUser developer;
    private static TestUser admin;
    private static DateTime modifiedOn;
    private String identifier1;
    private String identifier2;
    private String identifier3;

    private TestUser user;

    private String DOCUMENTATION = "test-documentation";
    private Integer PAGE_SIZE = 10;

    @Before
    public void before() throws IOException {
        admin = TestUserHelper.getSignedInAdmin();
        researcher = TestUserHelper.createAndSignInUser(HealthDataDocumentationTest.class, false, Role.RESEARCHER);
        developer = TestUserHelper.createAndSignInUser(HealthDataDocumentationTest.class, false, Role.DEVELOPER);
        user = TestUserHelper.createAndSignInUser(HealthDataDocumentationTest.class, true);
        modifiedOn = DateTime.now(DateTimeZone.UTC);

        identifier1 = Tests.randomIdentifier(HealthDataDocumentationTest.class);
        identifier2 = Tests.randomIdentifier(HealthDataDocumentationTest.class);
        identifier3 = Tests.randomIdentifier(HealthDataDocumentationTest.class);
    }

    @After
    public void after() throws IOException {
        ForAdminsApi adminsApi = admin.getClient(ForAdminsApi.class);
        adminsApi.deleteAllParticipantDataForAdmin(user.getAppId(), user.getUserId());

        if (user != null) {
            user.signOutAndDeleteUser();
        }
        if (developer != null) {
            developer.signOutAndDeleteUser();
        }
        if (researcher != null) {
            researcher.signOutAndDeleteUser();
        }
    }

    @Test
    public void researcherCanCreateReadHealthDataDocumentation() throws IOException {
        ForResearchersApi researchersApi = researcher.getClient(ForResearchersApi.class);

        HealthDataDocumentation doc1 = createHealthDataDocumentation("foo",identifier1, DOCUMENTATION);
        HealthDataDocumentation doc2 = createHealthDataDocumentation("bar", identifier2, DOCUMENTATION);
        HealthDataDocumentation doc3 = createHealthDataDocumentation("baz", identifier3, DOCUMENTATION);

        // researcher can create health data documentation
        researchersApi.createOrUpdateHealthDataDocumentation(doc1).execute();
        researchersApi.createOrUpdateHealthDataDocumentation(doc2).execute();
        researchersApi.createOrUpdateHealthDataDocumentation(doc3).execute();

        // researcher can get a health data documentation
        HealthDataDocumentation retrievedDoc1 = researchersApi.getHealthDataDocumentationForId(identifier1).execute().body();
        HealthDataDocumentation retrievedDoc2 = researchersApi.getHealthDataDocumentationForId(identifier2).execute().body();
        HealthDataDocumentation retrievedDoc3 = researchersApi.getHealthDataDocumentationForId(identifier3).execute().body();
        assertHealthDataDocumentation(retrievedDoc1, doc1);
        assertHealthDataDocumentation(retrievedDoc2, doc2);
        assertHealthDataDocumentation(retrievedDoc3, doc3);

        // admin can delete health data documentation
        ForAdminsApi adminsApi = admin.getClient(ForAdminsApi.class);

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier1).execute();
        try {
            researchersApi.getHealthDataDocumentationForId(identifier1).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier2).execute();
        try {
            researchersApi.getHealthDataDocumentationForId(identifier2).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier3).execute();
        try {
            researchersApi.getHealthDataDocumentationForId(identifier3).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }
    }

    @Test
    public void developerCanCreateReadHealthDataDocumentation() throws IOException {
        ForDevelopersApi devsApi = developer.getClient(ForDevelopersApi.class);

        HealthDataDocumentation doc1 = createHealthDataDocumentation("foo",identifier1, DOCUMENTATION);
        HealthDataDocumentation doc2 = createHealthDataDocumentation("bar", identifier2, DOCUMENTATION);
        HealthDataDocumentation doc3 = createHealthDataDocumentation("baz", identifier3, DOCUMENTATION);

        // researcher can create health data documentation
        devsApi.createOrUpdateHealthDataDocumentation(doc1).execute();
        devsApi.createOrUpdateHealthDataDocumentation(doc2).execute();
        devsApi.createOrUpdateHealthDataDocumentation(doc3).execute();

        // researcher can get a health data documentation
        HealthDataDocumentation retrievedDoc1 = devsApi.getHealthDataDocumentationForId(identifier1).execute().body();
        HealthDataDocumentation retrievedDoc2 = devsApi.getHealthDataDocumentationForId(identifier2).execute().body();
        HealthDataDocumentation retrievedDoc3 = devsApi.getHealthDataDocumentationForId(identifier3).execute().body();
        assertHealthDataDocumentation(retrievedDoc1, doc1);
        assertHealthDataDocumentation(retrievedDoc2, doc2);
        assertHealthDataDocumentation(retrievedDoc3, doc3);

        // admin can delete health data documentation
        ForAdminsApi adminsApi = admin.getClient(ForAdminsApi.class);

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier1).execute();
        try {
            devsApi.getHealthDataDocumentationForId(identifier1).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier2).execute();
        try {
            devsApi.getHealthDataDocumentationForId(identifier2).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        adminsApi.deleteHealthDataDocumentationForIdentifier(identifier3).execute();
        try {
            devsApi.getHealthDataDocumentationForId(identifier3).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }
    }

    @Test
    public void testAdminCanDeleteAll() throws IOException {
        ForResearchersApi researchersApi = researcher.getClient(ForResearchersApi.class);

        HealthDataDocumentation doc1 = createHealthDataDocumentation("foo",identifier1, DOCUMENTATION);
        HealthDataDocumentation doc2 = createHealthDataDocumentation("bar", identifier2, DOCUMENTATION);
        HealthDataDocumentation doc3 = createHealthDataDocumentation("baz", identifier3, DOCUMENTATION);

        String parentId = "test-parent-id";
        doc1.setParentId(parentId);
        doc2.setParentId(parentId);
        doc3.setParentId(parentId);

        researchersApi.createOrUpdateHealthDataDocumentation(doc1).execute();
        researchersApi.createOrUpdateHealthDataDocumentation(doc2).execute();
        researchersApi.createOrUpdateHealthDataDocumentation(doc3).execute();

        ForAdminsApi adminsApi = admin.getClient(ForAdminsApi.class);
        adminsApi.deleteAllHealthDataDocumentationForParentId(parentId).execute();

        try {
            researchersApi.getHealthDataDocumentationForId(identifier1).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        try {
            researchersApi.getHealthDataDocumentationForId(identifier2).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }

        try {
            researchersApi.getHealthDataDocumentationForId(identifier3).execute().body();
            fail("expected exception");
        } catch (EOFException e){
            // expected exception, the doc was deleted
        }
    }

    @Test
    public void testPaginationGetAllHealthDataDocumentation() throws IOException {
        ForDevelopersApi devsApi = developer.getClient(ForDevelopersApi.class);

        // create 10 health data documentations and save data
        HealthDataDocumentation[] docArray = new HealthDataDocumentation[PAGE_SIZE];
        String parentId = "test-parent-id";
        for (int i = 0; i < PAGE_SIZE; i++) {
            docArray[i] = createHealthDataDocumentation("title" + i, "id-" + i, "doc-" + i);
            docArray[i].setParentId(parentId);
            devsApi.createOrUpdateHealthDataDocumentation(docArray[i]).execute();
        }

        // get first 5 health data documentation
        HealthDataDocumentationList pagedResults = devsApi.getAllHealthDataDocumentationForParentId(
                parentId, null, 5).execute().body();

        // check the first 5
        assertPages(pagedResults, 0);
        String nextKey = pagedResults.getNextPageOffsetKey();
        assertNotNull(nextKey);

        // check the remaining 5
        pagedResults = devsApi.getAllHealthDataDocumentationForParentId(parentId, nextKey, 5).execute().body();
        assertPages(pagedResults, 5);
        assertNull(pagedResults.getNextPageOffsetKey());
    }

    private static void assertPages(HealthDataDocumentationList list, int start) {
        assertEquals(5, list.getItems().size());
        for (int i = start; i < 5 + start; i++) {
            assertEquals(list.getItems().get(i - start).getTitle(), "title" + (i));
        }
    }

    private static HealthDataDocumentation createHealthDataDocumentation(String title, String identifier, String documentation) {
        HealthDataDocumentation doc = new HealthDataDocumentation();
        doc.setTitle(title);
        doc.setIdentifier(identifier);
        doc.setDocumentation(documentation);
        return doc;
    }

    private static void assertHealthDataDocumentation(HealthDataDocumentation doc1, HealthDataDocumentation doc2) {
        assertEquals(doc1.getTitle(), doc2.getTitle());
        assertEquals(doc1.getIdentifier(), doc2.getIdentifier());
        assertEquals(doc1.getDocumentation(), doc2.getDocumentation());
    }
}
