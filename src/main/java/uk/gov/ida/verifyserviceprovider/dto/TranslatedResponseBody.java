package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class TranslatedResponseBody {
    public final String pid;
    public final LevelOfAssurance levelOfAssurance;
    public final ImmutableList<Attribute> attributes;

    @JsonCreator
    public TranslatedResponseBody(@JsonProperty("pid") String pid,
                                  @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
                                  @JsonProperty("attributes") List<Attribute> attributes) {
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = ImmutableList.copyOf(attributes);
    }
}
