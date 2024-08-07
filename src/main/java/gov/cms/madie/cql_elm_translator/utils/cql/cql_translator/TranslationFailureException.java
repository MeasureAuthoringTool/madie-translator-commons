package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TranslationFailureException extends RuntimeException {
  // Todo Is it helpful ?
  public TranslationFailureException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
