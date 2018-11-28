package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class TranslatedNonMatchingResponseBody {

    @JsonProperty("scenario")
    private final NonMatchingScenario scenario;
    @JsonProperty("pid")
    private final String pid;
    @JsonProperty("levelOfAssurance")
    private final LevelOfAssurance levelOfAssurance;
    @JsonProperty("attributes")
    private final NonMatchingAttributes attributes;

    public TranslatedNonMatchingResponseBody(
            NonMatchingScenario scenario,
            String pid,
            LevelOfAssurance levelOfAssurance,
            NonMatchingAttributes attributes
    ) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    public NonMatchingScenario getScenario() {
        return scenario;
    }

    public String getPid() {
        return pid;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Optional<NonMatchingAttributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatedNonMatchingResponseBody that = (TranslatedNonMatchingResponseBody) o;

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
        return "TranslatedResponseBody{" +
                "scenario='" + scenario + '\'' +
                ", pid='" + pid + '\'' +
                ", levelOfAssurance=" + levelOfAssurance +
                ", attributes=" + attributes +
                '}';
    }

}
