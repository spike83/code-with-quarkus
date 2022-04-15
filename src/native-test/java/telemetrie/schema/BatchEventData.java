package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class BatchEventData {
  @JsonProperty("Action")
  private String Action;

  @JsonProperty("CurrentDocument")
  private String CurrentDocument;

  @JsonProperty("DocDef")
  private String DocDef;

  @JsonProperty("Envelopes")
  private String Envelopes;

  @JsonProperty("ExtractType")
  private String ExtractType;

  @JsonProperty("FoundTasks")
  private String FoundTasks;

  @JsonProperty("ID")
  private String ID;

  @JsonProperty("Material")
  private String Material;

  @JsonProperty("Pages")
  private String Pages;

  @JsonProperty("ReturnMessage")
  private String ReturnMessage;

  @JsonProperty("SLA")
  private String SLA;

  @JsonProperty("Sheets")
  private String Sheets;

  @JsonProperty("SubAction")
  private String SubAction;

  @JsonProperty("Task")
  private String Task;

  private int exitCode;
  private String state;
  private String trigger;
}
