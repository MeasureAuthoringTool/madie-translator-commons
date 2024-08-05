package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

import java.util.List;

import lombok.Data;

@Data
public class CQLFunction implements CQLExpression {

  private String aliasName;

  /** The id. */
  private String id;

  /** The function name. */
  private String functionName;

  /** The function logic. */
  private String functionLogic;

  /** The argument. */
  private List<CQLFunctionArgument> argument;

  /** The context. */
  private String context = "Patient";

  private String commentString = "";

  private String returnType;

  /**
   * Gets the argument list.
   *
   * @return the argument list
   */
  public List<CQLFunctionArgument> getArgumentList() {
    return argument;
  }

  /**
   * Sets the argument list.
   *
   * @param argumentList the new argument list
   */
  public void setArgumentList(List<CQLFunctionArgument> argumentList) {
    this.argument = argumentList;
  }

  @Override
  public String getName() {
    return getFunctionName();
  }

  @Override
  public void setName(String name) {
    setFunctionName(name);
  }

  @Override
  public String getLogic() {
    return getFunctionLogic();
  }

  @Override
  public void setLogic(String logic) {
    setFunctionLogic(logic);
  }
}
