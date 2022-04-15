package telemetrie.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * Provides a central instance of the jackson ObjectMapper with the correct settings. This should
 * used instead of creating new ObjectMappers.
 */
public final class JsonOm {

  private static ObjectMapper mapper;

  /**
   * Get the singleton reusable ObjectMapper instance.
   *
   * @return ObjectMapper
   */
  public static ObjectMapper getMapper() {
    if (mapper == null) {
      final JsonFactory jf = new JsonFactory();
      jf.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
      jf.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
      mapper = new ObjectMapper(jf);
      mapper.registerModule(new JavaTimeModule());

      mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      mapper
          .setSerializationInclusion(Include.NON_NULL)
          .setSerializationInclusion(Include.NON_EMPTY);
    }
    return mapper;
  }
}
