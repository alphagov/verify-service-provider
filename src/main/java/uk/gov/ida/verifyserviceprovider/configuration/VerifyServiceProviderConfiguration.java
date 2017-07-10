package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

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
    private String secureTokenSeed;

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
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String signingPrivateKey;

    @NotNull
    @Valid
    @Size(min = 1, max = 2)
    @JsonProperty
    private List<String> encryptionCertificates;

    public String getHubSsoLocation() {
        return hubSsoLocation;
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

    public URI getMsaMetadataUrl() {
        return msaMetadataUrl;
    }

    public String getSecureTokenSeed() {
        return secureTokenSeed;
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

    public URI getHubMetadataUrl() {
        return hubMetadataUrl;
    }

    public String getSigningPrivateKey() {
        return signingPrivateKey;
    }

    public List<String> getEncryptionCertificates() {
        return encryptionCertificates;
    }
}
