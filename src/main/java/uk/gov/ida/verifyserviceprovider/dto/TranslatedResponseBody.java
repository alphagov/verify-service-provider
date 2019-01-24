package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class TranslatedResponseBody {
    @JsonProperty("scenario")
    protected final Scenario scenario;
    @JsonProperty("pid")
    protected final String pid;
    @JsonProperty("levelOfAssurance")
    protected final LevelOfAssurance levelOfAssurance;
    @JsonProperty("attributes")
    protected final Attributes attributes;

    public TranslatedResponseBody(Scenario scenario, String pid, LevelOfAssurance levelOfAssurance, Attributes attributes) {
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    public Scenario getScenario() {
        return scenario;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatedResponseBody that = (TranslatedResponseBody) o;

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

    public Attributes getAttributes() {
        return attributes;
    }
}
