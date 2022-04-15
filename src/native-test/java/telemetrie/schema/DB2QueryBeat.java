package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class DB2QueryBeat {

        private float queriesPerSecond;
        private float microSecondsPerQuery;
        private float cpuPerQuery;
        private float syncIOWaitPerQuery;
        private float lockWaitPerQuery;
        private float latchWaitPerQuery;
        private float unexplainedQuery;
        private String techUser;
        private OffsetDateTime eventTime;
        private String applicationName;
        private String stage = "na";
        private String newStage;
        private String meta_host;
        public String meta_tknameid = "ama-telemetry-processor-service";

}
