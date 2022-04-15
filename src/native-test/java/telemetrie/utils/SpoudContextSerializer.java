package telemetrie.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Serializable;
import java.util.Map;

public class SpoudContextSerializer implements Serializer<SpoudContext<?>>, Serializable {

  static final long serialVersionUID = 12141600018l;

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {}

  @Override
  public byte[] serialize(final String topic, final SpoudContext<?> data) {
    try {
      return JsonOm.getMapper().writeValueAsBytes(data);
    } catch (final JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void close() {}
}
