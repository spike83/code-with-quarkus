package telemetrie.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class RawMessageBatchDeserializer implements Deserializer {
  private static final SpoudContextDeserializer des =
      new SpoudContextDeserializer<>(ObjectNode[].class);
  private static final Logger log =
      LoggerFactory.getLogger(RawMessageBatchDeserializer.class.getName());

  @Override
  public void configure(final Map configs, final boolean isKey) {}

  @Override
  public SpoudContext<ObjectNode[]> deserialize(final String topic, final byte[] data) {
    try {

      return des.deserialize(data, topic, -1, -1);
    } catch (final IOException e) {
      log.error("error deserializing data", e);
      return null;
    }
  }

  @Override
  public void close() {}
}
