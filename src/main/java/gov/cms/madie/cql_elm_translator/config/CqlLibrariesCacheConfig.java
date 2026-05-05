package gov.cms.madie.cql_elm_translator.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CqlLibrariesCacheConfig {

  @Bean(name = "madieCqlLibrariesCacheManager")
  public CaffeineCacheManager madieCqlLibrariesCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCacheNames(List.of("cqlLibraries"));
    cacheManager.setCaffeine(
        Caffeine.newBuilder().maximumSize(2_000).expireAfterWrite(Duration.ofMinutes(30)));
    return cacheManager;
  }
}
