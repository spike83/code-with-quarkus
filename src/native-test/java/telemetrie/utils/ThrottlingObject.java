package telemetrie.utils;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@RegisterForReflection
public class ThrottlingObject {

    /**
     * The throttlingObject consists of a count, which is increased for every event which has the same key.
     * The samplingRule comes from the countPerEventKey stream (5018) and the string is parsed into the samplingFactor.
     * The samplingFactor will be used to calculate the samplingRate of the throttled event to decide how many events
     * will be sampled out.
     */
    private int count;
    private String samplingRule;
    private int samplingFactor;

    public ThrottlingObject(int count, String samplingRule) {
        this.count = count;
        this.samplingRule = samplingRule;
        setSamplingFactor(samplingRule);
    }

    public void setSamplingFactor(String samplingRule) {
        if (!samplingRule.equals("STOP")) {
            this.samplingFactor = Integer.parseInt(samplingRule.substring(samplingRule.lastIndexOf(":") + 1));
        }
    }

    public synchronized void increaseCount() {
        this.count++;
    }
}
