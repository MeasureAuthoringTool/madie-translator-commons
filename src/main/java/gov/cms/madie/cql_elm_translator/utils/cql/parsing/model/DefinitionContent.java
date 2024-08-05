package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DefinitionContent {
  private String name;
  private String content;
  private List<CQLFunctionArgument> functionArguments;
  private boolean function; // MAT-7450
}
