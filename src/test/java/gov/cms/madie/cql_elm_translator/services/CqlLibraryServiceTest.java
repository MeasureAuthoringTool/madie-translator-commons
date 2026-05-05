package gov.cms.madie.cql_elm_translator.services;

import gov.cms.madie.cql_elm_translator.exceptions.LibraryResourceLoaderException;
import gov.cms.mat.cql.elements.UsingProperties;
import gov.cms.madie.cql_elm_translator.service.CqlLibraryService;
import gov.cms.madie.cql_elm_translator.utils.cql.cql_translator.MadieLibrarySourceProvider;
import org.cqframework.cql.cql2elm.CqlIncludeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CqlLibraryServiceTest {

  @Mock private RestTemplate restTemplate;

  @InjectMocks CqlLibraryService cqlLibraryService;

  private final HttpHeaders httpHeaders = new HttpHeaders();

  private URI libraryUri;

  private final String cqlLibraryName = "FHIRHelpers";

  private final String cqlLibraryVersion = "1.0.000";

  private final String accessToken = "okta-access-token";

  @BeforeEach
  void setUp() throws URISyntaxException {
    ReflectionTestUtils.setField(
        cqlLibraryService, "madieLibraryService", "https://localhost:9090/api");
    ReflectionTestUtils.setField(cqlLibraryService, "librariesCqlUri", "/cql-libraries/cql");

    httpHeaders.add("Authorization", "okta-access-token");
    libraryUri =
        new URI(
            "https://localhost:9090/api/cql-libraries/cql?name="
                + cqlLibraryName
                + "&version="
                + cqlLibraryVersion);
  }

  private CaffeineCacheManager caffeineCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("cqlLibraries");
    cacheManager.setCaffeine(Caffeine.newBuilder());
    return cacheManager;
  }

  @Test
  void getLibraryCql() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'   \n"
            + "Response Cql String";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");
    when(restTemplate.exchange(
            libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class))
        .thenReturn(new ResponseEntity<>(cql, HttpStatus.OK));
    String responseBody =
        cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    assertTrue(responseBody.contains("Response Cql String"));
  }

  @Test
  void getLibraryCqlWrongModelThrowCqlIncludeException() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");

    String wrongLibrarycql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QDM version '5.6'\n"
            + "Response Cql String";
    when(restTemplate.exchange(
            libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class))
        .thenReturn(new ResponseEntity<>(wrongLibrarycql, HttpStatus.OK));
    assertThrows(
        CqlIncludeException.class,
        () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
  }

  @Test
  void getLibraryCqlWrongVersionThrowCqlIncludeException() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");

    String wrongLibrarycql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '6.0.0'\n"
            + "Response Cql String";
    when(restTemplate.exchange(
            libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class))
        .thenReturn(new ResponseEntity<>(wrongLibrarycql, HttpStatus.OK));
    assertThrows(
        CqlIncludeException.class,
        () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
  }

  @Test
  void getLibraryCqlQICoreAndFHIRAreOK() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");

    String wrongLibrarycql =
        "library QICoreCommon version '1.3.000'\n"
            + "using FHIR version '4.0.1'\n"
            + "Response Cql String";
    when(restTemplate.exchange(
            libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class))
        .thenReturn(new ResponseEntity<>(wrongLibrarycql, HttpStatus.OK));
    String responseBody =
        cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    assertTrue(responseBody.contains("Response Cql String"));
  }

  @Test
  void getLibraryCqlReturnsNull() {
    when(restTemplate.exchange(
            libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
    String responseBody =
        cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    assertNull(responseBody);
  }

  @Test
  void getLibraryCqlWhenLibraryNotFound() {
    HttpClientErrorException response = mock(HttpClientErrorException.NotFound.class);
    doThrow(response)
        .when(restTemplate)
        .exchange(libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    Exception ex =
        assertThrows(
            LibraryResourceLoaderException.class,
            () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
    assertThat(
        ex.getMessage(),
        is(equalTo("Library resource FHIRHelpers version '1.0.000' is not found.")));
  }

  @Test
  void getLibraryCqlReturnsNullWhenConflict() {
    HttpClientErrorException response = mock(HttpClientErrorException.Conflict.class);
    doThrow(response)
        .when(restTemplate)
        .exchange(libraryUri, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    Exception ex =
        assertThrows(
            LibraryResourceLoaderException.class,
            () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
    assertThat(
        ex.getMessage(),
        is(
            equalTo(
                "Multiple libraries found with name: FHIRHelpers, version: 1.0.000, but only one was expected.")));
  }

  @Test
  void getLibraryCqlCachesOnFirstCallAndHitsOnSecond() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    ReflectionTestUtils.setField(cqlLibraryService, "cacheManager", caffeineCacheManager());
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(cql, HttpStatus.OK));

    cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);

    verify(restTemplate, times(1)).exchange(any(URI.class), any(), any(), eq(String.class));
  }

  @Test
  void getLibraryCqlCallsServiceAgainForDifferentVersion() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    ReflectionTestUtils.setField(cqlLibraryService, "cacheManager", caffeineCacheManager());
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(cql, HttpStatus.OK));

    cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    cqlLibraryService.getLibraryCql(cqlLibraryName, "2.0.000", accessToken);

    verify(restTemplate, times(2)).exchange(any(URI.class), any(), any(), eq(String.class));
  }

  @Test
  void getLibraryCqlDoesNotCacheOnNotFound() {
    ReflectionTestUtils.setField(cqlLibraryService, "cacheManager", caffeineCacheManager());
    HttpClientErrorException notFound = mock(HttpClientErrorException.NotFound.class);
    doThrow(notFound)
        .when(restTemplate)
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

    Cache.ValueRetrievalException ex1 =
        assertThrows(
            Cache.ValueRetrievalException.class,
            () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
    assertInstanceOf(LibraryResourceLoaderException.class, ex1.getCause());

    Cache.ValueRetrievalException ex2 =
        assertThrows(
            Cache.ValueRetrievalException.class,
            () -> cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken));
    assertInstanceOf(LibraryResourceLoaderException.class, ex2.getCause());

    verify(restTemplate, times(2)).exchange(any(URI.class), any(), any(), eq(String.class));
  }

  @Test
  void getLibraryCqlWithNoCacheManagerCallsServiceDirectly() {
    String cql =
        "library QICoreCommon version '1.3.000'\n"
            + "using QICore version '4.1.1'\n"
            + "Response Cql String";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(cql, HttpStatus.OK));

    cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);
    cqlLibraryService.getLibraryCql(cqlLibraryName, cqlLibraryVersion, accessToken);

    verify(restTemplate, times(2)).exchange(any(URI.class), any(), any(), eq(String.class));
  }

  @Test
  void testSetUpLibrarySourceProvider() {
    String cql = "library QICoreCommon version '1.3.000'\n" + "using QICore version '4.1.1'";
    cqlLibraryService.setUpLibrarySourceProvider(cql, "ACCESS_TOKEN");
    assertThat(MadieLibrarySourceProvider.getAccessToken(), is(equalTo("ACCESS_TOKEN")));
    UsingProperties usingProperties = MadieLibrarySourceProvider.getUsingProperties();
    assertThat(usingProperties, is(notNullValue()));
    assertThat(usingProperties.getLibraryType(), is(equalTo("QICore")));
    assertThat(usingProperties.getVersion(), is(equalTo("4.1.1")));
  }
}
