package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class KeyPairConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    @Size(min = 1, message = VerifyServiceProviderConfiguration.NOT_EMPTY_MESSAGE)
    private String publicKey;

    @NotNull
    @Valid
    @JsonProperty
    @Size(min = 1, message = VerifyServiceProviderConfiguration.NOT_EMPTY_MESSAGE)
    private String privateKey;

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }
}
