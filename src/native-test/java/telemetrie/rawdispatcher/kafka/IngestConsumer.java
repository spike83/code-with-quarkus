package telemetrie.rawdispatcher.kafka;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.spoud.mobi.telemetrie.rawdispatcher.IngestlMessageProcessor;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class IngestConsumer {
  @Inject
  IngestlMessageProcessor<String, SpoudContext<ObjectNode[]>> processor;

  @Incoming("events")
  @Blocking
  @Acknowledgment(Strategy.POST_PROCESSING)
  public CompletableFuture<Void> consume(final Message<SpoudContext<ObjectNode[]>> record) {
    return processor.processIngestMessage((IncomingKafkaRecord<String, SpoudContext<ObjectNode[]>>) record);
  }
}
