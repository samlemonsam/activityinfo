package org.activityinfo.i18n.tools.model;

/**
 * Key defined in a ResourceClass
 */
public class ResourceClassTerm implements Term {
  private String key;
  private String defaultTranslation;
  private String meaning;

  public ResourceClassTerm(String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  public String getDefaultTranslation() {
    return defaultTranslation;
  }

  public void setDefaultTranslation(String defaultTranslation) {
    this.defaultTranslation = defaultTranslation;
  }

  public String getMeaning() {
    return meaning;
  }

  public void setMeaning(String meaning) {
    this.meaning = meaning;
  }

  @Override
  public String toString() {
    return key + "[" + defaultTranslation + "]";
  }
}
