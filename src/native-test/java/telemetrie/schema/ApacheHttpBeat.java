package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@RegisterForReflection
public class ApacheHttpBeat {
  private String request;
  private String corrID;

  @JsonProperty("ip")
  private String ip;

  @JsonProperty("DN")
  private String DN;

  private String workerA;
  private String meta_tknameid;
  private OffsetDateTime logTime;
  @JsonProperty(access = Access.READ_ONLY)
  private int bytes;
  private String host;
  private String workerN;
  private String meta_env;
  private String logCollectorTimestamp;
  private int time;
  private int status;
}
