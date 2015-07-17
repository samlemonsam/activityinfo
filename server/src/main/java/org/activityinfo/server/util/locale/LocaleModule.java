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

import com.google.inject.AbstractModule;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.i18n.shared.UiMessages;

import java.util.Locale;

public class LocaleModule extends AbstractModule {

    public LocaleModule() {
        LocaleProxy.initialize();
    }

    @Override
    protected void configure() {
        bind(UiConstants.class).toProvider(new LocalizableResourceProvider<>(UiConstants.class));
        bind(UiMessages.class).toProvider(new LocalizableResourceProvider<>(UiMessages.class));
        bind(Locale.class).toProvider(LocaleProvider.class);
    }
}
