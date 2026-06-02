package gov.cms.madie.cql_elm_translator.service;

import gov.cms.madie.cql_elm_translator.exceptions.LibraryResourceLoaderException;
import gov.cms.madie.cql_elm_translator.utils.FhirUtil;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CqlLibraryService {

  private final RestTemplate restTemplate;
  private final FhirUtil fhirUtil;

  @Autowired(required = false)
  private CacheManager cacheManager;

  @Value("${madie.library.service.baseUrl}")
  private String madieLibraryService;

  @Value("${madie.library.service.cql.uri}")
  private String librariesCqlUri;

  public void setUpLibrarySourceProvider(String cql, String accessToken) {
    CqlTextParser cqlTextParser = new CqlTextParser(cql);
    MadieLibrarySourceProvider.setUsing(cqlTextParser.getUsing());
    MadieLibrarySourceProvider.setAllUsings(cqlTextParser.getAllUsings());
    MadieLibrarySourceProvider.setCqlLibraryService(this);
    MadieLibrarySourceProvider.setAccessToken(accessToken);
  }

  public String getLibraryCql(String name, String version, String accessToken) {
    String cql = getRawLibraryCql(name, version, accessToken);
    if (cql != null) {
      List<UsingProperties> allUsings = new CqlTextParser(cql).getAllUsings();
      if (!validateUsingStatements(allUsings)) {
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
      }
    }
    return cql;
  }

  private String getRawLibraryCql(String name, String version, String accessToken) {
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
        return responseEntity.getBody();
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

  public boolean validateUsingStatements(List<UsingProperties> libraryAllUsings) {
    List<UsingProperties> measureAllUsings = MadieLibrarySourceProvider.getAllUsingProperties();

    // Determine the most specific FHIR model for the measure and library
    UsingProperties measureMostSpecific = fhirUtil.getMostSpecificFhirModel(measureAllUsings);
    boolean measureIsFhir = measureMostSpecific != null;

    UsingProperties libMostSpecific = fhirUtil.getMostSpecificFhirModel(libraryAllUsings);
    boolean libraryIsFhir = libMostSpecific != null;

    // If one is FHIR-based and the other is QDM, they are incompatible
    if (measureIsFhir != libraryIsFhir) {
      return false;
    }

    if (!measureIsFhir) {
      // Both are QDM: confirm library has a QDM using that matches the measure's QDM type
      UsingProperties measureQdm = measureAllUsings.get(0);
      UsingProperties libraryQdm = libraryAllUsings.get(0);
      if (measureQdm == null || libraryQdm == null) {
        return false;
      }

      String measureType = measureQdm.getLibraryType();
      String libraryType = libraryQdm.getLibraryType();
      return measureType != null
          && libraryType != null
          && measureType.trim().equalsIgnoreCase(libraryType.trim());
    }

    // Both are FHIR-based: enforce that any overlapping model names share the same version
    // (e.g. you cannot mix QICore 4.1.1 in the measure with QICore 6.0.0 in the library)
    if (!fhirUtil.fhirModelVersionsAreConsistent(measureAllUsings, libraryAllUsings)) {
      return false;
    }

    // The library model must be an ancestor-or-equal of the measure's most specific model
    // (e.g. a QICore measure can use a USCore or FHIR library, but not USQualityCore)
    String measureModelName = measureMostSpecific.getLibraryType().trim().toUpperCase();
    String libModelName = libMostSpecific.getLibraryType().trim().toUpperCase();
    return fhirUtil.isMeasureCompatibleWithLibrary(measureModelName, libModelName);
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
