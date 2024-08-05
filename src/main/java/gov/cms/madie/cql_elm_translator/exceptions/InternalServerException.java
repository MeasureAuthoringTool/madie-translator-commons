package gov.cms.madie.cql_elm_translator.exceptions;

public class InternalServerException extends RuntimeException {

  private static final long serialVersionUID = 6150623013532489034L;

  public InternalServerException(String message) {
    super(message);
  }
}
