package uk.gov.ida.verifyserviceprovider.utils;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

        ServerDetailFinder.ServerDetail actualResult = new ServerDetailFinder.ServerDetail("test", "test-admin-path");
        ServerDetailFinder.fetchStandardServerDetails(actualResult, mockServer);

        assertThat(actualResult.serverHttpPort).isEqualTo(applicationHttp.getLocalPort());
        assertThat(actualResult.serverHttpsPort).isEqualTo(applicationSsl.getLocalPort());
        assertThat(actualResult.adminHttpPort).isEqualTo(adminHttp.getLocalPort());
        assertThat(actualResult.adminHttpsPort).isEqualTo(adminSsl.getLocalPort());
    }

    @Test
    public void shouldNotSetUnknownConnectorTypeForStandardConfig() {
        ServerConnector unknownConnectorType = mock(ServerConnector.class);
        when(unknownConnectorType.getName()).thenReturn("Unknown");
        when(unknownConnectorType.getLocalPort()).thenReturn(666);
        when(unknownConnectorType.getDefaultProtocol()).thenReturn(ServerDetailFinder.DROPWIZARD_PROTOCOL_SSL);

        Server mockServer = mock(Server.class);

        ServerConnector[] serverConnectors = {unknownConnectorType};
        when(mockServer.getConnectors()).thenReturn(serverConnectors);

        ServerDetailFinder.ServerDetail actualResult = new ServerDetailFinder.ServerDetail("test", "test-admin-path");
        ServerDetailFinder.fetchStandardServerDetails(actualResult, mockServer);

        assertThat(actualResult.serverHttpPort).isNull();
        assertThat(actualResult.serverHttpsPort).isNull();
        assertThat(actualResult.adminHttpPort).isNull();
        assertThat(actualResult.adminHttpsPort).isNull();
    }

    @Test
    public void shouldSetOnlyHttpPortsForSimpleConfig() {
        ServerConnector serverConnector = mock(ServerConnector.class);
        when(serverConnector.getLocalPort()).thenReturn(55555);

        Server mockServer = mock(Server.class);

        ServerConnector[] serverConnectors = {serverConnector};
        when(mockServer.getConnectors()).thenReturn(serverConnectors);

        ServerDetailFinder.ServerDetail actualResult = new ServerDetailFinder.ServerDetail("test", "test-admin-path");
        ServerDetailFinder.fetchSimpleServerDetail(actualResult, mockServer);

        assertThat(actualResult.serverHttpsPort).isNull();
        assertThat(actualResult.adminHttpsPort).isNull();
        assertThat(actualResult.serverHttpPort).isEqualTo(serverConnector.getLocalPort());
        assertThat(actualResult.adminHttpPort).isEqualTo(serverConnector.getLocalPort());
    }

    @Test
    public void shouldGenerateBaseUrlCorrectly() {

        ServerDetailFinder.ServerDetail serverDetail = new ServerDetailFinder.ServerDetail("test-app", "test-admin-path");
        serverDetail.serverHttpPort = 11111;
        serverDetail.serverHttpsPort = 11112;
        serverDetail.adminHttpPort = 11113;
        serverDetail.adminHttpsPort = 11114;

        assertThat(serverDetail.generateApplicationBaseUrl(true)).isEqualTo("https://localhost:11112");
        assertThat(serverDetail.generateApplicationBaseUrl(false)).isEqualTo("http://localhost:11111");
        assertThat(serverDetail.generateAdminBaseUrl(true)).isEqualTo("https://localhost:11114");
        assertThat(serverDetail.generateAdminBaseUrl(false)).isEqualTo("http://localhost:11113");
        assertThat(serverDetail.generateAdminUrl(true)).isEqualTo("https://localhost:11114/test-admin-path?pretty=true");
        assertThat(serverDetail.generateAdminUrl(false)).isEqualTo("http://localhost:11113/test-admin-path?pretty=true");
        assertThat(serverDetail.generateHealthcheckUrl(true)).isEqualTo("https://localhost:11114/test-admin-path/healthcheck?pretty=true");
        assertThat(serverDetail.generateHealthcheckUrl(false)).isEqualTo("http://localhost:11113/test-admin-path/healthcheck?pretty=true");
    }
}
