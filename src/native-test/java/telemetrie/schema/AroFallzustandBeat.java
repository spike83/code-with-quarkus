package telemetrie.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class AroFallzustandBeat {

  private OffsetDateTime eventTime;
  private String stage;
  private String newStage;
  private String hostName;
  private String tkNameId;
  private String tkNameIdConsumer;
  private String correlationId;
  private String measurePoint;
  private String geschaeftsObjektArt;
  private String aufgabenDefinition;
  private String benutzerGruppe;
  private String oe;
  private String verantwortlicheBenutzerGruppe;
  private List<String> verantwortlicheOEs;
  private List<String> verantwortlicheStellen;
  private String benoetigteRolle;

}
