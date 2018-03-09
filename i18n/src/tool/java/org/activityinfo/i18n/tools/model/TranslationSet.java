/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
