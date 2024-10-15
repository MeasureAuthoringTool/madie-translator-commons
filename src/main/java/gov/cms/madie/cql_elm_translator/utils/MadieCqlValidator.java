package gov.cms.madie.cql_elm_translator.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.Library;

import gov.cms.madie.cql_elm_translator.exceptions.DuplicateIncludeCqlCompilerException;
import gov.cms.madie.cql_elm_translator.utils.cql.data.SimpleIncludeDef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MadieCqlValidator {

  public void checkNoDuplicateIncludes(CqlTranslator cqlTranslator, Library.Includes includes) {
    if (includes != null && includes.getDef() != null && !includes.getDef().isEmpty()) {
      // check for Include uniqueness

      Map<SimpleIncludeDef, String> uniqueSet =
          includes.getDef().stream()
              .map(includeDef -> new SimpleIncludeDef(includeDef))
              .collect(
                  Collectors.toMap(
                      p -> p, p -> p.getVersion(), (s, a) -> s + "," + a, LinkedHashMap::new));

      uniqueSet.forEach(
          (k, v) -> {
            String[] versionArray = v.split(",");
            int includesLen = versionArray.length;
            log.debug("Map:" + k + " - " + includesLen);

            if (versionArray.length > 1) {
              Set<String> set = Arrays.stream(versionArray).collect(Collectors.toSet());
              int startLine = Integer.parseInt(k.getLocator().split("-")[0].split(":")[0]);
              if (set.size() == 1) {
                cqlTranslator
                    .getExceptions()
                    .add(
                        new DuplicateIncludeCqlCompilerException(
                            k.getPath(),
                            cqlTranslator.getTranslatedLibrary().getIdentifier(),
                            (String) set.toArray()[0],
                            startLine));
              } else {
                cqlTranslator
                    .getExceptions()
                    .add(
                        new DuplicateIncludeCqlCompilerException(
                            k.getPath(),
                            cqlTranslator.getTranslatedLibrary().getIdentifier(),
                            startLine));
              }
            }
          });
    }
  }
}
