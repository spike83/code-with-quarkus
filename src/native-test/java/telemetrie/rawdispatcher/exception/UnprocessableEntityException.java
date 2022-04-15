package telemetrie.rawdispatcher.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UnprocessableEntityException extends Throwable {

  ObjectNode node;

  public UnprocessableEntityException(final String message, final Throwable cause,
      ObjectNode node) {
    super(message, cause);
    this.node = node;
  }

  @Override
  public String toString() {
    return "UnprocessableEntityException{" +
        "cause=" + getCause() +
        "message=" + getMessage() +
        "node=" + node +
        '}';
  }
}
