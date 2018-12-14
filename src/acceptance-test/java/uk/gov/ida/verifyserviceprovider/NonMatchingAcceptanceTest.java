package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.domain.MatchingAddress;
import uk.gov.ida.verifyserviceprovider.domain.MatchingAttribute;
import uk.gov.ida.verifyserviceprovider.domain.V2MatchingDataset;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.builders.VerifyServiceProviderAppRuleBuilder.aVerifyServiceProviderAppRule;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;
import static uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID;

public class NonMatchingAcceptanceTest {

    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();

    @ClassRule
    public static VerifyServiceProviderAppRule application = aVerifyServiceProviderAppRule()
            .withMockMsaServer(msaServer)
            .build();

    private static Client client;
    private static ComplianceToolService complianceTool;
    private static GenerateRequestService generateRequestService;

    @BeforeClass
    public static void setUpBeforeClass() {
        client = application.client();
        complianceTool = new ComplianceToolService(client);
        generateRequestService = new GenerateRequestService(client);
    }

    @Test
    public void shouldRespondWithIdentityVerifiedWhenVerificationSucceeds() {

        complianceTool.initialiseWithDefaultsForV2();

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_1.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-non-matching-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        assertThat(jsonResponse.getString("scenario")).isEqualTo(IDENTITY_VERIFIED.name());
        assertThat(jsonResponse.getString("levelOfAssurance")).isEqualTo(LEVEL_1.name());
    }

    @Test
    public void shouldCorrectlyReproduceMatchingDatasetInResponse() {
        String expectedPid = "some-expected-pid";

        String standardFromDateString = "2013-02-22T14:32:14.064";
        String standardToDateString = "2015-10-02T09:32:14.967";
        String laterFromDateString = "2015-10-02T09:32:14.967";
        String laterToDateString = "2018-03-03T10:20:50.163";
        LocalDateTime standardFromDate = LocalDateTime.parse(standardFromDateString);
        LocalDateTime standardToDate = LocalDateTime.parse(standardToDateString);
        LocalDateTime laterFromDate = LocalDateTime.parse(laterFromDateString);
        LocalDateTime laterToDate = LocalDateTime.parse(laterToDateString);

        V2MatchingDataset matchingDataset = new V2MatchingDataset(
            new MatchingAttribute("Bob", true, standardFromDate, standardToDate),
            new MatchingAttribute("Montgomery", true, standardFromDate, standardToDate),
            Arrays.asList(new MatchingAttribute("Smith", true, standardFromDate, standardToDate), new MatchingAttribute("Smythington", true, laterFromDate, laterToDate)),
            new MatchingAttribute("NOT_SPECIFIED", true, standardFromDate, standardToDate),
            new MatchingAttribute("1970-01-01", true, standardFromDate, standardToDate),
            singletonList(new MatchingAddress(true, standardFromDate, standardToDate, "E1 8QS", Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), null, null)),
            expectedPid
        );

