package org.activityinfo.i18n.tools.model;

import java.io.IOException;
import java.util.Map;

/**
 * Interface to a source of translations, for example a web-based translation manager.
 */
public interface TranslationSource {

    /**
     *  
     * @return a mapping from key to {@link Term} of all
     * terms present in the project
     */
    Map<String, ? extends Term> fetchTerms() throws IOException;

    /**
     * @return All available translations for the given {@code language}
     */
    TranslationSet fetchTranslations(String language) throws IOException;

}
