package unit.uk.gov.ida.verifyserviceprovider.utils;

import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.utils.ServerDetailFinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerDetailFinderTest {

    @Test
    public void shouldSetAllPortsCorrectlyForStandardConfig() {
        ServerConnector applicationHttp = mock(ServerConnector.class);
        when(applicationHttp.getName()).thenReturn(ServerDetailFinder.DROPWIZARD_CONNECTOR_APPLICATION);
        when(applicationHttp.getLocalPort()).thenReturn(12345);
        when(applicationHttp.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_HTTP);

        ServerConnector applicationSsl = mock(ServerConnector.class);
        when(applicationSsl.getName()).thenReturn(ServerDetailFinder.DROPWIZARD_CONNECTOR_APPLICATION);
        when(applicationSsl.getLocalPort()).thenReturn(54321);
        when(applicationSsl.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_SSL);

        ServerConnector adminHttp = mock(ServerConnector.class);
        when(adminHttp.getName()).thenReturn(ServerDetailFinder.DROPWIZARD_CONNECTOR_ADMIN);
        when(adminHttp.getLocalPort()).thenReturn(12121);
        when(adminHttp.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_HTTP);

        ServerConnector adminSsl = mock(ServerConnector.class);
        when(adminSsl.getName()).thenReturn(ServerDetailFinder.DROPWIZARD_CONNECTOR_ADMIN);
        when(adminSsl.getLocalPort()).thenReturn(21212);
        when(adminSsl.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_SSL);

        Server mockServer = mock(Server.class);
        ServerConnector[] serverConnectors = {applicationHttp, applicationSsl, adminHttp, adminSsl};
        when(mockServer.getConnectors()).thenReturn(serverConnectors);

        Environment environment = createMockEnvironment();

        ServerDetailFinder.ServerDetail actualResult = ServerDetailFinder.fetchStandardServerDetails(environment, mockServer);

        assertThat(actualResult.getApplicationName()).isEqualTo(environment.getName());
        assertThat(actualResult.getAdminPath()).isEqualTo(environment.getAdminContext().getContextPath());
        assertThat(actualResult.getServerHttpPort()).isEqualTo(applicationHttp.getLocalPort());
        assertThat(actualResult.getServerHttpsPort()).isEqualTo(applicationSsl.getLocalPort());
        assertThat(actualResult.getAdminHttpPort()).isEqualTo(adminHttp.getLocalPort());
        assertThat(actualResult.getAdminHttpsPort()).isEqualTo(adminSsl.getLocalPort());
    }

    @Test
    public void shouldNotSetUnknownConnectorTypeForStandardConfig() {
        ServerConnector unknownConnectorType = mock(ServerConnector.class);
        when(unknownConnectorType.getName()).thenReturn("Unknown");
        when(unknownConnectorType.getLocalPort()).thenReturn(666);
        when(unknownConnectorType.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_SSL);

        Environment environment = createMockEnvironment();

        Server mockServer = mock(Server.class);

        ServerConnector[] serverConnectors = {unknownConnectorType};
        when(mockServer.getConnectors()).thenReturn(serverConnectors);

        ServerDetailFinder.ServerDetail actualResult = ServerDetailFinder.fetchStandardServerDetails(environment, mockServer);

        assertThat(actualResult.getServerHttpPort()).isNull();
        assertThat(actualResult.getServerHttpsPort()).isNull();
        assertThat(actualResult.getAdminHttpPort()).isNull();
        assertThat(actualResult.getAdminHttpsPort()).isNull();
    }

    @Test
    public void shouldSetOnlyHttpPortsForSimpleConfig() {
        ServerConnector serverConnector = mock(ServerConnector.class);
        when(serverConnector.getLocalPort()).thenReturn(55555);

        Environment environment = createMockEnvironment();

        Server mockServer = mock(Server.class);

        ServerConnector[] serverConnectors = {serverConnector};
        when(mockServer.getConnectors()).thenReturn(serverConnectors);

        ServerDetailFinder.ServerDetail actualResult = ServerDetailFinder.fetchSimpleServerDetail(environment, mockServer);

        assertThat(actualResult.getServerHttpsPort()).isNull();
        assertThat(actualResult.getAdminHttpsPort()).isNull();
        assertThat(actualResult.getServerHttpPort()).isEqualTo(serverConnector.getLocalPort());
        assertThat(actualResult.getAdminHttpPort()).isEqualTo(serverConnector.getLocalPort());
    }

    @Test
    public void shouldGenerateBaseUrlCorrectly() {

        Environment environment = createMockEnvironment();

        ServerDetailFinder.ServerDetail serverDetail =
                new ServerDetailFinder.ServerDetail(environment.getName(), environment.getAdminContext().getContextPath(),
                        11111, 11112, 11113, 11114);

        assertThat(serverDetail.generateApplicationBaseUrl(true)).isEqualTo("https://localhost:11112");
        assertThat(serverDetail.generateApplicationBaseUrl(false)).isEqualTo("http://localhost:11111");
        assertThat(serverDetail.generateAdminBaseUrl(true)).isEqualTo("https://localhost:11114");
        assertThat(serverDetail.generateAdminBaseUrl(false)).isEqualTo("http://localhost:11113");
        assertThat(serverDetail.generateAdminUrl(true)).isEqualTo("https://localhost:11114/test-admin-path?pretty=true");
        assertThat(serverDetail.generateAdminUrl(false)).isEqualTo("http://localhost:11113/test-admin-path?pretty=true");
        assertThat(serverDetail.generateHealthcheckUrl(true)).isEqualTo("https://localhost:11114/test-admin-path/healthcheck?pretty=true");
        assertThat(serverDetail.generateHealthcheckUrl(false)).isEqualTo("http://localhost:11113/test-admin-path/healthcheck?pretty=true");
    }

    private Environment createMockEnvironment() {
        Environment environment = mock(Environment.class);
        when(environment.getName()).thenReturn("test-app");

        MutableServletContextHandler adminContext = mock(MutableServletContextHandler.class);
        when(adminContext.getContextPath()).thenReturn("/test-admin-path");

        when(environment.getAdminContext()).thenReturn(adminContext);
        return environment;
    }
}
