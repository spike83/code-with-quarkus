package telemetrie.rawdispatcher.kafka;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.spoud.mobi.telemetrie.rawdispatcher.ControlMessageProcessor;
import io.spoud.mobi.telemetrie.rawdispatcher.process.CountEventPerKeyBeatRawProcessor;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ControlMessageConsumer {
  @Inject
  ControlMessageProcessor<String, SpoudContext<CountEventPerKeyBeatRawProcessor>> processor;

  @Incoming("controlmessages")
  @Blocking
  @Acknowledgment(Strategy.PRE_PROCESSING)
  public CompletableFuture consume(final Message<SpoudContext<CountEventPerKeyBeatRawProcessor>> record) {
    return processor.processControlMessage((IncomingKafkaRecord<String, SpoudContext<CountEventPerKeyBeatRawProcessor>>) record);
  }
}
