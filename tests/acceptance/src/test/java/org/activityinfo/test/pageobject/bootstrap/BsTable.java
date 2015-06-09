package org.activityinfo.test.pageobject.bootstrap;
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

import com.google.common.base.Optional;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.api.FluentElement;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;

/**
 * @author yuriyz on 06/09/2015.
 */
public class BsTable {

    private final FluentElement container;

    public BsTable(FluentElement container) {
        this.container = container;
    }

    private Optional<FluentElement> button(String buttonName) {
        return container.find().button(withClass("btn"), withText(buttonName)).firstIfPresent();
    }

    public BsModal newSubmission() {
        button(I18N.CONSTANTS.newText()).get().clickWhenReady();
        return BsModal.find(container);
    }

}
