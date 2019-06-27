package uk.gov.ida.verifyserviceprovider.compliance;

import io.dropwizard.jersey.validation.ValidationErrorMessage;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDatasetBuilder;

import javax.ws.rs.core.Response;
import java.security.cert.CertificateEncodingException;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefreshDatasetResourceTest {

    ComplianceToolClient complianceToolClient = mock(ComplianceToolClient.class);

    @Rule
    public ResourceTestRule refreshDatasetResource = ResourceTestRule.builder()
            .addResource(new RefreshDatasetResource(complianceToolClient))
            .build();

    @Test
    public void itWillErrorIfTheMatchingDatasetIsNotValid() {
        Response response = refreshDatasetResource.client()
            .target("/refresh-matching-dataset")
            .request()
            .post(json("{}"));
        assertThat(response.getStatus()).isEqualTo(422);
        ValidationErrorMessage errorMessage = response.readEntity(ValidationErrorMessage.class);
        assertThat(errorMessage.getErrors()).isNotEmpty();
        assertThat(errorMessage.getErrors()).contains("firstName may not be null");
    }

    @Test
    public void willCallOnTheComplianceToolIfTheMatchingDatasetIsValid() throws CertificateEncodingException {
        when(complianceToolClient.initializeComplianceTool(any(MatchingDataset.class))).thenReturn(Response.ok().build());
        Response response = refreshDatasetResource.client()
            .target("/refresh-matching-dataset")
            .request()
            .post(json(new MatchingDatasetBuilder().build()));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(complianceToolClient).initializeComplianceTool(any(MatchingDataset.class));
    }

    @Test
    public void willAlsoLetYouUseRefreshIdentityDataset() throws CertificateEncodingException {
        when(complianceToolClient.initializeComplianceTool(any(MatchingDataset.class))).thenReturn(Response.ok().build());
        Response response = refreshDatasetResource.client()
            .target("/refresh-identity-dataset")
            .request()
            .post(json(new MatchingDatasetBuilder().build()));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(complianceToolClient).initializeComplianceTool(any(MatchingDataset.class));
    }

}