package gov.cms.madie.cql_elm_translator.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;

import org.cqframework.cql.tools.formatter.CqlFormatterVisitor;

import gov.cms.madie.cql_elm_translator.exceptions.CqlFormatException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormatUtil {

  public String formatCql(String cqlData, Principal principal) {
    try (var cqlDataStream = new ByteArrayInputStream(cqlData.getBytes())) {
      CqlFormatterVisitor.FormatResult formatResult =
          CqlFormatterVisitor.getFormattedOutput(cqlDataStream);
      if (formatResult.getErrors() != null && !formatResult.getErrors().isEmpty()) {
        log.info("User [{}] requested to format the CQL, but errors found", principal.getName());
        throw new CqlFormatException(
            "Unable to format CQL, because one or more Syntax errors are identified");
      }
      log.info("User [{}] successfully formatted the CQL", principal.getName());
      return formatResult.getOutput();
    } catch (IOException e) {
      log.info("User [{}] is unable to format the CQL", principal.getName());
      throw new CqlFormatException(e.getMessage());
    }
  }
}
