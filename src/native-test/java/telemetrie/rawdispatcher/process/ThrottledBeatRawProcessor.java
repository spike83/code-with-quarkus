package telemetrie.rawdispatcher.process;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.utils.CountEventPerKeyState;
import io.spoud.mobi.telemetrie.utils.SpoudContext;

import java.util.concurrent.atomic.AtomicInteger;

@RegisterForReflection
public interface ThrottledBeatRawProcessor<T> extends RawProcessor<T>{

  AtomicInteger throttled = new AtomicInteger(0);
  String extractThrottlingKey(final SpoudContext<T> sc);

  void setSamplingRate(Integer samplingRate);

  // SHI-2245: Implementation Throttling
  default boolean throttle(SpoudContext<T> sc, boolean throttlingEnabled) {
    String throttlingKey = extractThrottlingKey(sc);
    if (CountEventPerKeyState.getInstance().retrieve(throttlingKey) != null) {
      CountEventPerKeyState.getInstance().retrieve(throttlingKey).increaseCount();
      int count = CountEventPerKeyState.getInstance().retrieve(throttlingKey).getCount();
      int samplingFactor =
          CountEventPerKeyState.getInstance().retrieve(throttlingKey).getSamplingFactor();
      int samplingRate = 100 / (100 - samplingFactor);
      boolean keep = count % samplingRate == 0;
      if(throttlingEnabled && !keep){
        setSamplingRate(samplingRate);
        sc.setThrottling(true);
        return true;
      }else{
        if(!keep && throttled.incrementAndGet() % 1000 == 0){
          log.warn("would have throttled by 1000 events but throttling is not enabled");
          throttled.set(0);
        }
        setSamplingRate(samplingRate);
        sc.setThrottling(false);
        return false;
      }
    } else {
      setSamplingRate(1);
      sc.setThrottling(false);
      return false;
    }
  }

  @Override
  default void process(final SpoudContext<T> sc, boolean throttlingEnabled) {
    RawProcessor.super.process(sc, throttlingEnabled);
    throttle(sc, throttlingEnabled);
  }

  @Override
  default String getTargetTopic(final SpoudContext<T> sc, String topicPattern, String samplingTopic, String inaccurateTopic, String spamTopic) {
    if (!isValid(sc)) {
      topicPattern = inaccurateTopic;
    }
    if (sc.isThrottling()){
      if(samplingTopic == null){
        throw new IllegalArgumentException("sampling topic not defined");
      }
      return samplingTopic;
    }
    return RawProcessor.super.getTargetTopic(sc, topicPattern, samplingTopic, inaccurateTopic, spamTopic);
  }

}
