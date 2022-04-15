package telemetrie.rawdispatcher;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.utils.*;

@RegisterForReflection(
    classNames = {},
    targets = {
        org.apache.kafka.common.serialization.StringDeserializer.class,
        org.apache.kafka.common.serialization.StringSerializer.class,
        RawMessageBatchDeserializer.class,
        SpoudContextDeserializer.class,
        CountPerKeyMessageDeserializer.class,
        SpoudContextSerializer.class,
        SpoudContext.class,
        SpoudTrace.class,
        java.util.Optional.class

    })
public class RawDispatcherReflection {

}
