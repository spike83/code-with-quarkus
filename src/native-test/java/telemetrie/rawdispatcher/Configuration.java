package telemetrie.rawdispatcher;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.List;

@ConfigProperties(prefix = "rd")
@RegisterForReflection
@Data
public class Configuration {

  private List<String> eventTypes;

  private String kafkaProducerTopicTemplate;

  private boolean kafkaProducerTestFraction;

  private String inaccurateTopic;

  private String spamTopic;

  private String samplingTopic;

  private String jsonParseErrorTopicName;

  private boolean throttling;

  private int initialDelayMs;

}
