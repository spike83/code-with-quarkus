package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.schema.CountEventPerKeyBeat;
import io.spoud.mobi.telemetrie.utils.CountEventPerKeyState;
import io.spoud.mobi.telemetrie.utils.MissingField;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import io.spoud.mobi.telemetrie.utils.ThrottlingObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@RegisterForReflection
public class CountEventPerKeyBeatRawProcessor extends CountEventPerKeyBeat implements RawProcessor<CountEventPerKeyBeatRawProcessor> {


    @Getter
    @Setter
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String fraction = "";

    @Override
    public String extractKey(SpoudContext<CountEventPerKeyBeatRawProcessor> sc) {
        return null;
    }

    @Override
    @JsonIgnore
    public void process(final SpoudContext<CountEventPerKeyBeatRawProcessor> sc,
        boolean throttlingEnabled) {
        buildMap(sc);
    }

    @Override
    @JsonIgnore
    public List<MissingField> getMissingFields() {
        final List<MissingField> missing = new ArrayList<>();
        missing.add(getMissingField("stage", getStage()));
        return missing;
    }

    @Override
    public String getDataProducer() {
        return "unknown";
    }

    @Override
    public String getStage() {
        return null;
    }

    @Override
    public void setStage(String stage) {
        // no stage in beat
    }

    @Override
    public String getCorrelationId() {
        return "unknown";
    }

    @JsonIgnore
    public void buildMap(final SpoudContext<CountEventPerKeyBeatRawProcessor> sc){
        String key = sc.getEvent().getKey();
        ThrottlingObject value = new ThrottlingObject(0, sc.getEvent().getSamplingRule());

        if (!value.getSamplingRule().equals("STOP")){
            CountEventPerKeyState.getInstance().store(key, value);
        } else {
            CountEventPerKeyState.getInstance().remove(key);
        }
    }
}
