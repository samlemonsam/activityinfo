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
package org.activityinfo.api.tools;

import java.util.Arrays;
import java.util.List;

/**
 * Root object passed to the template generator
 */
public class DocModel {

    /**
     * General topics written in markdown and read from src/main/content
     */
    private String topics;

    /**
     * The OpenAPI specification model
     */
    private SpecModel spec;

    /**
     * Programming languages for which we provide examples
     */
    private List<String> languages;

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public SpecModel getSpec() {
        return spec;
    }

    public void setSpec(SpecModel spec) {
        this.spec = spec;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(String... languages) {
        this.languages = Arrays.asList("shell", "R");
    }
}
