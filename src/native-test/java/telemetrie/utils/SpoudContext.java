package telemetrie.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * SpoudContext wraps an event object (T) into some metadata which are widely used by spouds
 * pipelines. Created by lzaugg on 31/08/16.
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoudContext<T> implements Serializable {

  static final long serialVersionUID = 12141600018L;
  final SpoudContextSerializer serializer = new SpoudContextSerializer();
  SpoudContextDeserializer<T> deserializer;

  /**
   * Timestamp when the event happened in time. This is the one and only truth for event time
   * processing. This field is the timestamp field used in elasticsearch.
   */
  protected OffsetDateTime timestamp;

  /**
   * Timestamp when the event arrived the first spoud component for events we get from outside. The
   * difference between arrival and timestamp is considered to be the latency of event distribution
   * outside of spoud network and systems.
   */
  protected OffsetDateTime arrival;

  /** Path on the rest API */
  @JsonProperty(access = Access.WRITE_ONLY)
  protected String path;

  /** The event type wrapped into this spoud context. */
  protected T event;

  /** The problems found when processing the event. */
  protected List<RawDataProblem> problems = new ArrayList<>();

  /** Event type identifier used to route events to streams / elasticsearch indices. */
  protected String type;

  /**
   * Group of event types which usually belongs to a customer or use case. This is used together
   * with the type to determine streams or elasticsearch indices.
   */
  protected String group;

  /**
   * Trace is the record of all arrival timestamps of all components in the whole pipeline. Each
   * comonent should add itself with the corresponding time. So we can see where latency in the
   * pipeline comes from.
   */
  protected List<SpoudTrace> trace;

  /**
   * The id is identifying the element uniq. So if we have two objects with the same id this means
   * it's the same object and one is the update of the other. Elasticsearch for example will then do
   * an update instead of adding a new document. In some cases this is intended but be careful with
   * this field.
   */
  protected String id;

  /** The key can be used to extract a key just once and use this then for the KeyExtractors. */
  @JsonIgnore
  protected String key;

  // TODO: make the kafka related fields optional in serialization.
  /** Topic from kafka where we have read the data from. For debugging purpose. */
  @JsonIgnore
  protected String topic;

  /** Partition from kafka where we have read the data from. For debugging purpose. */
  @JsonIgnore
  protected int partition;

  /** Offset from kafka where we have read the data from. For debugging purpose. */
  @JsonIgnore
  protected long offset;

  /** Size of the input byte array. */
  @JsonIgnore
  protected int sizeInBytes;

  /** Size of the input byte array. */
  @JsonIgnore
  protected int numberOfEvents;

  /** Default constructor of SpoudContext. */
  public SpoudContext() {}

  @JsonIgnore
  /** Throttling flag. */
  protected boolean throttling;

  /**
   * Adapt the context of one SpoudContext to this one. This will cary over everything bedsides of
   * the event field and key. This is used if you derive an event so you have then the same metadata
   * and trace but another event. Be careful with the id which is copied as well. So the events will
   * override each other.
   *
   * @param context the context to adapt from.
   */
  public void adaptContext(final SpoudContext<?> context) {
    setTimestamp(context.getTimestamp());
    setArrival(context.getArrival());
    setType(context.getType());
    setGroup(context.getGroup());
    copyTrace(context.getTrace());
    setId(context.getId());
    setTopic(context.getTopic());
    setPartition(context.getPartition());
    setOffset(context.getOffset());
  }

  private void copyTrace(final List<SpoudTrace> trace) {
    if (trace == null) {
      setTrace(null);
      return;
    }
    final List<SpoudTrace> newTrace = new LinkedList<>(trace);
    setTrace(newTrace);
  }

  /**
   * Returns a compacted version of the trace which just includes components and versions in one
   * string.
   *
   * @return traceString
   */
  public String getTraceString() {
    final StringBuilder strb = new StringBuilder();
    if (trace == null) {
      return "";
    }
    for (final SpoudTrace tr : trace) {
      strb.append(tr.getId());
      strb.append(tr.getVersion());
    }
    return strb.toString();
  }

  /**
   * Adds a version trace to a SpoudContext. This trace is based on the properties app.id and
   * app.version Additionally it sets the Stream types of the SpoudContext
   *
   * @param type the types to set
   * @param version the version of the pipeline to add to the trace
   */
  public void addVersioningTrace(final String type, final String appId, final String version) {
    setType(type);
    // Add app trace
    List<SpoudTrace> traces = getTrace();
    if (traces == null) {
      traces = new LinkedList<>();
      setTrace(traces);
    }
    traces.add(new SpoudTrace(appId, version, OffsetDateTime.now()));
  }

  /**
   * Create a deep copy of this instances using serialization and deserialization.
   *
   * @return the copied spoudcontext
   */
  public SpoudContext<T> copy() {
    try {
      if (deserializer == null) {
        deserializer = new SpoudContextDeserializer<T>((Class<T>) this.event.getClass());
      }
      return deserializer.deserialize(serializer.serialize(this.topic, this), this.topic, this.partition, this.offset);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String toString() {
    return "Spoudcontext time "
        + (timestamp != null ? timestamp.toString() : "")
        + " arrival "
        + (arrival != null ? arrival.toInstant() : "")
        + " id "
        + id
        + " key "
        + key
        + " type "
        + type
        + " group "
        + group
        + " topic "
        + topic
        + " partition "
        + partition
        + " offset "
        + offset
        + " event "
        + (event != null ? event.toString() : "")
        + " ";
  }
}
