package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class ServiceProviderBeat {

  public String podName;
  public String namespace;
  public String meta_tknameid;
  public String meta_host;
  private String correlationId;
  private String sourceCorrelationId;
  private String followsFromId;
  private String callId;
  private String parentCallId;
  private String callScope;
  private String callMode;
  private String appIdProvider;
  private String fkNameProvider;
  private String fkNameIdProvider;
  private String tkNameIdProvider;
  private String hostName;
  private String measurePoint;
  private boolean isFrontEnd;
  private String normMeasurePoint;
  private String appIdConsumer;
  private String tkNameIdConsumer;
  private String fkNameConsumer;
  private String fkNameIdConsumer;
  private String role;
  private String stage;
  private String traceId;
  private String sourceTraceId;
  private String newStage;
  private String userId;
  private String userSessionId;
  private String orgId;
  private String orgKurzbezeichnung = "NA";
  private String orgBezeichnung = "NA";
  private String orgSubkategorien = "NA";
  private String responseStatusCode;
  private Integer durationMs;
  private String requestUrl;
  private String httpMethod;
  private String[] tags;
  private Map<String, Object> values;
  private Map<String, String> kubernetes;
  private String containerName;
  @JsonProperty(access = Access.WRITE_ONLY)
  private String oriMeasurePoint;
  private Integer samplingRate;
}
