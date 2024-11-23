package dev.coms4156.project;

import dev.coms4156.project.exception.BadRequestException;
import dev.coms4156.project.exception.ForbiddenException;
import dev.coms4156.project.exception.InternalServerErrorException;
import dev.coms4156.project.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A unit test class for the Codec Utils class.
 */
@SpringBootTest
public class ExceptionTest {

  private void throwBadRequestException() throws BadRequestException {
    throw new BadRequestException();
  }

  private void throwBadRequestException(String str) throws BadRequestException {
    throw new BadRequestException(str);
  }

  private void throwForbiddenException() throws ForbiddenException {
    throw new ForbiddenException();
  }

  private void throwForbiddenException(String str) throws ForbiddenException {
    throw new ForbiddenException(str);
  }

  private void throwInternalServerErrorException() throws InternalServerErrorException {
    throw new InternalServerErrorException();
  }

  private void throwInternalServerErrorException(String str) throws InternalServerErrorException {
    throw new InternalServerErrorException(str);
  }

  private void throwNotFoundException() throws NotFoundException {
    throw new NotFoundException();
  }

  private void throwNotFoundException(String str) throws NotFoundException {
    throw new NotFoundException(str);
  }

  @Test
  public void testBadRequestException() {
    Assertions.assertThrows(BadRequestException.class, this::throwBadRequestException);
    Assertions.assertThrows(BadRequestException.class, () -> this.throwBadRequestException("Err"));
  }

  @Test
  public void testForbiddenException() {
    Assertions.assertThrows(ForbiddenException.class, this::throwForbiddenException);
    Assertions.assertThrows(ForbiddenException.class, () -> this.throwForbiddenException("Err"));
  }

  @Test
  public void testInternalServerErrorException() {
    Assertions.assertThrows(
        InternalServerErrorException.class,
        this::throwInternalServerErrorException
    );
    Assertions.assertThrows(
        InternalServerErrorException.class, () -> this.throwInternalServerErrorException("Err")
    );
  }

  @Test
  public void testNotFoundException() {
    Assertions.assertThrows(NotFoundException.class, this::throwNotFoundException);
    Assertions.assertThrows(NotFoundException.class, () -> this.throwNotFoundException("Err"));
  }

}
