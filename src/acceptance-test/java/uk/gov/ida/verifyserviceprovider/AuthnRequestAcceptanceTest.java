package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthnRequestAcceptanceTest {

    private static String COMPLIANCE_TOOL_HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            resourceFilePath("verify-service-provider.yml"),
            ConfigOverride.config("hubSsoLocation", String.format("%s/%s", COMPLIANCE_TOOL_HOST, "SAML2/SSO"))
    );

    private static Client client = application.client();

    @Before
    public void setupComplianceTool() throws Exception {
        ComplianceToolServiceTestData data = new ComplianceToolServiceTestData(
                "http://verify-service-provider-acceptance-test",
                "http://verify-service-provider-acceptance-test/response",
                // certificate lasting for 100 years CN=My Application, O=My Organisation, L=My City, C=DE
                "MIIDIjCCAgqgAwIBAgIESSrg2jANBgkqhkiG9w0BAQsFADBSMQswCQYDVQQGEwJERTEQMA4GA1UEBxMHTXkgQ2l0eTEYMBYGA1UEChMPTXkgT3JnYW5pc2F0aW9uMRcwFQYDVQQDEw5NeSBBcHBsaWNhdGlvbjAgFw0xNzA3MjQxMzM3MTdaGA8yMTE3MDYzMDEzMzcxN1owUjELMAkGA1UEBhMCREUxEDAOBgNVBAcTB015IENpdHkxGDAWBgNVBAoTD015IE9yZ2FuaXNhdGlvbjEXMBUGA1UEAxMOTXkgQXBwbGljYXRpb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCHsxejk7eW9NucMiEFWCy+gtw4HGf/8NotDKRo0zzWeq7VImb2B4bWdmUH12wV5fhD7UXPfSwCdOH5LDJ84O232oKsuB5oosxRA7zq7kUJTI6ZiNRYJMbFR3oYnhXgibNGzYAqYrum1A9fiF3HydvpEkgdi9ElJSUx3bIQIKJLK1KP3k/+4q4IPkmPWmgnZ81/tJzh9v8WqMJ5Dnb7txVFXcaMAHYRP/MG/ycyuZrYiWHAJoljG/nAjkXlaYdxLwvLXhNUCyHvtOvvOIEgNDYHGjT9UtvmDloc3Z8Gno5zYqvtqh4UEg60U2MOMx+vzVuhRTfdngWyfOwKwoxfQiMjAgMBAAEwDQYJKoZIhvcNAQELBQADggEBADvsKfulEN0APMu7HN9SKeEcNTvIkPdvk1TDnOTGQJIztOvFSiHrRBBcgQduA9bZRwnPow/d5azKiFMCWNwW7DqRm9dloBx80Fo/rwiYk2F8ELFIHMnW6XGeeenAqaLgWyhWkAYb9NXbePHmY2gHplNN7rhFZCDgbMEwuEbN+R3/D/FdEtVcs8J1QbH/tRI6Zsk30trAdc8DNnA/WclK7PirhA+Pdpez5r6mFNMBQKDG6xEzNIkZ4/EtkQygZ9Lv55sbHbQumsSfF4TtsNzj/RyPMyvd9pEMwWxEHT0kJ1tNURclOSpqq4fhw+O2X34tOZZjc9QgtxR46dQFmUOtmZo=",
                "MIIDIjCCAgqgAwIBAgIEWceUbjANBgkqhkiG9w0BAQsFADBSMQswCQYDVQQGEwJERTEQMA4GA1UEBxMHTXkgQ2l0eTEYMBYGA1UEChMPTXkgT3JnYW5pc2F0aW9uMRcwFQYDVQQDEw5NeSBBcHBsaWNhdGlvbjAgFw0xNzA3MjQxMzM3MThaGA8yMTE3MDYzMDEzMzcxOFowUjELMAkGA1UEBhMCREUxEDAOBgNVBAcTB015IENpdHkxGDAWBgNVBAoTD015IE9yZ2FuaXNhdGlvbjEXMBUGA1UEAxMOTXkgQXBwbGljYXRpb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCtiX7tnyvlsQK8Q8k4EU7dnXYCYY4vglpAbBI5YpMemNrOQnMzBClogasyKK/hUfn2BIQYBm8qBv3gH2vylRYuHSTw2TuypPRWBaetPV1R6fo3c2PURy/c9aBNtgF750WkEStrzSIug9GtY5WifYf+lMf1Tc/S0bVWBnj/MSXf+EnZCt8+uSeKemJx9FdMEwxXVvopIECgMZcLwtgEav6JOww+Bj2oCUWKlkJZvvoUlQWaYBAuDG7WCh7K/SVPnEHN6/f+fJHOMl5mg0rQuidWoQdNVTfbzKDeUdU0nChrqam64oa3h76K/874iTfQ98K1zJvSFFgfH/deH/cT0J5NAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAKvSYhva27JdC+akcXSiun10rJbNuLf+PfrtUv2JT9OeL3PX7ejjK8A8J5WTX8E3icVq+kDat8eU+Eo4ujh5q/vNtRIgHgpRoLNJZ6NHNn++WPLtyhH0wYcbYnGYwu0jfdOLMKA52hYw0yiTjfcyt5OH7/cuGl5q1lylk9IjKsSbIH6L52wbbYBFg34Y/wjkN6ghgX9zFmLXD+Cz5gPPjHUt3tIN4PT30WSaEcfxXAVTulnPo/hZyO5TBWfhQ+0tlOzJ0Ubs7SyG3XuAEBiNc9w3PmMQfYfTP3NS51v87sKOTvn3DqG6+ZtQDlt/2yiDyR1pwT3P556zYR8+OTOg0Io=",
                "verify-service-provider-pid",
                "http://verify-service-provider-acceptance-test/msa",
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCHsxejk7eW9NucMiEFWCy+gtw4HGf/8NotDKRo0zzWeq7VImb2B4bWdmUH12wV5fhD7UXPfSwCdOH5LDJ84O232oKsuB5oosxRA7zq7kUJTI6ZiNRYJMbFR3oYnhXgibNGzYAqYrum1A9fiF3HydvpEkgdi9ElJSUx3bIQIKJLK1KP3k/+4q4IPkmPWmgnZ81/tJzh9v8WqMJ5Dnb7txVFXcaMAHYRP/MG/ycyuZrYiWHAJoljG/nAjkXlaYdxLwvLXhNUCyHvtOvvOIEgNDYHGjT9UtvmDloc3Z8Gno5zYqvtqh4UEg60U2MOMx+vzVuhRTfdngWyfOwKwoxfQiMjAgMBAAECggEBAIEsURdMORoAscBo05gTzFv5k4nyOGmhKv5xJ1wFoMcF98E5Q0t19LvX7epd/SyHQiHfDnIp1CfufWFE2jaXopI99eBWT9QJDHceGMqFz7+/WYr7fi79qx0GIkqmyWp5ieefHR6756cx/ARTefoBxE2EMpO1kXxLdLGYOLUza2ojQ57bygNXLyRZgqoDIdUK1gFiWZJefpUfwOpWkNTk9NkAkvGHWt/T30zN9bmX52zFFP8SKnBOdka+lk8iFHrwpd/8v1ass51WhNAjDafnBE2wMYQQTtIuaEhmZ7f8juEOC1NizXHkwMq9uXx3cAdXbVjs+rGsgjAwfD9BVAMvi0ECgYEA5YaKtC+O4pGxFVhG8ZsabvscmeVVkcHS7jVHU7Lwj/2NEZE2B7w5pdEJNWjNXRXqGUNUTwrapEIewyFcLGz1tbbBWAnve28e55J7WwI36wCmXD8v+LpuPfl9ZJxXO2ZOxhvWMlvh8pAaDzConc/W5/dU3V3sQUH99pAb96+vs6ECgYEAl1oLvVTMcPgq8cOiwnvJa1pav/w3QZCB+h+tTib1f8cgTcIuWMA0z4uDu3fBPXU6JvC//EwQYE9cNzdtDldxhdfeccwZoVka7jAmdYuu14DVtAPPqet2HL4AyIDKICofewGtxVDLR13EEmJegtkqVIZOcPeYYAj16SxybXySIEMCgYEAhni4srBaSiuJUDQT/GEer363VwKhi2+/IIhebY8cmX+3Ml+dBBMmwxshBgWMq8i3Cm4D6vs09Z83XqMg2XZMzlVwGSBZCjwkIxAS7VLzZ99NmCX8+QBgrjaJXHSsNsTNygttBrwGOJJschHT+AFYqzagpcDtNZ5wKBBuEkL/8YECgYBF1Y97IYhfS7KM8ObFc9ZhCUS8NsTMJMBER59wYvt9pMRb/I/j9XOom8gBlOT91XwqgYUkBXi854E2HQXdyy0fQ5ZozXK6BuItKtxj+jqHRvPT5rpHvdQ2uNilqv8YTjdOS10BoSDaYgJZNThEia1FaN8CssuE7D2DBDYcHJFT/wKBgBwmn7fkoSzrDypaaZzdK+BMPc/YLnIVgF7DF2zRv/laLr5FChf0n1Sdi5LrdPjUT0YmnFCZODhj3UV9PU9AoKX1ePAaxLs5X8pMQfeqW9lWbYWW2cE4on7QlPcKtJluBGboKAyvdI69t2sS+FoK2OiPqcWZLd3zBETYmJ/Hh95A",
                Collections.EMPTY_LIST
        );

        Response complianceToolResponse = client
                .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
                .request()
                .buildPost(Entity.json(data))
                .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }

    @Test
    @Ignore
    public void shouldGenerateValidAuthnRequest() throws Exception {
        Response authnResponse = client
                .target(URI.create(String.format("http://localhost:%d/generate-request", application.getLocalPort())))
                .request()
                .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2)))
                .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);

        Response complianceToolResponse = client
                .target(authnSaml.getLocation())
                .request()
                .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
                .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");
    }

}
