package dev.coms4156.project.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This is the global exception handler for the service.
 * Citation:
 * <a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc">spring</a>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private Map<String, String> errorResponse(String message) {
    Map<String, String> response = new HashMap<>();
    response.put("status", "failed");
    response.put("message", message);
    return response;
  }

  private Map<String, String> errorResponse(Exception ex) {
    return this.errorResponse(ex.getMessage());
  }

  private Map<String, String> errorResponse(Exception ex, String title) {
    return this.errorResponse(title + ": " + ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleItemNotFoundException(NotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(this.errorResponse(ex));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<?> handleForbiddenException(ForbiddenException ex) {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(this.errorResponse(ex));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(this.errorResponse(ex));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(this.errorResponse(ex, "Bad Request"));
  }

  /**
   * Exception handler to catch Internal Server Error exceptions thrown by the application.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception ex) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(this.errorResponse(ex, "Internal Server Error"));
  }


}
