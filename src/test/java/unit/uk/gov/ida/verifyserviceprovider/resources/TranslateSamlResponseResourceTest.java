package unit.uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sun.jdi.connect.Connector;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.Attributes;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.*;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.ResponseBuilder.aResponse;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory.createStringToResponseTransformer;

public class TranslateSamlResponseResourceTest {

    private static final ResponseService responseService = mock(ResponseService.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new TranslateSamlResponseResource(responseService))
        .build();

    @Test
    public void shouldUseResponseServiceToTranslateSaml() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response");

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        verify(responseService, times(1)).convertTranslatedResponseBody(translateResponseRequest.getString("samlResponse"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void translateAuthnResponseShouldReturn400WhenUnknownLevel() {
    }

    @Test
    public void translateAuthenticationResponseShouldReturn500WhenInternalServerError() {
    }

}
