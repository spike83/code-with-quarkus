package telemetrie.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class Mapping {
  private String tkNameId;
  @JsonProperty("Messpunkt")
  private String measurePoint;
  @JsonProperty("Tags")
  private List<String> tags;
  @JsonIgnore private Pattern measurePointPattern;

  public void setTkNameId(final String tkNameId) {
    this.tkNameId = tkNameId.trim();
  }

  public void setMeasurePoint(String measurePoint) {
    measurePoint = measurePoint.trim();
    this.measurePoint = measurePoint;
    measurePoint =
        measurePoint
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{number}", "(?:\\+|-)?[0-9]+")
            .replace("{uuid}", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    measurePointPattern = Pattern.compile(measurePoint);
  }


  // this is used implicitly (overrides the method from lombok)
  // we split up the string into an array when needed.
  @SuppressWarnings("unused")
  public void setTags(final List<String> tags) {
    final List<String> tmp = new ArrayList<>(tags);
    this.tags = new ArrayList<>();
    tmp.forEach(
        x -> {
          final String[] parts = x.split(",");
          for (final String part : parts) {
            this.tags.add(part.trim());
          }
        });
  }
}
