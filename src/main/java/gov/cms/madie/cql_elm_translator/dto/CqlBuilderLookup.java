package gov.cms.madie.cql_elm_translator.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class CqlBuilderLookup {
  private Set<Lookup> parameters;
  private Set<Lookup> definitions;
  private Set<Lookup> functions;
  private Set<Lookup> fluentFunctions;

  @Data
  @Builder
  public static class Lookup {
    private String name;
    private String libraryName;
    private String libraryAlias;
    private String logic;
    private int startLine;

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      CqlBuilderLookup.Lookup lookup = (CqlBuilderLookup.Lookup) o;

      if (StringUtils.equals(lookup.getName(), this.getName())
          && StringUtils.equals(lookup.getLibraryAlias(), this.getLibraryAlias())
          && lookup.getLibraryName().equals(this.getLibraryName())) {
        return true;
      }
      return false;
    }
  }
}
