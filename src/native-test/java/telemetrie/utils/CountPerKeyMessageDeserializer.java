package telemetrie.utils;

import io.spoud.mobi.telemetrie.rawdispatcher.process.CountEventPerKeyBeatRawProcessor;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class CountPerKeyMessageDeserializer implements Deserializer {
  private static final SpoudContextDeserializer des =
      new SpoudContextDeserializer<>(CountEventPerKeyBeatRawProcessor.class);
  private static final Logger log =
      LoggerFactory.getLogger(CountPerKeyMessageDeserializer.class.getName());

  @Override
  public void configure(final Map configs, final boolean isKey) {}

  @Override
  public SpoudContext<CountEventPerKeyBeatRawProcessor> deserialize(final String topic, final byte[] data) {
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
