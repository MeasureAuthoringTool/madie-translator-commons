package gov.cms.madie.cql_elm_translator.config;

import gov.cms.madie.cql_elm_translator.utils.cql.cql_translator.MadieLibrarySourceProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(CqlLibrariesCacheConfig.class)
public class MadieTranslatorCommonsAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public SpringContextHolder springContextHolder() {
    return new SpringContextHolder();
  }

  @Bean
  @ConditionalOnMissingBean
  public MadieLibrarySourceProvider madieLibrarySourceProvider() {
    return new MadieLibrarySourceProvider();
  }
}
