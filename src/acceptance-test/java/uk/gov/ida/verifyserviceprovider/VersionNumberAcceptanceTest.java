package uk.gov.ida.verifyserviceprovider;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class VersionNumberAcceptanceTest {
    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();

    @ClassRule
    public static VerifyServiceProviderAppRule application = new VerifyServiceProviderAppRule(msaServer);

    private static Client client;

    @BeforeClass
    public static void setUpBeforeClass() {
        client = application.client();
    }

    @Test
    public void shouldRespondWithVersionNumber() {
        Response response = client
                .target(String.format("http://localhost:%d/version-number", application.getLocalPort()))
                .request()
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        //tests run /out directory instead of jar file hence can't read actual version from META-INF
        assertThat(response.readEntity(String.class)).isEqualTo("UNKNOWN_VERSION");
    }
}
