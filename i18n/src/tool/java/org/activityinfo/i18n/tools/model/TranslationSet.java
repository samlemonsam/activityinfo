package org.activityinfo.i18n.tools.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Set of translations in a given language for all the project's terms.
 */
public class TranslationSet {
    
    private String language;
    private Map<String, TranslatedTerm> translations = Maps.newHashMap();
    
    public TranslationSet(String language) {
        this.language = language;
    }
    
    public TranslationSet(String language, List<? extends TranslatedTerm> terms) {
        this.language = language;
        for(TranslatedTerm term : terms) {
            if(term.isTranslated()) {
                translations.put(term.getKey(), term);
            }
        }
    }

    public String getLanguage() {
        return language;
    }
    
    public String get(String key) {
        TranslatedTerm term = translations.get(key);
        if(term == null) {
            return null;
        }
        return term.getTranslation();
    }

    public void add(final String key, final String translation) {
        translations.put(key, new TranslatedTerm() {
            @Override
            public String getTranslation() {
                return translation;
            }

            @Override
            public boolean isTranslated() {
                return true;
            }

            @Override
            public String getKey() {
                return key;
            }
        });
    }

    public boolean isEmpty() {
        return translations.isEmpty();
    }

    public boolean has(String key) {
        return get(key) != null;
    }
}
