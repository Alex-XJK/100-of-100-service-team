package dev.coms4156.project.interceptor;

import dev.coms4156.project.exception.BadRequestException;
import dev.coms4156.project.exception.ForbiddenException;
import dev.coms4156.project.utils.CodecUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * This class decodes the client ID parameter in the request.
 * Citation:
 * I was inspired by the following blog post to decode parameter in Java:
 * <a href="https://medium.com/@snyksec/secure-java-url-encoding-and-decoding-1e8b3eac669b">
 *   decode</a>
 */
@Component
public class ParameterDecodingInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(ParameterDecodingInterceptor.class);
  private static final String CLIENT_ID = "cid";

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) {
    String originalCid = request.getParameter(CLIENT_ID);
    if (originalCid == null) {
      logger.warn("Client ID is required.");
      throw new ForbiddenException("Client ID is required.");
    }

    String decodedCid;
    try {
      decodedCid = CodecUtils.decode(originalCid);
    } catch (IllegalArgumentException e) {
      logger.warn("Failed to decode [{}].", originalCid);
      throw new BadRequestException("Invalid client ID [" + originalCid + "]");
    }
    request.setAttribute(CLIENT_ID, decodedCid);
    logger.info("Decode client id from [{}] to [{}].", originalCid, decodedCid);

    return true;
  }

}
