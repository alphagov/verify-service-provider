package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

import static io.dropwizard.util.Duration.minutes;
import static io.dropwizard.util.Duration.seconds;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;

public class HubMetadataConfiguration implements VerifyServiceProviderMetadataConfiguration {
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

    public HubMetadataConfiguration(HubEnvironment environment, MetadataConfigurationOverrides overrides) {
        this.trustStorePath = ofNullable(overrides.getTrustStorePath()).orElseGet(() -> generateTrustStorePath(environment));
        this.trustStorePassword = ofNullable(overrides.getTrustStorePassword()).orElse(DEFAULT_TRUST_STORE_PASSWORD);
        this.uri = ofNullable(overrides.getMetadataUri()).orElse(environment.getMetadataUri());
        this.minRefreshDelay = ofNullable(overrides.getMinRefreshDelay()).orElse(60000L);
        this.maxRefreshDelay = ofNullable(overrides.getMaxRefreshDelay()).orElse(600000L);
        this.expectedEntityId = ofNullable(overrides.getEntityId()).orElseGet(() -> generateExpectedEntityId(environment));
        this.jerseyClientConfiguration = ofNullable(overrides.getJerseyClientConfiguration()).orElse(createClient());
        this.jerseyClientName = ofNullable(overrides.getJerseyClientName()).orElse(HUB_JERSEY_CLIENT_NAME);
        this.shouldLoadTrustStoreFromResources = (overrides.getTrustStorePath() == null);
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
        return new JerseyClientConfiguration() {{
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
    }

    private static String generateTrustStorePath(HubEnvironment hubEnvironment) {
        String trustStoreName;
        switch (hubEnvironment) {
            case PRODUCTION:
                trustStoreName = PRODUCTION_VERIFY_TRUSTSTORE_NAME;
                break;
            case INTEGRATION:
            case COMPLIANCE_TOOL:
                trustStoreName = TEST_VERIFY_TRUSTSTORE_NAME;
                break;
            default:
                throw new RuntimeException("No trust store configured for Hub Environment: " + hubEnvironment.name());
        }

        return trustStoreName;
    }

    private static String generateExpectedEntityId(HubEnvironment hubEnvironment) {
        String expectedEntityId;
        switch (hubEnvironment) {
            case PRODUCTION:
            case INTEGRATION:
            case COMPLIANCE_TOOL:
                expectedEntityId = "https://signin.service.gov.uk";
                break;
            default:
                throw new RuntimeException("No entity ID configured for Hub Environment: " + hubEnvironment.name());
        }

        return expectedEntityId;
    }
}
