package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class TeamCityPerformanceBeat {

    private OffsetDateTime eventTime;

    private String stage;

    private int tcBuildId;

    private String tcBuildTypeId;

    private String tcProjectId;

    private String tcTestName;

    private int value90;

    private int value95;

    private int value99;

    private int nfa90;

    private int nfa95;

    private int nfa99;

    private int nrOfSamples;
}
