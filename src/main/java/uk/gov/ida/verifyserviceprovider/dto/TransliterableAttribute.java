package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class TransliterableAttribute extends HistoricalVerifiableAttribute<String> {

    @JsonProperty("nonLatinScriptValue") @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String nonLatinScriptValue;

    public TransliterableAttribute(String value,
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
