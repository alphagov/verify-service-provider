package uk.gov.ida.verifyserviceprovider.utils;

import com.google.common.collect.ImmutableList;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.ida.verifyserviceprovider.utils.ApplicationUrlsGenerator.ADMIN_URL_TYPE;
import static uk.gov.ida.verifyserviceprovider.utils.ApplicationUrlsGenerator.APPLICATION_URL_TYPE;
import static uk.gov.ida.verifyserviceprovider.utils.ApplicationUrlsGenerator.HEALTHCHECK_URL_TYPE;

public class UsefulApplicationUrlsTableFormatter {

    public static String format(Environment environment, Server server) {
        String startupTableTitle = environment.getName() + " started successfully with the following useful URLs:";

        return StringTableFormatter.format(100, startupTableTitle, extractTableRowsFrom(environment, server));
    }

    private static List<String> extractTableRowsFrom(Environment environment, Server server) {
        List<ServerConnector> connectors = Arrays.stream(server.getConnectors())
            .map(ServerConnector.class::cast)
            .collect(toList());

        List<ApplicationUrlsGenerator> applicationUrlsGenerators = extractDetails(environment, connectors);

        return applicationUrlsGenerators.stream()
            .map(ApplicationUrlsGenerator::generate)
            .collect(toList());
    }

    private static List<ApplicationUrlsGenerator> extractDetails(Environment environment, List<ServerConnector> connectors) {
        if (isSimpleServer(connectors)) {
            return extractRowsForSimpleServer(environment, connectors.get(0));
        }

        return connectors.stream()
            .map(ServerConnector.class::cast)
            .flatMap(serverConnector -> extractTableRowForDefaultServer(environment, serverConnector).stream())
            .collect(toList());
    }

    private static List<ApplicationUrlsGenerator> extractTableRowForDefaultServer(Environment environment, ServerConnector connector) {
        String protocol = connector.getDefaultProtocol();
        int port = connector.getLocalPort();

        if ("admin".equals(connector.getName())) {
            String adminContextPath = environment.getAdminContext().getContextPath();

            return ImmutableList.of(
                new ApplicationUrlsGenerator(ADMIN_URL_TYPE, isHttps(protocol), port, adminContextPath),
                new ApplicationUrlsGenerator(HEALTHCHECK_URL_TYPE, isHttps(protocol), port, adminContextPath)
            );
        }

        return ImmutableList.of(
            new ApplicationUrlsGenerator(APPLICATION_URL_TYPE, isHttps(protocol), port, environment.getApplicationContext().getContextPath())
        );
    }

    private static List<ApplicationUrlsGenerator> extractRowsForSimpleServer(Environment environment, ServerConnector connector) {
        return ImmutableList.of(
            new ApplicationUrlsGenerator(
                APPLICATION_URL_TYPE,
                isHttps(connector.getDefaultProtocol()),
                connector.getLocalPort(),
                environment.getApplicationContext().getContextPath()
            ),
            new ApplicationUrlsGenerator(
                ADMIN_URL_TYPE,
                isHttps(connector.getDefaultProtocol()),
                connector.getLocalPort(),
                environment.getAdminContext().getContextPath()
            ),
            new ApplicationUrlsGenerator(
                HEALTHCHECK_URL_TYPE,
                isHttps(connector.getDefaultProtocol()),
                connector.getLocalPort(),
                environment.getAdminContext().getContextPath()
            )
        );
    }

    private static boolean isSimpleServer(List<ServerConnector> connectors) {
        return connectors.size() == 1;
    }

    private static boolean isHttps(String protocol) {
        return "SSL".equals(protocol);
    }
}