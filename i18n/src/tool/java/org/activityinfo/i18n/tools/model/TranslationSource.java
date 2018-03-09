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
