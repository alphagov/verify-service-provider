package uk.gov.ida.verifyserviceprovider.utils;

import io.dropwizard.Configuration;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Utility that looks up various server details so that useful URLs can be logged to the log file.
 */
public class ServerDetailFinder {

    static final String DROPWIZARD_CONNECTOR_APPLICATION = "application";

    static final String DROPWIZARD_CONNECTOR_ADMIN = "admin";

    static final String DROPWIZARD_PROTOCOL_SSL = "SSL";

    static final String DROPWIZARD_PROTOCOL_HTTP = "HTTP";

    public static ServerDetail fetchServerDetails(Server server, Configuration config, Environment environment) {

        ServerDetail serverDetail = new ServerDetail(environment.getName(), environment.getAdminContext().getContextPath());

        if (config.getServerFactory() instanceof SimpleServerFactory) {
            fetchSimpleServerDetail(serverDetail, server);
        }
        else {
            fetchStandardServerDetails(serverDetail, server);
        }

        return serverDetail;
    }

    static void fetchStandardServerDetails(ServerDetail serverDetail, Server server) {

        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                ServerConnector serverConnector = (ServerConnector) connector;

                if (serverConnector.getName().equals(DROPWIZARD_CONNECTOR_APPLICATION)) {
                    if (serverConnector.getDefaultProtocol().equals(DROPWIZARD_PROTOCOL_SSL)) {
                        serverDetail.serverHttpsPort = serverConnector.getLocalPort();
                    }
                    else if (serverConnector.getDefaultProtocol().startsWith(DROPWIZARD_PROTOCOL_HTTP)) {
                        serverDetail.serverHttpPort = serverConnector.getLocalPort();
                    }
                }

                if (serverConnector.getName().equals(DROPWIZARD_CONNECTOR_ADMIN)) {
                    if (serverConnector.getDefaultProtocol().equals(DROPWIZARD_PROTOCOL_SSL)) {
                        serverDetail.adminHttpsPort = serverConnector.getLocalPort();
                    }
                    else if (serverConnector.getDefaultProtocol().startsWith(DROPWIZARD_PROTOCOL_HTTP)) {
                        serverDetail.adminHttpPort = serverConnector.getLocalPort();
                    }
                }
            }
        }
    }

    static void fetchSimpleServerDetail(ServerDetail serverDetail, Server server) {
        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                ServerConnector serverConnector = (ServerConnector) connector;
                serverDetail.serverHttpPort = serverConnector.getLocalPort();
                serverDetail.adminHttpPort = serverConnector.getLocalPort();
                break;
            }
        }
    }

    public static class ServerDetail {

        private static final String LOCAL_HOST = "localhost";

        private static final String HTTP_LOCAL_HOST_BASE_URL = "http://" + LOCAL_HOST;

        private static final String HTTPS_LOCAL_HOST_BASE_URL = "https://" + LOCAL_HOST;

        private final String applicationName;

        private final String adminPath;

        Integer serverHttpPort;

        Integer serverHttpsPort;

        Integer adminHttpPort;

        Integer adminHttpsPort;

        ServerDetail(String applicationName, String adminPath) {
            this.applicationName = applicationName;
            this.adminPath = adminPath;
        }

        public String toLogOutputString() {

            StringBuilder sb = new StringBuilder("Logging server details...\n\n");
            sb.append("======================================================================================\n");
            sb.append("| ").append(applicationName).append(" started successfully with the following useful URLs: \n");
            sb.append("| ------------------------------------------------------------------------------------ \n");

            if (serverHttpPort != null) {
                appendUrlLog(sb, "| Server HTTP base URL  ", HTTP_LOCAL_HOST_BASE_URL, serverHttpPort, "");
            }

            if (serverHttpsPort != null) {
                appendUrlLog(sb, "| Server HTTPS base URL ", HTTPS_LOCAL_HOST_BASE_URL, serverHttpsPort, "");
            }

            if (adminHttpPort != null) {
                appendUrlLog(sb, "| Admin HTTP Url        ", HTTP_LOCAL_HOST_BASE_URL, adminHttpPort, adminPath + "?pretty=true");
                appendUrlLog(sb, "| Healthcheck HTTP URL  ", HTTP_LOCAL_HOST_BASE_URL, adminHttpPort, adminPath + "/healthcheck?pretty=true");
            }

            if (adminHttpsPort != null) {
                appendUrlLog(sb, "| Admin HTTPS Url       ", HTTPS_LOCAL_HOST_BASE_URL, adminHttpsPort, adminPath + "?pretty=true");
                appendUrlLog(sb, "| Healthcheck HTTPS URL ", HTTPS_LOCAL_HOST_BASE_URL, adminHttpsPort, adminPath + "/healthcheck?pretty=true");
            }

            sb.append("======================================================================================\n");
            return sb.toString();
        }

        String generateApplicationBaseUrl(boolean isSsl) {
            String baseUrl = isSsl ? HTTPS_LOCAL_HOST_BASE_URL : HTTP_LOCAL_HOST_BASE_URL;
            int port = isSsl ? serverHttpsPort : serverHttpPort;
            return baseUrl + ":" + port;
        }

        String generateAdminBaseUrl(boolean isSsl) {
            String baseUrl = isSsl ? HTTPS_LOCAL_HOST_BASE_URL : HTTP_LOCAL_HOST_BASE_URL;
            int port = isSsl ? adminHttpsPort : adminHttpPort;
            return baseUrl + ":" + port;
        }

        String generateAdminUrl(boolean isSsl) {
            return generateAdminBaseUrl(isSsl) + "/" + adminPath + "?pretty=true";
        }

        String generateHealthcheckUrl(boolean isSsl) {
            return generateAdminBaseUrl(isSsl) + "/" + adminPath + "/healthcheck?pretty=true";
        }

        private void appendUrlLog(StringBuilder sb, String description, String baseUrl, Integer port, String subPath) {
            sb.append(description).append(" - ").append(baseUrl).append(":").append(port).append(subPath).append("\n");
        }
    }
}
