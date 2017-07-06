package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TrustStoreConfiguration {

    @Valid
    @JsonProperty
    @NotNull
    @Size(min = 1, message = VerifyServiceProviderConfiguration.NOT_EMPTY_MESSAGE)
    private String path;

    @Valid
    @JsonProperty
    @NotNull
    @Size(min = 1, message = VerifyServiceProviderConfiguration.NOT_EMPTY_MESSAGE)
    private String password;

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }
}
