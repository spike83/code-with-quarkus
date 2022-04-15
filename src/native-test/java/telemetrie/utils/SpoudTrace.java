package telemetrie.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

/** Created by lzaugg on 31/08/16. */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoudTrace implements Serializable {

  static final long serialVersionUID = 12141600019L;

  private String id;

  @JsonProperty("v")
  private String version;

  @JsonProperty("t")
  private OffsetDateTime timestamp;
}
