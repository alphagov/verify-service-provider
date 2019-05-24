package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class TranslatedMatchingResponseBody implements TranslatedResponseBody {

    private final MatchingScenario scenario;
    private final String pid;
    private final LevelOfAssurance levelOfAssurance;
    private final Attributes attributes;

    @JsonCreator
    public TranslatedMatchingResponseBody(
        @JsonProperty("scenario") MatchingScenario scenario,
        @JsonProperty("pid") String pid,
        @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
        @JsonProperty("attributes") Attributes attributes
    ) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    public MatchingScenario getScenario() {
        return scenario;
    }

    public String getPid() {
        return pid;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Optional<Attributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatedMatchingResponseBody that = (TranslatedMatchingResponseBody) o;

        if (scenario != that.scenario) return false;
        if (pid != null ? !pid.equals(that.pid) : that.pid != null) return false;
        if (levelOfAssurance != that.levelOfAssurance) return false;
        return attributes != null ? attributes.equals(that.attributes) : that.attributes == null;
    }

    @Override
    public int hashCode() {
        int result = scenario != null ? scenario.hashCode() : 0;
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        result = 31 * result + (levelOfAssurance != null ? levelOfAssurance.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TranslatedMatchingResponseBody{" +
            "scenario='" + scenario + '\'' +
            ", pid='" + pid + '\'' +
            ", levelOfAssurance=" + levelOfAssurance +
            ", attributes=" + attributes +
            '}';
    }
}
