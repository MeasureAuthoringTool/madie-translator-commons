package gov.cms.madie.cql_elm_translator.utils;

import gov.cms.madie.cql_elm_translator.exceptions.InternalServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class ResourceUtils extends StreamUtils {
  public static String getData(String resource) {
    try (InputStream inputStream = getStream(resource)) {
      if (inputStream == null) {
        throw new InternalServerException("Unable to fetch resource " + resource);
      }
      return new String(inputStream.readAllBytes());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (NullPointerException nullPointerException) {
      throw new InternalServerException("Resource name cannot be null");
    }
  }
}
