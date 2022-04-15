package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class PerformanceMetricBeat {

  public String meta_tknameid;
  public String meta_host;
  private String metricName;
  private String metricType;

  // derived from labels
  private Map<String, String> labels;
  private float value;
  private String userSessionId;
  private String correlationId;
  private String tkNameId;
  private String measurePoint;

  // meta
  private String stage;
  private String newStage;
}