        complianceTool.initialiseWithMatchingDatasetForV2(matchingDataset);

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_1.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-non-matching-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));

        JSONObject attributes = jsonResponse.getJSONObject("attributes");
        assertThat(attributes.keys()).containsExactlyInAnyOrder("firstName", "middleNames", "surnames", "dateOfBirth", "gender", "addresses");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedFromDateString = standardFromDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");
        String expectedToDateString = standardToDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");
        String expectedLaterFromDateString = laterFromDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");
        String expectedLaterToDateString = laterToDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");

        checkMdsValueOfAttribute("firstName", "Bob", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueInArrayAttribute("middleNames", 0, "Montgomery", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueInArrayAttribute("surnames", 0, "Smith", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueInArrayAttribute("surnames", 1, "Smythington", true, expectedLaterFromDateString, expectedLaterToDateString, attributes);
        checkMdsValueOfAttribute("dateOfBirth", "1970-01-01", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueOfAttribute("gender", "NOT_SPECIFIED", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueOfAddress(0, Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), "E1 8QS", "", true, expectedFromDateString, expectedToDateString, attributes);
    }

    @Test
    public void shouldCorrectlyHandleEmptyValuesInMatchingDataset() {
        String expectedPid = "some-expected-pid";

        String standardFromDateString = "2013-02-22T14:32:14.064";
        String standardToDateString = "2018-10-02T09:32:14.967";
        LocalDateTime standardFromDate = LocalDateTime.parse(standardFromDateString);
        LocalDateTime standardToDate = LocalDateTime.parse(standardToDateString);


        V2MatchingDataset matchingDataset = new V2MatchingDataset(
                new MatchingAttribute("Bob", true, standardFromDate, standardToDate),
                null,
                singletonList(new MatchingAttribute("Smith", true, standardFromDate, null)),
                new MatchingAttribute("NOT_SPECIFIED", true, standardFromDate, standardToDate),
                null,
                singletonList(new MatchingAddress(true, standardFromDate, standardToDate, "E1 8QS", Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), null, null)),
                expectedPid
        );

        complianceTool.initialiseWithMatchingDatasetForV2(matchingDataset);

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_1.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-non-matching-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        assertThat(jsonResponse.getString("scenario")).isEqualTo(IDENTITY_VERIFIED.name());
        assertThat(jsonResponse.getString("levelOfAssurance")).isEqualTo(LEVEL_1.name());

        JSONObject attributes = jsonResponse.getJSONObject("attributes");
        assertThat(attributes.keys()).containsExactlyInAnyOrder("firstName", "middleNames", "surnames", "gender", "addresses");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedFromDateString = standardFromDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");
        String expectedToDateString = standardToDate.toLocalDate().atStartOfDay().format(formatter).replace(" ", "T");

        checkMdsValueOfAttribute("firstName", "Bob", true, expectedFromDateString, expectedToDateString, attributes);
        assertThat(attributes.getJSONArray("middleNames").length()).isEqualTo(0);
        checkMdsValueInArrayAttribute("surnames", 0, "Smith", true, expectedFromDateString, null, attributes);
        assertThat(attributes.isNull("dateOfBirth")).isTrue();
        checkMdsValueOfAttribute("gender", "NOT_SPECIFIED", true, expectedFromDateString, expectedToDateString, attributes);
        checkMdsValueOfAddress(0, Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), "E1 8QS", "", true, expectedFromDateString, expectedToDateString, attributes);
    }

    @Test
    public void shouldThrowExceptionIfLoAReturnedByIdpIsTooLow () {
        String expectedPid = "some-expected-pid";

        complianceTool.initialiseWithPidForV2(expectedPid);

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
                "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID),
                "requestId", requestResponseBody.getRequestId(),
                "levelOfAssurance", LEVEL_2.name()
        );

        Response response = client
                .target(String.format("http://localhost:%d/translate-non-matching-response", application.getLocalPort()))
                .request()
                .buildPost(json(translateResponseRequestData))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo("Expected Level of Assurance to be at least LEVEL_2, but was LEVEL_1");
    }

    private void checkMdsValueOfAttribute(
        String attributeName,
        String expectedValue,
        boolean expectedIsVerified,
        String expectedFromDateString,
        String expectedToDateString,
        JSONObject attributes
    ) {
        JSONObject attribute = attributes.getJSONObject(attributeName);
        checkMdsValueInJsonObject(attribute, expectedValue, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    private void checkMdsValueOfAddress(
        int index,
        List<String> lines,
        String postcode,
        String internationalPostcode,
        boolean expectedIsVerified,
        String expectedFromDateString,
        String expectedToDateString,
        JSONObject attributes
    ) {
        JSONArray addresses = attributes.getJSONArray("addresses");
        JSONObject addressMdsValue = addresses.getJSONObject(index);
        JSONObject addressValue = addressMdsValue.getJSONObject("value");

        // TODO: The Compliance Tool isn't currently returning from/to dates for addresses.  Until that is fixed, the next line needs to be commented out.
        //checkMdsMetadataInJsonObject(addressMdsValue, expectedIsVerified, expectedFromDateString, expectedToDateString);

        JSONArray jsonLines = addressValue.getJSONArray("lines");
        assertThat(jsonLines.length()).isEqualTo(lines.size());
        for (index = 0; index < lines.size(); index ++) {
            assertThat(jsonLines.getString(index)).isEqualTo(lines.get(index));
        }
        assertThat(addressValue.getString("postCode")).isEqualTo(postcode);
        assertThat(addressValue.getString("internationalPostCode")).isEqualTo(internationalPostcode);
    }

    private void checkMdsValueInArrayAttribute(
        String attributeName,
        int index,
        String expectedValue,
        boolean expectedIsVerified,
        String expectedFromDateString,
        String expectedToDateString,
        JSONObject attributes
    ) {
        JSONArray jsonArray = attributes.getJSONArray(attributeName);
        JSONObject mdsObjectJson = jsonArray.getJSONObject(index);
        checkMdsValueInJsonObject(mdsObjectJson, expectedValue, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    private void checkMdsValueInJsonObject(
        JSONObject jsonObject,
        String expectedValue,
        boolean expectedIsVerified,
        String expectedFromDateString,
        String expectedToDateString
    ) {
        assertThat(jsonObject.getString("value")).isEqualTo(expectedValue);
        checkMdsMetadataInJsonObject(jsonObject, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    private void checkMdsMetadataInJsonObject(
        JSONObject jsonObject,
        boolean expectedIsVerified,
        String expectedFromDateString,
        String expectedToDateString
    ) {
        assertThat(jsonObject.getBoolean("verified")).isEqualTo(expectedIsVerified);
        assertThat(jsonObject.getString("from")).isEqualTo(expectedFromDateString);
        if (expectedToDateString != null) {
            assertThat(jsonObject.getString("to")).isEqualTo(expectedToDateString);
        }
    }
}
