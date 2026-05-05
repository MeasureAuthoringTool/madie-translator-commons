package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import gov.cms.madie.cql_elm_translator.service.CqlLibraryService;
import gov.cms.mat.cql.elements.UsingProperties;
import kotlinx.io.Source;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.utils.SourceKt;
import org.hl7.elm.r1.VersionedIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MadieLibrarySourceProvider implements LibrarySourceProvider {

  private static final String[] STRING_ARR = new String[0];
  private static final ThreadLocal<UsingProperties> threadLocalValue = new ThreadLocal<>();
  private static final ThreadLocal<String> threadLocalValueAccessToken = new ThreadLocal<>();
  private static CqlLibraryService cqlLibraryService;
  private static CacheManager cacheManager;
  private static final Map<String, String[]> supportedLibrariesMap =
      Map.of(
          "FHIR", List.of("FHIR").toArray(STRING_ARR),
          "QICORE", List.of("FHIR", "QICore").toArray(STRING_ARR),
          "QDM", List.of("QDM").toArray(STRING_ARR));

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

  @Autowired(required = false)
  public void initCqlLibraryService(CqlLibraryService cqlLibraryService) {
    if (cqlLibraryService != null) {
      setCqlLibraryService(cqlLibraryService);
    }
  }

  @Autowired(required = false)
  public void initCacheManager(
      @org.springframework.beans.factory.annotation.Qualifier("madieCqlLibrariesCacheManager")
          CacheManager cacheManager) {
    if (cacheManager != null) {
      MadieLibrarySourceProvider.cacheManager = cacheManager;
    }
  }

  public static void setCqlLibraryService(CqlLibraryService cqlLibraryService) {
    MadieLibrarySourceProvider.cqlLibraryService = cqlLibraryService;
  }

  public static void setUsing(UsingProperties usingProperties) {
    threadLocalValue.set(usingProperties);
  }

  public static void setAccessToken(String accessToken) {
    threadLocalValueAccessToken.set(accessToken);
  }

  private static String createKey(String name, String qdmVersion, String version) {
    return name + "-" + qdmVersion + "-" + version;
  }

  public static String buildCacheKey(VersionedIdentifier libraryIdentifier) {
    String usingVersion = threadLocalValue.get() == null ? "" : threadLocalValue.get().getVersion();
    return createKey(libraryIdentifier.getId(), usingVersion, libraryIdentifier.getVersion());
  }

  public static Map<String, String[]> getSupportedLibrariesMap() {
    return supportedLibrariesMap;
  }

  /**
   * Inspects the cqlLibraries cache and logs all currently cached keys and whether a specific key
   * is present. Useful for runtime debugging of cache misses.
   *
   * @param key the cache key to probe (use buildCacheKey to construct it)
   */
  public static void debugCache(String key) {
    if (cacheManager == null) {
      log.warn("[CacheDebug] CacheManager not injected — cache is not active.");
      return;
    }
    Cache cache = cacheManager.getCache("cqlLibraries");
    if (cache == null) {
      log.warn(
          "[CacheDebug] 'cqlLibraries' cache not found in CacheManager: {}",
          cacheManager.getClass().getName());
      return;
    }
    log.info("[CacheDebug] CacheManager type: {}", cacheManager.getClass().getName());
    log.info(
        "[CacheDebug] Underlying native cache type: {}",
        cache.getNativeCache().getClass().getName());
    // For Caffeine, we can cast to com.github.benmanes.caffeine.cache.Cache to inspect keys
    Object nativeCache = cache.getNativeCache();
    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
      log.info("[CacheDebug] All cached keys: {}", caffeineCache.asMap().keySet());
      log.info("[CacheDebug] Cache stats: {}", caffeineCache.stats());
      log.info(
          "[CacheDebug] Entry for key [{}]: present={}",
          key,
          caffeineCache.asMap().containsKey(key));
    } else {
      log.info("[CacheDebug] Spring cache.get({}) = {}", key, cache.get(key));
    }
  }

  @Override
  @Cacheable(
      cacheNames = "cqlLibraries",
      cacheManager = "madieCqlLibrariesCacheManager",
      key =
          "T(gov.cms.madie.cql_elm_translator.utils.cql.cql_translator.MadieLibrarySourceProvider)"
              + ".buildCacheKey(#libraryIdentifier)")
  public Source getLibrarySource(VersionedIdentifier libraryIdentifier) {
    String key = buildCacheKey(libraryIdentifier);
    log.info(
        "[CacheMiss] getLibrarySource body executing — CACHE MISS for key [{}] on bean [{}]",
        key,
        this.getClass().getName());
    debugCache(key);
    return processLibrary(libraryIdentifier, key);
  }

  private Source processLibrary(VersionedIdentifier libraryIdentifier, String key) {
    String[] supportedLibraries =
        supportedLibrariesMap.get(threadLocalValue.get().getLibraryType().toUpperCase());
    if (Arrays.stream(supportedLibraries)
        .anyMatch(threadLocalValue.get().getLibraryType()::contains)) {
      return getInputStream(libraryIdentifier, key);
    } else {
      throw new IllegalArgumentException(
          String.format("%s is not supported.", threadLocalValue.get().getLibraryType()));
    }
  }

  private Source getInputStream(VersionedIdentifier libraryIdentifier, String key) {
    String cql =
        cqlLibraryService.getLibraryCql(
            libraryIdentifier.getId(),
            libraryIdentifier.getVersion(),
            threadLocalValueAccessToken.get());
    return processCqlFromService(key, cql);
  }

  private Source processCqlFromService(String key, String cql) {
    if (StringUtils.isEmpty(cql)) {
      log.debug("Did not find any cql for key : {}", key);
      return null;
    } else {
      return SourceKt.asSource(cql);
    }
  }
}
