package telemetrie.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@RegisterForReflection
public class WesBeat {
  private String Origin;
  private String country;

  //@JsonProperty("dTcB") // could be a list of string and sometimes has -
  //private int dTcB;

  private String instance;
  private String corrID;

  @JsonProperty("reqB")
  private String reqB;

  private String srvcon;

  @JsonProperty(value="sCB", access = Access.WRITE_ONLY)
  private int sCB;

  // @JsonProperty("dTB")   // could be a list of string and sometimes has -
  // private int dTB;

  private String AddrB;

  @JsonProperty("bsF")
  private int bsF;

  @JsonProperty("sCF")
  private int sCF;

  @JsonProperty("dTF")
  private int dTF;

  private String meta_env;
  private OffsetDateTime logCollectorTimestamp;

  // @JsonProperty("dTr1B")
  // private int dTr1B; // could be a list of string and sometimes has -

  private String meta_host;

  @JsonProperty("reqF")
  private String reqF;

  private String method;
  private String xcaller;
  private String Referer;
  private String meta_tknameid;
  private OffsetDateTime logTime;

  @JsonProperty("trID")
  private String trID;

  // @JsonProperty("dTsB")
  // private int dTsB; // could be a list of string and sometimes has -

  @JsonProperty("invS")
  private String invS;

  private String servername;

  @JsonProperty("ipB")
  private String ipB;

  // @JsonProperty("dTr2B")
  // private int dTr2B; // could be a list of string and sometimes has -

  private String user;

  @JsonProperty("ipF")
  private String ipF;
}
