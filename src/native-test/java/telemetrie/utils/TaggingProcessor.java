package telemetrie.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.spoud.mobi.telemetrie.rawdispatcher.process.ServiceProviderBeatRawProcessor;
import io.spoud.mobi.telemetrie.schema.NfaServiceBeat;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.*;

@Slf4j
public class TaggingProcessor {

  private static final HashMap<String, TaggingProcessor> processorCahce = new HashMap<>();
  private final HashMap<String, List<Mapping>> componentWiseMapping = new HashMap<>();

  public TaggingProcessor(final String filename) {
    final CsvMapper mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
    InputStream is =  getClass().getResourceAsStream(filename);

    final CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
    MappingIterator<Mapping> it;
    try {
      it = mapper.readerFor(Mapping.class).with(bootstrapSchema).readValues(is);
      while (it.hasNext()) {
        final Mapping m = it.next();
        componentWiseMapping.computeIfAbsent(m.getTkNameId(), k -> new ArrayList<>());
        componentWiseMapping.get(m.getTkNameId()).add(m);
      }
    } catch (final Exception e) {
      log.error("loading csvFile {} failed", filename, e);
    }
  }

  public static TaggingProcessor getInstance(final String s) {
    TaggingProcessor p = processorCahce.get(s);
    if (p == null) {
      p = new TaggingProcessor(s);
      processorCahce.put(s, p);
    }
    return p;
  }

  public SpoudContext<ServiceProviderBeatRawProcessor> processServiceProviderBeat(
      final SpoudContext<ServiceProviderBeatRawProcessor> sc) {
    final Optional<Mapping> m =
        componentWiseMapping
            .getOrDefault(sc.getEvent().getTkNameIdProvider(), Collections.emptyList()).stream()
            .filter(y -> isMeasurePointInMapping(y, sc.getEvent().getMeasurePoint()))
            .findFirst();
    m.ifPresent(x -> sc.getEvent().setTags(m.get().getTags().toArray(new String[] {})));
    return sc;
  }

  public SpoudContext<NfaServiceBeat> processNfaServiceBeat(final SpoudContext<NfaServiceBeat> sc) {
    final Optional<Mapping> m =
        componentWiseMapping.getOrDefault(sc.getEvent().getTkNameId(), Collections.emptyList())
            .stream()
            .filter(y -> isMeasurePointInMapping(y, sc.getEvent().getMeasurePoint()))
            .findFirst();
    m.ifPresent(x -> sc.getEvent().setTags(m.get().getTags().toArray(new String[] {})));
    return sc;
  }

  private boolean isMeasurePointInMapping(final Mapping mapping, final String measurePoint) {
    return mapping.getMeasurePointPattern().matcher(measurePoint).matches();
  }
}
