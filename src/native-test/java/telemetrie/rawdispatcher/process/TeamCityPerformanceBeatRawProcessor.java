package telemetrie.rawdispatcher.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.spoud.mobi.telemetrie.rawdispatcher.RawDispatcher;
import io.spoud.mobi.telemetrie.schema.TeamCityPerformanceBeat;
import io.spoud.mobi.telemetrie.utils.Key;
import io.spoud.mobi.telemetrie.utils.Key.Method;
import io.spoud.mobi.telemetrie.utils.MissingField;
import io.spoud.mobi.telemetrie.utils.SpoudContext;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RegisterForReflection
public class TeamCityPerformanceBeatRawProcessor extends TeamCityPerformanceBeat implements RawProcessor<TeamCityPerformanceBeatRawProcessor> {

    // register type in processors registry
    static {
        RawDispatcher.processorsRegistry.put("71", TeamCityPerformanceBeatRawProcessor.class);
    }

    @Getter
    @Setter
    @JsonProperty(access = Access.WRITE_ONLY)
    public String fraction = "";

    @Override
    @JsonIgnore
    public OffsetDateTime extractTimestamp(final OffsetDateTime eventTime) {
        return getEventTime();
    }

    @Override
    @JsonIgnore
    public String extractKey(final SpoudContext<TeamCityPerformanceBeatRawProcessor> sc) {
        return new Key.Builder()
                .withPart(getStage())
                .withPart(getTcBuildTypeId())
                .withMethod(Method.SHA256HEX)
                .build()
                .toString();
    }

    @Override
    @JsonIgnore
    public void process(final SpoudContext<TeamCityPerformanceBeatRawProcessor> sc,
        boolean throttlingEnabled) {
        RawProcessor.super.process(sc, throttlingEnabled);
    }

    @Override
    @JsonIgnore
    public List<MissingField> getMissingFields() {
        final List<MissingField> missing = new ArrayList<>();
        missing.add(getMissingField("stage", getStage()));
        missing.add(getMissingField("tcBuildId", getTcBuildId()));
        missing.add(getMissingField("tcBuildTypeId", getTcBuildTypeId()));
        missing.add(getMissingField("tcProjectId", getTcProjectId()));
        missing.add(getMissingField("tcTestName", getTcTestName()));
        missing.add(getMissingField("value90", getValue90()));
        missing.add(getMissingField("value95", getValue95()));
        missing.add(getMissingField("value99", getValue99()));
        return missing;
    }

    @Override
    @JsonIgnore
    public String getDataProducer() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getCorrelationId() {
        return null;
    }
}


