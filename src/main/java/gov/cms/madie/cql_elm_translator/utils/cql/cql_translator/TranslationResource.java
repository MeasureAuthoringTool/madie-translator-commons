package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import kotlinx.io.Source;
import lombok.Getter;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.CqlCompilerException.ErrorSeverity;
import org.fhir.ucum.UcumService;
import org.hl7.cql.model.NamespaceInfo;
import org.hl7.elm.r1.VersionedIdentifier;

import gov.cms.madie.cql_elm_translator.utils.cql.data.RequestData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranslationResource {
  public enum ModelType {
    FHIR,
    QICore
  }

  private static final MultivaluedMap<String, CqlCompilerOptions.Options> PARAMS_TO_OPTIONS_MAP =
      new MultivaluedHashMap<>() {
        {
          putSingle(
              "date-range-optimization", CqlCompilerOptions.Options.EnableDateRangeOptimization);
          putSingle("annotations", CqlCompilerOptions.Options.EnableAnnotations);
          putSingle("locators", CqlCompilerOptions.Options.EnableLocators);
          putSingle("result-types", CqlCompilerOptions.Options.EnableResultTypes);
          putSingle("detailed-errors", CqlCompilerOptions.Options.EnableDetailedErrors);
          putSingle("disable-list-traversal", CqlCompilerOptions.Options.DisableListTraversal);
          putSingle("disable-list-demotion", CqlCompilerOptions.Options.DisableListDemotion);
          putSingle("disable-list-promotion", CqlCompilerOptions.Options.DisableListPromotion);
          putSingle("enable-interval-demotion", CqlCompilerOptions.Options.EnableIntervalDemotion);
          putSingle(
              "enable-interval-promotion", CqlCompilerOptions.Options.EnableIntervalPromotion);
          putSingle(
              "disable-method-invocation", CqlCompilerOptions.Options.DisableMethodInvocation);
          putSingle("require-from-keyword", CqlCompilerOptions.Options.RequireFromKeyword);

          // Todo Do we even use these consolidated options ?
          put(
              "strict",
              Arrays.asList(
                  CqlCompilerOptions.Options.DisableListTraversal,
                  CqlCompilerOptions.Options.DisableListDemotion,
                  CqlCompilerOptions.Options.DisableListPromotion,
                  CqlCompilerOptions.Options.DisableMethodInvocation));
          put(
              "debug",
              Arrays.asList(
                  CqlCompilerOptions.Options.EnableAnnotations,
                  CqlCompilerOptions.Options.EnableLocators,
                  CqlCompilerOptions.Options.EnableResultTypes));
          put(
              "mat",
              Arrays.asList(
                  CqlCompilerOptions.Options.EnableAnnotations,
                  CqlCompilerOptions.Options.EnableLocators,
                  CqlCompilerOptions.Options.DisableListDemotion,
                  CqlCompilerOptions.Options.DisableListPromotion,
                  CqlCompilerOptions.Options.DisableMethodInvocation));
        }
      };

  @Getter private final ModelManager modelManager;

  @Getter private final LibraryManager libraryManager;

  private String modelType;
  private static final String FHIR = "FHIR";
  private static final String QDM = "QDM";

  public TranslationResource(boolean isFhir) {
    modelManager = new ModelManager();
    if (isFhir) {
      modelManager.resolveModel(FHIR, "4.0.1");
      modelType = FHIR;
    } else {
      modelManager.resolveModel(QDM, "5.6");
      modelType = QDM;
    }
    // MAT-6240: Upgrading to cqframework 3.2.0 introduced a reliance on default options that would
    // include locator even if locator was false
    //   org.cqframework.cql.cql2elm.CqlCompilerOptions.setOptions only adds new options, it doesn't
    // remove
    this.libraryManager = new LibraryManager(modelManager, new CqlCompilerOptions());
  }

  public TranslationResource(ModelManager modelManager, boolean isFhir) {
    this.modelManager = modelManager;
    this.libraryManager = new LibraryManager(modelManager, new CqlCompilerOptions());
    if (isFhir) {
      modelType = FHIR;
    } else {
      modelType = QDM;
    }
  }

  public CqlTranslator buildTranslator(RequestData requestData) {
    return buildTranslator(
        requestData.getCqlAsSource(), requestData.createMap(), requestData.getSourceInfo());
  }

  /*sets the options and calls cql-elm-translator using MatLibrarySourceProvider,
  which helps the translator to fetch the CQL of the included libraries from HAPI FHIR Server*/
  public CqlTranslator buildTranslator(
      Source cqlSource, MultivaluedMap<String, String> params, VersionedIdentifier sourceInfo) {
    try {
      UcumService ucumService = null;
      // MAT-7300: change signature level to overloads ONLY for QICore
      LibraryBuilder.SignatureLevel signatureLevel = LibraryBuilder.SignatureLevel.None;
      log.info("buildTranslator for: " + this.modelType);
      if (FHIR.equalsIgnoreCase(this.modelType)) {
        signatureLevel = LibraryBuilder.SignatureLevel.Overloads;
      }
      List<CqlCompilerOptions.Options> optionsList = new ArrayList<>();

      for (String paramKey : params.keySet()) {
        if (PARAMS_TO_OPTIONS_MAP.containsKey(paramKey)
            && Boolean.parseBoolean(params.getFirst(paramKey))) {
          optionsList.addAll(PARAMS_TO_OPTIONS_MAP.get(paramKey));
        } else if (paramKey.equals("validate-units")
            && Boolean.parseBoolean(params.getFirst(paramKey))) {
          libraryManager.getCqlCompilerOptions().setValidateUnits(true);
        } else if (paramKey.equals("signatures")) {
          signatureLevel = LibraryBuilder.SignatureLevel.valueOf(params.getFirst("signatures"));
        } else if (paramKey.equals("error-severity")) {
          // error-severity can either be Info (default)|Warning|Error
          // If no Error Level is provided libraryManager will default it to INFO
          List<String> severityList = params.get(paramKey);
          if (severityList != null && !severityList.isEmpty()) {
            String severityStr = severityList.get(0);
            try {
              ErrorSeverity severity = ErrorSeverity.valueOf(severityStr);
              libraryManager.getCqlCompilerOptions().setErrorLevel(severity);
            } catch (IllegalArgumentException e) {
              throw new TranslationFailureException("Invalid error level is provided:", e);
            }
          }
        }
      }

      CqlCompilerOptions.Options[] options = optionsList.toArray(new CqlCompilerOptions.Options[0]);

      libraryManager.getLibrarySourceLoader().registerProvider(new MadieLibrarySourceProvider());

      NamespaceInfo nsInfo = null;

      // MAT-7300: change signature level to overloads
      if (FHIR.equalsIgnoreCase(this.modelType)) {
        libraryManager.getCqlCompilerOptions().setSignatureLevel(signatureLevel);
      }
      libraryManager.getCqlCompilerOptions().setOptions(options);
      return CqlTranslator.fromSource(nsInfo, sourceInfo, cqlSource, libraryManager);

    } catch (Exception e) {
      throw new TranslationFailureException("Unable to read request", e);
    }
  }
}
