package gov.cms.madie.cql_elm_translator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CqlLibraryDetails {
  private String cql;
  private String libraryName;
  private Set<String> expressions;
}
