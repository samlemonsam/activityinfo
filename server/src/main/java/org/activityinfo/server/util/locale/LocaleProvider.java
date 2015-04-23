package org.activityinfo.server.util.locale;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import org.activityinfo.model.auth.AuthenticatedUser;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Provides
 */
@Singleton
public class LocaleProvider implements Provider<Locale> {

    public static final String DEFAULT_LANGUAGE = "en";

    private final Provider<HttpServletRequest> req;

    @Inject
    public LocaleProvider(Provider<HttpServletRequest> req) {
        super();
        this.req = req;
    }

    @Override
    public Locale get() {
        String language = null;
        if (req.get().getCookies() != null) {
            for (Cookie cookie : req.get().getCookies()) {
                if (cookie.getName().equals(AuthenticatedUser.USER_LOCAL_COOKIE)) {
                    language = cookie.getValue();
                }
            }
        }

        if (language == null) {
            language = languageFromHeader();
        }

        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }

        if (language.equals("en")) {
            return Locale.ENGLISH;
        } else {
            return Locale.FRANCE;
        }
    }

    private String languageFromHeader() {
        String[] acceptLanguages = Strings.nullToEmpty(req.get().getHeader("Accept-Language")).split(",");

        for (String lang : acceptLanguages) {
            if (lang.startsWith("en")) {
                return "en";
            } else if (lang.startsWith("fr")) {
                return "fr";
            }
        }
        return null;
    }
}