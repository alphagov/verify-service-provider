package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;

public class VerifyServiceProviderConfiguration extends Configuration {

    public static final String NOT_EMPTY_MESSAGE = "may not be empty";

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String hubSsoLocation;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String hubEntityId;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String msaEntityId;

    @JsonProperty
    @NotNull
    @Valid
    private URI msaMetadataUrl;

    @JsonProperty
    @NotNull
    @Valid
    private URI hubMetadataUrl;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String secret;

    @JsonProperty
    @NotNull
    @Valid
    private TrustStoreConfiguration msaTrustStore;

    @JsonProperty
    @NotNull
    @Valid
    private TrustStoreConfiguration hubTrustStore;

    @JsonProperty
    @NotNull
    @Valid
    private TrustStoreConfiguration relyingPartyTrustStore;

    @JsonProperty
    @NotNull
    @Valid
    private SigningKeysConfiguration signingKeys;

    public String getHubSsoLocation() {
        return hubSsoLocation;
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

    public URI getMsaMetadataUrl() {
        return msaMetadataUrl;
    }

    public URI getHubMetadataUrl() {
        return hubMetadataUrl;
    }

    public String getSecret() {
        return secret;
    }

    public TrustStoreConfiguration getMsaTrustStore() {
        return msaTrustStore;
    }

    public TrustStoreConfiguration getHubTrustStore() {
        return hubTrustStore;
    }

    public TrustStoreConfiguration getRelyingPartyTrustStore() {
        return relyingPartyTrustStore;
    }

    public String getMsaEntityId() {
        return msaEntityId;
    }
}
