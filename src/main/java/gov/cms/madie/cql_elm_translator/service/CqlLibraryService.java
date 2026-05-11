package gov.cms.madie.cql_elm_translator.service;

import gov.cms.madie.cql_elm_translator.exceptions.LibraryResourceLoaderException;
import gov.cms.madie.cql_elm_translator.utils.cql.cql_translator.MadieLibrarySourceProvider;
import gov.cms.mat.cql.CqlTextParser;
import gov.cms.mat.cql.elements.UsingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cqframework.cql.cql2elm.CqlIncludeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CqlLibraryService {

  private final RestTemplate restTemplate;

  @Autowired(required = false)
  private CacheManager cacheManager;

  @Value("${madie.library.service.baseUrl}")
  private String madieLibraryService;

  @Value("${madie.library.service.cql.uri}")
  private String librariesCqlUri;

  public void setUpLibrarySourceProvider(String cql, String accessToken) {
    MadieLibrarySourceProvider.setUsing(new CqlTextParser(cql).getUsing());
    MadieLibrarySourceProvider.setCqlLibraryService(this);
    MadieLibrarySourceProvider.setAccessToken(accessToken);
  }

  public String getLibraryCql(String name, String version, String accessToken) {
    if (cacheManager != null) {
      try {
        return cacheManager
            .getCache("cqlLibraries")
            .get(name + "_" + version, () -> fetchLibraryCql(name, version, accessToken));
      } catch (Cache.ValueRetrievalException e) {
        throw (RuntimeException) e.getCause();
      }
    }
    return fetchLibraryCql(name, version, accessToken);
  }

  private String fetchLibraryCql(String name, String version, String accessToken) {
    URI uri = buildMadieLibraryServiceUri(name, version);
    log.debug("Getting Madie library: {} ", uri);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", accessToken);

    try {
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
      if (responseEntity.hasBody()) {
        log.debug("Retrieved a valid cqlPayload");
        List<String> supportedLibraries =
            Arrays.stream(
                    MadieLibrarySourceProvider.getSupportedLibrariesMap()
                        .get(
                            MadieLibrarySourceProvider.getUsingProperties()
                                .getLibraryType()
                                .toUpperCase()))
                .toList();

        UsingProperties libraryUsing = new CqlTextParser(responseEntity.getBody()).getUsing();
        if (validateUsingStatements(libraryUsing)) {
          return responseEntity.getBody();
        }
        log.error("Library model and version does not match the Measure model and version");
        throw new CqlIncludeException(
            String.format(
                "Library model and version does not match the Measure model and version for"
                    + " name: %s, version: %s",
                name, version),
            null,
            name,
            version,
            null);

      } else {
        log.error("Cannot find Cql payload in the response");
        return null;
      }
    } catch (HttpClientErrorException.NotFound ex) {
      String message =
          String.format("Library resource %s version '%s' is not found.", name, version);
      log.error(message);
      throw new LibraryResourceLoaderException(message);
    } catch (HttpClientErrorException.Conflict ex) {
      String message =
          String.format(
              "Multiple libraries found with name: %s, version: %s, but only one was expected.",
              name, version);
      log.error(message);
      throw new LibraryResourceLoaderException(message);
    }
  }

  private boolean validateUsingStatements(UsingProperties libraryUsing) {
    String[] includeStatement = libraryUsing.getLine().split(" ");
    String[] measureProperties =
        MadieLibrarySourceProvider.getUsingProperties().getLine().split(" ");
    // QICore
    if ((measureProperties[1].equals("QICore")
            && includeStatement[1].equals("FHIR")
            && includeStatement[3].equals("'4.0.1'"))
        || (includeStatement[1].equals("QICore")
            && measureProperties[1].equals("QICore")
            && includeStatement[3].equals(measureProperties[3]))) {
      return true;
    }
    // QDM -> model & version need to match
    if (libraryUsing
        .getLine()
        .equals(MadieLibrarySourceProvider.getUsingProperties().getLine().trim())) {
      return true;
    }
    return false;
  }

  private URI buildMadieLibraryServiceUri(String name, String version) {
    return UriComponentsBuilder.fromHttpUrl(madieLibraryService + librariesCqlUri)
        .queryParam("name", name)
        .queryParam("version", version)
        .build()
        .encode()
        .toUri();
  }
}
