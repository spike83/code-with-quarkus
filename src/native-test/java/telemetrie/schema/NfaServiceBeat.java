package telemetrie.schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@RegisterForReflection
public class NfaServiceBeat {
  private String tkNameId;
  private String measurePoint;
  private String httpMethod;
  private int responseTimeP90;
  private int responseTimeP95;
  private int responseTimeP99;
  private String resource;
  private int concurrency;
  private String availability;
  private String meta_host;
  private OffsetDateTime logCollectorTimestamp;
  private String meta_tknameid;
  private int performanceIndexBaseline;
  private String baselineSource;

  private String[] tags;
  private Map<String, String> values;
}
