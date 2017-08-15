package uk.gov.ida.verifyserviceprovider.dto;

import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

public enum LevelOfAssurance {

    LEVEL_1(1),
    LEVEL_2(2);

    private final int value;

    private LevelOfAssurance(int value) {
        this.value = value;
    }

    public static LevelOfAssurance fromSamlValue(String samlValue) {
        switch (samlValue) {
            case IdaAuthnContext.LEVEL_1_AUTHN_CTX:
                return LEVEL_1;
            case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return LEVEL_2;
            default:
                throw new RuntimeException("Unknown level of assurance: " + samlValue);
        }
    }

    public boolean isGreaterThan(LevelOfAssurance target) {
        return value > target.value;
    }
}
