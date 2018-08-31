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
package org.activityinfo.i18n.shared;

/**
 * Defines available application locales.
 */
public enum ApplicationLocale {

    // ORDER:
    // 1) English first.
    // 2) All remaining languages with Latin script, sorted alphabetically
    // 3) Languages written with Arabic script, sorted alphabetically
    // 4) Additional writing systems?

    EN("English"),
    ES("Español"),
    EL("Ελληνικά"),
    FR("Français"),
    NL("Nederlands"),
    RU("русский"),
    TR("Türkçe"),
    VI("Vietnamese"),
    AR("العربية"),
    FA("فارسی");

    private String localizedName;

    ApplicationLocale(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getLocalizedName() {
        return localizedName;
    }
    
    public String getCode() {
        return name().toLowerCase();
    }

    public static ApplicationLocale fromCode(String localeCode) {
        return valueOf(localeCode.toUpperCase());
    }
}
