package telemetrie.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class KafkaProducerBeat {

  private String caller;
  private String stageCaller;
  private String cluster;
  private String topic;
  private String topicType;
  private String partitioner;
  private String payload;
  public String meta_tknameid;
  protected OffsetDateTime actionStart;
  protected OffsetDateTime actionEnd;
  private OffsetDateTime eventTime;
  private String corrId;
  private String callId;
  private String user;
  private String orgUnit;
  private Map<String, String> ccBaggage;
  private String clientType;
  private String topicPartitionOffset;
  private String groupId;
  private String newStage;

}
