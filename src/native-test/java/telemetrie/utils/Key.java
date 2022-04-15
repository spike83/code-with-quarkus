package telemetrie.utils;

import lombok.AllArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class Key {

  private final List<String> parts;
  private final String delimiter;
  private final Method method;

  private static String bytesToHex(final byte[] hash) {
    final StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < hash.length; i++) {
      final String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  @Override
  public String toString() {
    if (method == Method.STRING_CONCAT) {
      return String.join("", parts);
    } else if (method == Method.DELIM_STRING_CONCAT) {
      return String.join(delimiter, parts);
    } else if (method == Method.SHA256HEX) {
      return bytesToHex(getMessageDigestBytes());
    } else if (method == Method.SHA256BASE64) {
      return new String(
          Base64.getEncoder().encode(getMessageDigestBytes()), StandardCharsets.ISO_8859_1);
    } else if (method == Method.UUID) {
      return UUID.nameUUIDFromBytes(getMessageDigestBytes()).toString();
    }
    return null;
  }

  private byte[] getMessageDigestBytes() {
    final MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException e) {
      // Fail fast
      throw new IllegalStateException(e);
    }
    for (final String part : parts) {
      try {
        messageDigest.update(part.getBytes(StandardCharsets.UTF_8.name()));
      } catch (final UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
    }
    return messageDigest.digest();
  }

  public enum Method {
    STRING_CONCAT,
    DELIM_STRING_CONCAT,
    SHA256HEX,
    SHA256BASE64,
    UUID
  }

  public static class Builder {
    private final List<String> parts = new ArrayList<>();
    private String delimiter = "";
    private Method method = Method.DELIM_STRING_CONCAT;

    public Builder withDelimiter(final String delimiter) {
      this.delimiter = delimiter;
      return this;
    }

    public Builder withPart(final String part) {
      if (part == null || part.isEmpty()) {
        return this;
      }
      parts.add(part);
      return this;
    }

    public Builder withPart(final float f) {
      return withPart("" + f);
    }

    public Builder withPart(final int i) {
      return withPart("" + i);
    }

    public Builder withPart(final double d) {
      return withPart("" + d);
    }

    public Builder withMethod(final Method method) {
      this.method = method;
      return this;
    }

    public Key build() {
      return new Key(parts, delimiter, method);
    }
  }
}
