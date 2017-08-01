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

    private static final String DROPWIZARD_CONNECTOR_APPLICATION = "application";

    private static final String DROPWIZARD_CONNECTOR_ADMIN = "admin";

    private static final String DROPWIZARD_PROTOCOL_SSL = "SSL";

    private static final String DROPWIZARD_PROTOCOL_HTTP = "HTTP";

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

    private static void fetchStandardServerDetails(ServerDetail serverDetail, Server server) {

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

    private static void fetchSimpleServerDetail(ServerDetail serverDetail, Server server) {
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

        private Integer serverHttpPort;

        private Integer serverHttpsPort;

        private Integer adminHttpPort;

        private Integer adminHttpsPort;

        ServerDetail(String applicationName, String adminPath) {
            this.applicationName = applicationName;
            this.adminPath = adminPath;
        }

        public String toLogOutputString() {

            StringBuilder sb = new StringBuilder("Logging server details...\n\n");
            sb.append("======================================================================================\n");
            sb.append("| ").append(applicationName).append(" started successfully with the following useful URLs: \n");
            sb.append("| ------------------------------------------------------------------------------------ \n");

            appendLogConditionally(sb, "| Server HTTP base URL  ", HTTP_LOCAL_HOST_BASE_URL, serverHttpPort, "");
            appendLogConditionally(sb, "| Server HTTPS base URL ", HTTPS_LOCAL_HOST_BASE_URL, serverHttpsPort, "");
            appendLogConditionally(sb, "| Admin HTTP Url        ", HTTP_LOCAL_HOST_BASE_URL, adminHttpPort, adminPath + "?pretty=true");
            appendLogConditionally(sb, "| Admin HTTPS Url       ", HTTPS_LOCAL_HOST_BASE_URL, adminHttpsPort, adminPath + "?pretty=true");
            appendLogConditionally(sb, "| Healthcheck HTTP URL  ", HTTP_LOCAL_HOST_BASE_URL, adminHttpPort, adminPath + "/healthcheck?pretty=true");
            appendLogConditionally(sb, "| Healthcheck HTTPS URL ", HTTPS_LOCAL_HOST_BASE_URL, adminHttpsPort, adminPath + "/healthcheck?pretty=true");

            sb.append("======================================================================================\n");
            return sb.toString();
        }

        private void appendLogConditionally(StringBuilder sb, String description, String baseUrl, Integer port, String subPath) {
            if (port != null) {
                sb.append(description).append(" - ")
                        .append(baseUrl).append(":").append(port).append(subPath).append("\n");
            }
        }
    }
}
