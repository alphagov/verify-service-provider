package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SigningKeysConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private KeyPairConfiguration primary;

    @Valid
    @JsonProperty
    private KeyPairConfiguration secondary = null;

    protected SigningKeysConfiguration() {
    }

    public KeyPairConfiguration getPrimary() {
        return primary;
    }

    public KeyPairConfiguration getSecondary() {
        return secondary;
    }

    public List<KeyPairConfiguration> getKeyPairs() {
        return Stream.of(primary, secondary).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
