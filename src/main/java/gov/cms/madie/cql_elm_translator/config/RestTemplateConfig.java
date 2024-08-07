package gov.cms.madie.cql_elm_translator.config;

import gov.cms.madie.cql_elm_translator.config.logging.RequestResponseLoggingMdcInternalInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

  private final RequestResponseLoggingMdcInternalInterceptor
      requestResponseLoggingMdcInternalInterceptor;

  public RestTemplateConfig(
      RequestResponseLoggingMdcInternalInterceptor requestResponseLoggingMdcInternalInterceptor) {
    this.requestResponseLoggingMdcInternalInterceptor =
        requestResponseLoggingMdcInternalInterceptor;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    ClientHttpRequestFactory factory =
        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    RestTemplate restTemplate = new RestTemplate(factory);
    restTemplate.setInterceptors(List.of(requestResponseLoggingMdcInternalInterceptor));
    return restTemplate;
  }
}
