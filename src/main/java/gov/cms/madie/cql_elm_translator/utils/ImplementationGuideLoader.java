package gov.cms.madie.cql_elm_translator.utils;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.fhir.npm.NpmPackageManager;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImplementationGuideLoader {

  public static List<ImplementationGuide> load(String igResourcePattern) {
    List<ImplementationGuide> igs = new ArrayList<>();
    try {
      PathMatchingResourcePatternResolver resourceLoader =
          new PathMatchingResourcePatternResolver(ImplementationGuideLoader.class.getClassLoader());
      org.springframework.core.io.Resource[] resources =
          resourceLoader.getResources(igResourcePattern);

      for (org.springframework.core.io.Resource resource : resources) {
        String fileName = resource.getFilename();
        try {
          igs.add(parseFromInputStream(resource.getInputStream()));
        } catch (Exception e) {
          log.error(
              "Error processing IG file: {}, skipping and continuing with next file.", fileName, e);
        }
      }
    } catch (Exception e) {
      log.error("Error initializing IGs", e);
    }
    return igs;
  }

  public static NpmPackageManager buildPackageManager(
      String fhirCachePath, ImplementationGuide implementationGuide) throws IOException {
    FilesystemPackageCacheManager.Builder fspcmBuilder =
        new FilesystemPackageCacheManager.Builder();
    if (StringUtils.isNotBlank(fhirCachePath)) {
      fspcmBuilder = fspcmBuilder.withCacheFolder(fhirCachePath);
    }
    FilesystemPackageCacheManager fspcm = fspcmBuilder.build();
    return new NpmPackageManager(implementationGuide, fspcm);
  }

  public static ImplementationGuide parseFromInputStream(InputStream inputStream) {
    Resource igResource =
        (Resource) FhirContext.forR4Cached().newJsonParser().parseResource(inputStream);

    VersionConvertor_40_50 convertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
    return (ImplementationGuide) convertor.convertResource(igResource);
  }
}
