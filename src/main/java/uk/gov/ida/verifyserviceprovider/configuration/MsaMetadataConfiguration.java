package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import org.apache.commons.lang3.NotImplementedException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

import static io.dropwizard.util.Duration.minutes;
import static io.dropwizard.util.Duration.seconds;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;

public class MsaMetadataConfiguration implements VerifyServiceProviderMetadataConfiguration {

    private final URI uri;
    private final Long minRefreshDelay;
    private final Long maxRefreshDelay;
    private final String expectedEntityId;
    private final JerseyClientConfiguration jerseyClientConfiguration;
    private final String jerseyClientName;

    @JsonCreator
    public MsaMetadataConfiguration(
        @JsonProperty("uri") URI uri,
        @JsonProperty("minRefreshDelay") Long minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("jerseyClientConfiguration") JerseyClientConfiguration jerseyClientConfiguration,
        @JsonProperty("jerseyClientName") String jerseyClientName
    ) {
        this.uri = uri;
        this.minRefreshDelay = ofNullable(minRefreshDelay).orElse(60000L);
        this.maxRefreshDelay = ofNullable(maxRefreshDelay).orElse(600000L);
        this.expectedEntityId = expectedEntityId;
        this.jerseyClientConfiguration = ofNullable(jerseyClientConfiguration).orElse(createClient());
        this.jerseyClientName = ofNullable(jerseyClientName).orElse(MSA_JERSEY_CLIENT_NAME);
    }

    @Override
    public String getTrustStorePath() {
        throw new NotImplementedException("MSA metadata is not signed, so no trust store available");
    }

    @Override
    public String getTrustStorePassword() {
        throw new NotImplementedException("MSA metadata is not signed, so no trust store available");
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

    @Override
    public boolean shouldLoadTrustStoreFromResources() {
        return false;
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
}
