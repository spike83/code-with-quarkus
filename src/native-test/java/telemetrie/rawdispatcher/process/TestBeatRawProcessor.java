package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.TestBeat;
import io.spoud.mobi.telemetrie.utils.Key;
import io.spoud.mobi.telemetrie.utils.Key.Method;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;

@ApplicationScoped
@RegisterForReflection
public class TestBeatRawProcessor extends TestBeat implements ThrottledBeatRawProcessor<TestBeatRawProcessor> {

  // register type in processors registry
  static {
    RawDispatcher.processorsRegistry.put("test", TestBeatRawProcessor.class);
  }

  @Getter
  @Setter
  @JsonProperty(access = Access.WRITE_ONLY)
  public String fraction = "";

  @Getter public OffsetDateTime eventTime;

  @Override
  @JsonIgnore
  public String extractKey(final SpoudContext<TestBeatRawProcessor> sc) {
    return new Key.Builder()
        .withPart(getS())
        .withPart(getF())
        .withMethod(Method.SHA256HEX)
        .build()
        .toString();
  }

  @Override
  public OffsetDateTime extractTimestamp(final OffsetDateTime batchEventTime) {
    return getEventTime();
  }

  @Override
  public boolean isValid(final SpoudContext<TestBeatRawProcessor> sc) {
    return true;
  }

  @Override
  public String getDataProducer() {
    return "unknown";
  }

  @Override
  public String getStage() {
    return STAGE_NOT_APPLICABLE;
  }

  @Override
  public void setStage(final String stage) {
    // no stage in this beat
  }

  @Override
  @JsonIgnore
  public String getCorrelationId() {
    return null;
  }

  @Override
  public String extractThrottlingKey(SpoudContext<TestBeatRawProcessor> sc) {
    return this.getS();
  }
}
