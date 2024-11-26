package dev.coms4156.project.utils;

import java.util.Base64;

/**
 * This class provides utility methods for encoding and decoding strings.
 */
public final class CodecUtils {
  /**
   * Private constructor to prevent instantiation.
   */
  private CodecUtils() {}

  /**
   * Encodes a string to a URL-encoded string.
   *
   * @param decoded the to-be-encoded string
   * @return the encoded string
   */
  public static String encode(String decoded) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(decoded.getBytes());
  }

  /**
   * Decodes a URL-encoded string.
   *
   * @param encoded the to-be-decoded string
   * @return the decoded string
   * @throws IllegalArgumentException if the input is not a valid Base64 encoding
   */
  public static String decode(String encoded) throws IllegalArgumentException {
    return new String(Base64.getUrlDecoder().decode(encoded));
  }
}