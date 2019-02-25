package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class NonMatchingTransliterableAttribute extends NonMatchingVerifiableAttribute<String> {

    @JsonProperty("nonLatinScriptValue")
    private final String nonLatinScriptValue;

    public NonMatchingTransliterableAttribute(String value,
                                              String nonLatinScriptValue,
                                              boolean verified,
                                              LocalDate from,
                                              LocalDate to) {
        super(value, verified, from, to);
        this.nonLatinScriptValue = nonLatinScriptValue;
    }

    public String getNonLatinScriptValue() {
        return nonLatinScriptValue;
    }
}
