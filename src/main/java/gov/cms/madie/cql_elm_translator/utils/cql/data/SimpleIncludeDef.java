package gov.cms.madie.cql_elm_translator.utils.cql.data;

import org.hl7.elm.r1.IncludeDef;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SimpleIncludeDef {

  private String mediaType;
  private String path;

  @EqualsAndHashCode.Exclude private String version;

  @EqualsAndHashCode.Exclude private String locator;

  public SimpleIncludeDef(IncludeDef includeDef) {

    this.setMediaType(includeDef.getMediaType());
    this.setVersion(includeDef.getVersion());
    this.setPath(includeDef.getPath());
    this.setLocator(includeDef.getLocator());
  }
}
