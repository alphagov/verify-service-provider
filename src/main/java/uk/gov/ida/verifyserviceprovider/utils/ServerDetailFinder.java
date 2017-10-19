package uk.gov.ida.verifyserviceprovider.utils;

import io.dropwizard.Configuration;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility that looks up various server details so that useful URLs can be logged to the log file.
 */
public class ServerDetailFinder {

    public static final String DROPWIZARD_CONNECTOR_APPLICATION = "application";

    public static final String DROPWIZARD_CONNECTOR_ADMIN = "admin";

    public static final String DROPWIZARD_PROTOCOL_SSL = "SSL";

    public static final String DROPWIZARD_PROTOCOL_HTTP = "HTTP";

    public static ServerDetail fetchServerDetails(Server server, Configuration config, Environment environment) {

        if (config.getServerFactory() instanceof SimpleServerFactory) {
            return fetchSimpleServerDetail(environment, server);
        } else {
            return fetchStandardServerDetails(environment, server);
        }
    }

    public static ServerDetail fetchStandardServerDetails(Environment environment, Server server) {

        Map<String, Integer> adminConnectorDetails = extractPortDetailsByConnectorType(server, DROPWIZARD_CONNECTOR_ADMIN);
        Map<String, Integer> appConnectorDetails = extractPortDetailsByConnectorType(server, DROPWIZARD_CONNECTOR_APPLICATION);

        return new ServerDetail(
            environment.getName(),
            environment.getAdminContext().getContextPath(),
            appConnectorDetails.get(DROPWIZARD_PROTOCOL_HTTP),
            appConnectorDetails.get(DROPWIZARD_PROTOCOL_SSL),
            adminConnectorDetails.get(DROPWIZARD_PROTOCOL_HTTP),
            adminConnectorDetails.get(DROPWIZARD_PROTOCOL_SSL));
    }

    public static ServerDetail fetchSimpleServerDetail(Environment environment, Server server) {
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                ServerConnector serverConnector = (ServerConnector) connector;
                return new ServerDetail(
                    environment.getName(),
                    environment.getAdminContext().getContextPath(),
                    serverConnector.getLocalPort(),
                    null,
                    serverConnector.getLocalPort(),
                    null);
            }
        }
        return new ServerDetail(environment.getName(), environment.getAdminContext().getContextPath(), null, null, null, null);
    }

    private static Map<String, Integer> extractPortDetailsByConnectorType(Server server, String connectorType) {
        return Arrays.stream(server.getConnectors())
            .filter(connector -> {
                ServerConnector serverConnector = (ServerConnector) connector;

                return serverConnector.getName().equals(connectorType) &&
                    (serverConnector.getDefaultProtocol().equals(DROPWIZARD_PROTOCOL_SSL) ||
                        serverConnector.getDefaultProtocol().contains(DROPWIZARD_PROTOCOL_HTTP));
            })
            .map(connector -> (ServerConnector) connector)
            .collect(Collectors.toMap(ServerConnector::getDefaultProtocol, ServerConnector::getLocalPort));
    }

    public static class ServerDetail {

        private static final String LOCAL_HOST = "localhost";

        private static final String HTTP_LOCAL_HOST_BASE_URL = "http://" + LOCAL_HOST;

        private static final String HTTPS_LOCAL_HOST_BASE_URL = "https://" + LOCAL_HOST;

        private final String applicationName;

        private final String adminPath;

        private final Integer serverHttpPort;

        private final Integer serverHttpsPort;

        private final Integer adminHttpPort;

        private final Integer adminHttpsPort;

        public ServerDetail(String applicationName, String adminPath,
                            Integer serverHttpPort, Integer serverHttpsPort,
                            Integer adminHttpPort, Integer adminHttpsPort) {
            this.applicationName = applicationName;
            this.adminPath = adminPath;
            this.serverHttpPort = serverHttpPort;
            this.serverHttpsPort = serverHttpsPort;
            this.adminHttpPort = adminHttpPort;
            this.adminHttpsPort = adminHttpsPort;
        }

        public String generateApplicationBaseUrl(boolean isSsl) {
            String baseUrl = isSsl ? HTTPS_LOCAL_HOST_BASE_URL : HTTP_LOCAL_HOST_BASE_URL;
            int port = isSsl ? serverHttpsPort : serverHttpPort;
            return baseUrl + ":" + port;
        }

        public String generateAdminBaseUrl(boolean isSsl) {
            String baseUrl = isSsl ? HTTPS_LOCAL_HOST_BASE_URL : HTTP_LOCAL_HOST_BASE_URL;
            int port = isSsl ? adminHttpsPort : adminHttpPort;
            return baseUrl + ":" + port;
        }

        public String generateAdminUrl(boolean isSsl) {
            return generateAdminBaseUrl(isSsl) + adminPath + "?pretty=true";
        }

        public String generateHealthcheckUrl(boolean isSsl) {
            return generateAdminBaseUrl(isSsl) + adminPath + "/healthcheck?pretty=true";
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getAdminPath() {
            return adminPath;
        }

        public Integer getServerHttpPort() {
            return serverHttpPort;
        }

        public Integer getServerHttpsPort() {
            return serverHttpsPort;
        }

        public Integer getAdminHttpPort() {
            return adminHttpPort;
        }

        public Integer getAdminHttpsPort() {
            return adminHttpsPort;
        }
    }
}
