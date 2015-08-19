package org.activityinfo.i18n.tools.model;

/**
 * A translated term
 */
public interface TranslatedTerm extends Term {
    
    String getTranslation();
    
    boolean isTranslated();
}
