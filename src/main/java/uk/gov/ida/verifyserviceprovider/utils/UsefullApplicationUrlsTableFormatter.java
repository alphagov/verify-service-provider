package uk.gov.ida.verifyserviceprovider.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class UsefullApplicationUrlsTableFormatter {

    public static String format(ServerDetailFinder.ServerDetail serverDetail) {
        String startupTableTitle = serverDetail.getApplicationName() + " started successfully with the following useful URLs:";

        return StringTableFormatter.format(100, startupTableTitle, getStartupTableRowsData(serverDetail));
    }

    private static List<String> getStartupTableRowsData(ServerDetailFinder.ServerDetail serverDetail) {
        String serverHttpBaseUrl = Optional.ofNullable(serverDetail.getServerHttpPort())
            .map(port -> "Server HTTP base URL   - " + serverDetail.generateApplicationBaseUrl(false))
            .orElse("");
        String serverHttpsBaseUrl = Optional.ofNullable(serverDetail.getServerHttpsPort())
            .map(port -> "Server HTTPS base URL  - " + serverDetail.generateApplicationBaseUrl(true))
            .orElse("");
        String adminHttpUrl = Optional.ofNullable(serverDetail.getAdminHttpPort())
            .map(port -> "Admin HTTP Url         - " + serverDetail.generateAdminUrl(false))
            .orElse("");
        String healthCheckHttpUrl = Optional.ofNullable(serverDetail.getAdminHttpPort())
            .map(port -> "Healthcheck HTTP URL   - " + serverDetail.generateHealthcheckUrl(false))
            .orElse("");
        String adminHttpsUrl = Optional.ofNullable(serverDetail.getAdminHttpsPort())
            .map(port -> "Admin HTTPS Url        - " + serverDetail.generateAdminUrl(true))
            .orElse("");
        String healthCheckHttpsUrl = Optional.ofNullable(serverDetail.getAdminHttpsPort())
            .map(port -> "Healthcheck HTTPS URL  - " + serverDetail.generateHealthcheckUrl(true))
            .orElse("");

        List<String> result = ImmutableList.of(
            serverHttpBaseUrl,
            serverHttpsBaseUrl,
            adminHttpUrl,
            healthCheckHttpUrl,
            adminHttpsUrl,
            healthCheckHttpsUrl
        ).stream()
            .filter(item -> !item.isEmpty())
            .collect(toList());

        return result;
    }
}
