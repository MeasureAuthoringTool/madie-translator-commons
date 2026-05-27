package gov.cms.madie.cql_elm_translator.utils;

import gov.cms.mat.cql.elements.UsingProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FhirUtil {
  // Future-proof for additional FHIR model support
  private static final ModelNode FHIR = new ModelNode("FHIR", null);
  private static final ModelNode USCORE = new ModelNode("USCORE", FHIR);
  private static final ModelNode QICORE = new ModelNode("QICORE", USCORE);
  private static final ModelNode USQUALITYCORE = new ModelNode("USQUALITYCORE", USCORE);
  private static final Map<String, ModelNode> MODEL_MAP = new HashMap<>();

  private static final Map<String, String> MODEL_VERSION_MAP = new HashMap<>();

  static {
    MODEL_MAP.put(FHIR.getName(), FHIR);
    MODEL_MAP.put(USCORE.getName(), USCORE);
    MODEL_MAP.put(QICORE.getName(), QICORE);
    MODEL_MAP.put(USQUALITYCORE.getName(), USQUALITYCORE);

    MODEL_VERSION_MAP.put(USCORE.getName(), "7.0.0");
    MODEL_VERSION_MAP.put(QICORE.getName(), "7.0.0");
    MODEL_VERSION_MAP.put(USQUALITYCORE.getName(), "0.1.0");
  }

  /**
   * Checks if the given model string is a supported FHIR model or its descendant.
   *
   * @param model the model string to check
   * @return true if the model is a supported FHIR model or descendant, false otherwise
   */
  public boolean isFhirModel(String model) {
    if (model == null) {
      return false;
    }
    String normalized = model.trim().toUpperCase();
    ModelNode node = MODEL_MAP.get(normalized);
    if (node == null) {
      return false;
    }
    return node.isOrIsDescendantOf("FHIR");
  }

  /**
   * Given a list of UsingProperties, returns the most specific one based on model tree depth. If
   * none match, returns null.
   *
   * @param usingPropertiesList List of UsingProperties
   * @return The most specific UsingProperties or null if none found
   */
  public UsingProperties getMostSpecificFhirModel(List<UsingProperties> usingPropertiesList) {
    if (usingPropertiesList == null || usingPropertiesList.isEmpty()) {
      return null;
    }
    UsingProperties mostSpecific = null;
    int maxDepth = -1;
    for (UsingProperties using : usingPropertiesList) {
      if (using == null) {
        continue;
      }
      String type = using.getLibraryType();
      if (type != null) {
        ModelNode node = MODEL_MAP.get(type.trim().toUpperCase());
        if (node != null && node.isOrIsDescendantOf("FHIR")) {
          int depth = getDepth(node);
          if (depth > maxDepth) {
            maxDepth = depth;
            mostSpecific = using;
          }
        }
      }
    }
    return mostSpecific;
  }

  public String getMinVersionForNpm(UsingProperties usingProperty) {
    if (usingProperty == null) {
      return null;
    }
    String type = usingProperty.getLibraryType();
    if (type == null) {
      return null;
    }
    ModelNode node = MODEL_MAP.get(type.trim().toUpperCase());
    if (node != null && node.isOrIsDescendantOf("FHIR")) {
      return MODEL_VERSION_MAP.get(node.getName());
    }
    return null;
  }

  /**
   * Extracts the normalized model string from a model/version field. For example, "QI-Core v4.1.1"
   * or "QI-Core-4.1.1" returns "QICORE". "FHIR4.0.1" returns "FHIR". "QDM-5.6" returns "QDM".
   *
   * @param modelVersion The model/version string from a Measure or CqlLibrary
   * @return The normalized model string (e.g., "QICORE", "FHIR", "QDM"), or null if not found
   */
  public static String extractModelString(String modelVersion) {
    if (modelVersion == null || modelVersion.isEmpty()) {
      return null;
    }
    char[] chars = modelVersion.toCharArray();
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < chars.length) {
      char c = chars[i];
      if (Character.isLetter(c)) {
        // Check if this is a version prefix: a lone 'v'/'V' after a separator, followed by a digit
        if ((c == 'v' || c == 'V') && sb.length() > 0) {
          int next = i + 1;
          if (next < chars.length && Character.isDigit(chars[next])) {
            break; // version indicator, stop collecting
          }
        }
        sb.append(c);
        i++;
      } else if (c == '-' || c == ' ' || c == '_') {
        i++; // skip separators
      } else {
        break; // digit or other non-letter, non-separator: stop
      }
    }
    return sb.length() > 0 ? sb.toString().toUpperCase() : null;
  }

  /**
   * Checks whether a library model is compatible with a measure model. A library model is
   * compatible if the measure model is an ancestor of (or equal to) the library's most specific
   * model. For example, a FHIR or USCore library is compatible with a QICore measure, but a QICore
   * library is not compatible with a USQualityCore measure.
   *
   * @param measureModel the normalized model name of the measure (e.g. "QICORE")
   * @param libraryModel the normalized model name of the library (e.g. "FHIR")
   * @return true if the measure model is an ancestor-or-equal of the library model
   */
  public boolean isMeasureCompatibleWithLibrary(String measureModel, String libraryModel) {
    if (measureModel == null || libraryModel == null) {
      return false;
    }
    ModelNode measureNode = MODEL_MAP.get(measureModel.trim().toUpperCase());
    if (measureNode == null) {
      return false;
    }
    return measureNode.isOrIsDescendantOf(libraryModel.trim().toUpperCase());
  }

  private int getDepth(ModelNode node) {
    int depth = 0;
    ModelNode checkNode = node;
    while (checkNode.getParent() != null) {
      depth++;
      checkNode = checkNode.getParent();
    }
    return depth;
  }
}
