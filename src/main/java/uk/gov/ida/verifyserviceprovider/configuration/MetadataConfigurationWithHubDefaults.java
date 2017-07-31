package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

import static io.dropwizard.util.Duration.minutes;
import static io.dropwizard.util.Duration.seconds;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_TOOL_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;

public class MetadataConfigurationWithHubDefaults implements VerifyServiceProviderMetadataConfiguration {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */
    private String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";

    private final String trustStorePath;
    private final String trustStorePassword;
    private final URI uri;
    private final Long minRefreshDelay;
    private final Long maxRefreshDelay;
    private final String expectedEntityId;
    private final JerseyClientConfiguration jerseyClientConfiguration;
    private final String jerseyClientName;
    private final boolean shouldLoadTrustStoreFromResources;

    @JsonCreator
    public MetadataConfigurationWithHubDefaults(
        @JsonProperty("trustStorePath") String trustStorePath,
        @JsonProperty("trustStorePassword") String trustStorePassword,
        @JsonProperty("uri") URI uri,
        @JsonProperty("minRefreshDelay") Long minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("jerseyClientConfiguration") JerseyClientConfiguration jerseyClientConfiguration,
        @JsonProperty("jerseyClientName") String jerseyClientName
    ) {
        this.trustStorePath = ofNullable(trustStorePath).orElseGet(() -> generateTrustStorePath(uri));
        this.trustStorePassword = ofNullable(trustStorePassword).orElse(DEFAULT_TRUST_STORE_PASSWORD);
        this.uri = uri;
        this.minRefreshDelay = ofNullable(minRefreshDelay).orElse(60000L);
        this.maxRefreshDelay = ofNullable(maxRefreshDelay).orElse(600000L);
        this.expectedEntityId = generateExpectedEntityId(uri, expectedEntityId);
        this.jerseyClientConfiguration = ofNullable(jerseyClientConfiguration).orElse(createClient());
        this.jerseyClientName = ofNullable(jerseyClientName).orElse(HUB_JERSEY_CLIENT_NAME);
        this.shouldLoadTrustStoreFromResources = (trustStorePath == null);
    }

    @NotNull
    @Valid
    @Override
    public String getTrustStorePath() {
        return trustStorePath;
    }

    @NotNull
    @Valid
    @Override
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    @NotNull
    @Valid
    @Override
    public URI getUri() {
        return uri;
    }

    @NotNull
    @Valid
    @Override
    public Long getMinRefreshDelay() {
        return minRefreshDelay;
    }

    @NotNull
    @Valid
    @Override
    public Long getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    @NotNull
    @Valid
    @Override
    public String getExpectedEntityId() {
        return expectedEntityId;
    }

    @NotNull
    @Valid
    @Override
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }

    @NotNull
    @Valid
    @Override
    public String getJerseyClientName() {
        return jerseyClientName;
    }

    @JsonIgnore
    @Override
    public boolean shouldLoadTrustStoreFromResources() {
        return shouldLoadTrustStoreFromResources;
    }

    private static JerseyClientConfiguration createClient() {
        JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration() {{
            setTimeout(seconds(2));
            setTimeToLive(minutes(10));
            setCookiesEnabled(false);
            setConnectionTimeout(seconds(1));
            setRetries(3);
            setKeepAlive(seconds(60));
            setChunkedEncodingEnabled(false);
            setValidateAfterInactivityPeriod(seconds(5));
            TlsConfiguration tlsConfiguration = new TlsConfiguration() {{
                setProtocol("TLSv1.2");
                setVerifyHostname(true);
                setTrustSelfSignedCertificates(false);
            }};
            setTlsConfiguration(tlsConfiguration);
        }};
        return jerseyClientConfiguration;
    }

    private static String generateTrustStorePath(URI uri) {
        String trustStoreName;
        switch (uri.toString()) {
            case PRODUCTION_METADATA_URI:
                trustStoreName = PRODUCTION_VERIFY_TRUSTSTORE_NAME;
                break;
            case INTEGRATION_METADATA_URI:
            case COMPLIANCE_TOOL_METADATA_URI:
                trustStoreName = TEST_VERIFY_TRUSTSTORE_NAME;
                break;
            default:
                throw new RuntimeException("Unknown metadata uri");
        }

        return trustStoreName;
    }

    private static String generateExpectedEntityId(URI uri, String providedExpectedEntityId) {
        if (providedExpectedEntityId != null) {
            return providedExpectedEntityId;
        }

        String expectedEntityId;
        switch (uri.toString()) {
            case PRODUCTION_METADATA_URI:
            case INTEGRATION_METADATA_URI:
            case COMPLIANCE_TOOL_METADATA_URI:
                expectedEntityId = "https://signin.service.gov.uk";
                break;
            default:
                throw new RuntimeException("Unknown metadata uri");
        }

        return expectedEntityId;
    }
}
