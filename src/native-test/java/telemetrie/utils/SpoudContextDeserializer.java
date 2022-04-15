package telemetrie.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SpoudContextDeserializer<T> {

  static final long serialVersionUID = 12141600015l;
  private static final Logger log = LoggerFactory.getLogger(SpoudContextDeserializer.class);

  private final JavaType type;

  private final ObjectReader reader;

  public SpoudContextDeserializer() {
    type =
        TypeFactory.defaultInstance().constructParametricType(SpoudContext.class, ObjectNode.class);
    reader = JsonOm.getMapper().readerFor(type);
  }
  /**
   * @param typeParameterClass parameter type class used to specify the @see JavaType to use
   *     for @see ObjectMapper
   */
  public SpoudContextDeserializer(final Class<T> typeParameterClass) {
    type =
        TypeFactory.defaultInstance()
            .constructParametricType(SpoudContext.class, typeParameterClass);
    reader = JsonOm.getMapper().readerFor(type);
  }

  public SpoudContext<T> deserialize(final byte[] message, final String topic, final int partition, final long offset)
      throws IOException {
    if (message == null) {
      return null;
    }

    final SpoudContext<T> sc = reader.readValue(message);
    sc.setSizeInBytes(message.length);
    if (sc.getEvent() instanceof ObjectNode[]) {
      sc.setNumberOfEvents(((ObjectNode[]) sc.getEvent()).length);
    }
    sc.setPartition(partition);
    sc.setTopic(topic);
    sc.setOffset(offset);
    return sc;
  }
}
