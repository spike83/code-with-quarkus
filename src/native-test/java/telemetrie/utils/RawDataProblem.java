package telemetrie.utils;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.List;

@Data
@RegisterForReflection
public class RawDataProblem {
  public List<MissingField> missingFields;
  public String reason;
}
