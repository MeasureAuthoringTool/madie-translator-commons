package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import gov.cms.madie.cql_elm_translator.service.CqlLibraryService;
import gov.cms.mat.cql.elements.UsingProperties;
import kotlinx.io.Source;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.utils.SourceKt;
import org.hl7.elm.r1.VersionedIdentifier;

import java.util.*;

@Slf4j
public class MadieLibrarySourceProvider implements LibrarySourceProvider {

  private static final String[] STRING_ARR = new String[0];
  private static final ThreadLocal<UsingProperties> threadLocalValue = new ThreadLocal<>();
  private static final ThreadLocal<List<UsingProperties>> threadLocalAllUsings =
      new ThreadLocal<>();
  private static final ThreadLocal<String> threadLocalValueAccessToken = new ThreadLocal<>();
  private static CqlLibraryService cqlLibraryService;

  public static String getAccessToken() {
    return threadLocalValueAccessToken.get();
  }

  public static UsingProperties getUsingProperties() {
    return UsingProperties.builder()
        .libraryType(threadLocalValue.get().getLibraryType())
        .version(threadLocalValue.get().getVersion())
        .line(threadLocalValue.get().getLine())
        .comment(threadLocalValue.get().getComment())
        .build();
  }

  public static List<UsingProperties> getAllUsingProperties() {
    return Optional.ofNullable(threadLocalAllUsings.get()).orElse(Collections.emptyList()).stream()
        .map(
            usingProperties ->
                UsingProperties.builder()
                    .libraryType(usingProperties.getLibraryType())
                    .version(usingProperties.getVersion())
                    .line(usingProperties.getLine())
                    .comment(usingProperties.getComment())
                    .build())
        .toList();
  }

  public static void setCqlLibraryService(CqlLibraryService cqlLibraryService) {
    MadieLibrarySourceProvider.cqlLibraryService = cqlLibraryService;
  }

  public static void setUsing(UsingProperties usingProperties) {
    threadLocalValue.set(usingProperties);
  }

  public static void setAllUsings(List<UsingProperties> allUsings) {
    threadLocalAllUsings.set(allUsings);
  }

  public static void setAccessToken(String accessToken) {
    threadLocalValueAccessToken.set(accessToken);
  }

  @Override
  public Source getLibrarySource(VersionedIdentifier libraryIdentifier) {
    // removed processLibrary as it no longer serves a purpose. The CqlLibraryService already
    // validates using statements
    // in the library against threadLocal using statements.
    return getInputStream(libraryIdentifier);
  }

  private Source getInputStream(VersionedIdentifier libraryIdentifier) {
    String cql =
        cqlLibraryService.getLibraryCql(
            libraryIdentifier.getId(),
            libraryIdentifier.getVersion(),
            threadLocalValueAccessToken.get());
    return processCqlFromService(libraryIdentifier, cql);
  }

  private Source processCqlFromService(VersionedIdentifier libraryIdentifier, String cql) {
    if (StringUtils.isEmpty(cql)) {
      log.debug(
          "Did not find any cql for library id: {}, version: {}",
          libraryIdentifier.getId(),
          libraryIdentifier.getVersion());
      return null;
    } else {
      return SourceKt.asSource(cql);
    }
  }
}
