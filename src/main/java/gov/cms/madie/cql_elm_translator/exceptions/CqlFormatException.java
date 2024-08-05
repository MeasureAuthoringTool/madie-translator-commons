package gov.cms.madie.cql_elm_translator.exceptions;

public class CqlFormatException extends RuntimeException {
  private static final long serialVersionUID = 6674614957232903629L;

  public CqlFormatException(String message) {
    super(message);
  }
}
