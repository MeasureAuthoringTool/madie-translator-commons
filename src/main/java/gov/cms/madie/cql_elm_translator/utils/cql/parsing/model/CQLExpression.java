package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

public interface CQLExpression {

  public String getId();

  public void setId(String id);

  public String getName();

  public void setName(String name);

  public String getLogic();

  public void setLogic(String logic);
}
