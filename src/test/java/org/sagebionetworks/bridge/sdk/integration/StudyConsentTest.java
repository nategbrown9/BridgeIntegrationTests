package org.sagebionetworks.bridge.sdk.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.sagebionetworks.bridge.sdk.Roles;
import org.sagebionetworks.bridge.sdk.StudyConsentClient;
import org.sagebionetworks.bridge.sdk.SubpopulationClient;
import org.sagebionetworks.bridge.sdk.integration.TestUserHelper.TestUser;
import org.sagebionetworks.bridge.sdk.exceptions.BridgeSDKException;
import org.sagebionetworks.bridge.sdk.models.ResourceList;
import org.sagebionetworks.bridge.sdk.models.holders.GuidVersionHolder;
import org.sagebionetworks.bridge.sdk.models.subpopulations.StudyConsent;
import org.sagebionetworks.bridge.sdk.models.subpopulations.Subpopulation;
import org.sagebionetworks.bridge.sdk.models.subpopulations.SubpopulationGuid;

public class StudyConsentTest {

    private TestUser admin;
    private TestUser developer;
    private SubpopulationGuid subpopGuid;

    @Before
    public void before() {
        admin = TestUserHelper.getSignedInAdmin();
        developer = TestUserHelper.createAndSignInUser(StudyConsentTest.class, true, Roles.DEVELOPER);
    }

    @After
    public void after() {
        if (subpopGuid != null) {
            admin.getSession().getSubpopulationClient().deleteSubpopulationPermanently(subpopGuid);    
        }
        if (developer != null) {
            developer.signOutAndDeleteUser();    
        }
    }

    @Test(expected=BridgeSDKException.class)
    public void cannotBeAccessedByRegularUser() {
        TestUser user = TestUserHelper.createAndSignInUser(StudyConsentTest.class, true);
        try {
            StudyConsent consent = new StudyConsent();
            consent.setDocumentContent("<p>Test content.</p>");

            user.getSession().getStudyConsentClient().createStudyConsent(
                    user.getDefaultSubpopulation(), consent);
        } finally {
            user.signOutAndDeleteUser();
        }
    }

    @Test(expected=BridgeSDKException.class)
    public void cannotBeAccessedByResearcher() {
        TestUser researcher = TestUserHelper.createAndSignInUser(StudyConsentTest.class, true, Roles.RESEARCHER);
        try {
            StudyConsent consent = new StudyConsent();
            consent.setDocumentContent("<p>Test content.</p>");

            researcher.getSession().getStudyConsentClient().createStudyConsent(
                    researcher.getDefaultSubpopulation(), consent);
        } finally {
            researcher.signOutAndDeleteUser();
        }
    }
    
    @Test
    public void addAndActivateConsent() {
        StudyConsentClient studyConsentClient = developer.getSession().getStudyConsentClient();
        SubpopulationClient subpopClient = developer.getSession().getSubpopulationClient();

        // Create a subpopulation to test this so we can delete the subpopulation to clean up.
        // Because we create it from scratch, we know the exact number of consents that are in it.
        // It is not required, so shouldn't prevent other tests from creating users.
        Subpopulation subpop = new Subpopulation();
        subpop.setName(Tests.randomIdentifier(StudyConsentTest.class));
        subpop.setRequired(false);
        GuidVersionHolder holder = subpopClient.createSubpopulation(subpop);
        subpop.setHolder(holder);
        subpopGuid = new SubpopulationGuid(holder.getGuid());
        
        StudyConsent consent = new StudyConsent();
        consent.setDocumentContent("<p>Test content</p>");
        studyConsentClient.createStudyConsent(subpopGuid, consent);

        ResourceList<StudyConsent> studyConsents = studyConsentClient.getAllStudyConsents(subpopGuid);

        assertEquals(2, studyConsents.getTotal());

        StudyConsent current = studyConsentClient.getStudyConsent(subpopGuid, studyConsents.getItems().get(0).getCreatedOn());
        assertEquals(consent.getDocumentContent(), current.getDocumentContent());
        assertNotNull(current.getCreatedOn());

        studyConsentClient.publishStudyConsent(subpopGuid, current.getCreatedOn());

        StudyConsent published = studyConsentClient.getPublishedStudyConsent(subpopGuid);
        assertTrue("Published consent is returned.", published.isActive());
        
        studyConsentClient.createStudyConsent(subpopGuid, current);
        
        StudyConsent newOne = studyConsentClient.getMostRecentStudyConsent(subpopGuid);
        assertTrue(newOne.getCreatedOn().isAfter(published.getCreatedOn()));
        
        ResourceList<StudyConsent> studyConsents2 = studyConsentClient.getAllStudyConsents(subpopGuid);
        assertEquals(3, studyConsents2.getTotal());
    }

}
