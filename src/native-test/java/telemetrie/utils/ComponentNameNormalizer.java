package telemetrie.utils;

import io.spoud.mobi.telemetrie.rawdispatcher.process.ServiceProviderBeatRawProcessor;

import java.util.regex.Pattern;

public class ComponentNameNormalizer {

  private static final Pattern uuidPattern =
      Pattern.compile("^\\{?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\}?$");
  private static final Pattern numberPattern = Pattern.compile("^(\\-|\\+)?[0-9]+(?:\\.[0-9]+)?$");
  private static final Pattern digitPattern = Pattern.compile("[0-9]+");
  private static final Pattern uNumberPattern =
      Pattern.compile("U[0-9]{6}", Pattern.CASE_INSENSITIVE);

  public static SpoudContext<ServiceProviderBeatRawProcessor> normalize(
      final SpoudContext<ServiceProviderBeatRawProcessor> input) {
    if (input.getEvent().getTkNameIdProvider() != null
        && digitPattern.matcher(input.getEvent().getTkNameIdProvider()).matches()
        && numberPattern.matcher(input.getEvent().getTkNameIdProvider()).matches()) {
      input.getEvent().setTkNameIdProvider("{generic-Number}");
      if ((input.getEvent().getFkNameProvider() != null
              && numberPattern.matcher(input.getEvent().getFkNameProvider()).matches())
          || (input.getEvent().getFkNameIdProvider() != null
              && numberPattern.matcher(input.getEvent().getFkNameIdProvider()).matches())) {
        input.getEvent().setFkNameProvider("{generic-Number}");
        input.getEvent().setFkNameIdProvider("{generic-Number}");
        if (numberPattern.matcher(input.getEvent().getAppIdProvider()).matches()) {
          input.getEvent().setAppIdProvider("{generic-Number}");
        }
      }
    }
    if (input.getEvent().getTkNameIdConsumer() != null
        && digitPattern.matcher(input.getEvent().getTkNameIdConsumer()).matches()
        && numberPattern.matcher(input.getEvent().getTkNameIdConsumer()).matches()) {
      input.getEvent().setTkNameIdConsumer("{generic-Number}");
      if ((input.getEvent().getFkNameConsumer() != null
              && numberPattern.matcher(input.getEvent().getFkNameConsumer()).matches())
          || (input.getEvent().getFkNameIdConsumer() != null
              && numberPattern.matcher(input.getEvent().getFkNameIdConsumer()).matches())) {
        input.getEvent().setFkNameConsumer("{generic-Number}");
        input.getEvent().setFkNameIdConsumer("{generic-Number}");
        if (numberPattern.matcher(input.getEvent().getAppIdConsumer()).matches()) {
          input.getEvent().setAppIdConsumer("{generic-Number}");
        }
      }
    }

    if (input.getEvent().getTkNameIdConsumer() != null) {
      input
          .getEvent()
          .setTkNameIdConsumer(
              uNumberPattern
                  .matcher(input.getEvent().getTkNameIdConsumer())
                  .replaceAll("{UNumber}"));
    }

    if (input.getEvent().getTkNameIdProvider() != null) {
      input
          .getEvent()
          .setTkNameIdProvider(
              uNumberPattern
                  .matcher(input.getEvent().getTkNameIdProvider())
                  .replaceAll("{UNumber}"));
    }

    if (input.getEvent().getTkNameIdProvider() != null
        && input.getEvent().getTkNameIdProvider().length() == 36
        && uuidPattern.matcher(input.getEvent().getTkNameIdProvider()).matches()) {
      if (input.getEvent().getFkNameProvider() != null
              && input
                  .getEvent()
                  .getTkNameIdProvider()
                  .contains(input.getEvent().getFkNameProvider())
          || (input.getEvent().getFkNameIdProvider() != null
              && input
                  .getEvent()
                  .getTkNameIdProvider()
                  .contains(input.getEvent().getFkNameIdProvider()))) {
        if (input.getEvent().getTkNameIdProvider().contains(input.getEvent().getAppIdProvider())) {
          input.getEvent().setAppIdProvider("{generic-UUID-Part}");
        }
        input.getEvent().setFkNameProvider("{generic-UUID-Part}");
        input.getEvent().setFkNameIdProvider("{generic-UUID-Part}");
      }
      input.getEvent().setTkNameIdProvider("{generic-UUID}");
    }
    if (input.getEvent().getTkNameIdConsumer() != null
        && input.getEvent().getTkNameIdConsumer().length() == 36
        && uuidPattern.matcher(input.getEvent().getTkNameIdConsumer()).matches()) {
      if ((input.getEvent().getFkNameConsumer() != null
              && input
                  .getEvent()
                  .getTkNameIdConsumer()
                  .contains(input.getEvent().getFkNameConsumer()))
          || (input.getEvent().getFkNameIdConsumer() != null
              && input
                  .getEvent()
                  .getTkNameIdConsumer()
                  .contains(input.getEvent().getFkNameIdConsumer()))) {
        if (input.getEvent().getTkNameIdConsumer().contains(input.getEvent().getAppIdConsumer())) {
          input.getEvent().setAppIdConsumer("{generic-UUID-Part}");
        }
        input.getEvent().setFkNameConsumer("{generic-UUID-Part}");
        input.getEvent().setFkNameIdConsumer("{generic-UUID-Part}");
      }
      input.getEvent().setTkNameIdConsumer("{generic-UUID}");
    }
    return input;
  }
}
