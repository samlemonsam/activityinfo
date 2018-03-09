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
