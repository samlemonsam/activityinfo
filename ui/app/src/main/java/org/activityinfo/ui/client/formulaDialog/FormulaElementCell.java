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
package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FormulaElementCell extends AbstractCell<FormulaElement> {


    interface CellTemplates extends SafeHtmlTemplates {

        @Template("<span class=\"{0}\">{1}</span> {2}")
        SafeHtml cellWithCode(String formulaClass, String code, String label);
    }

    private static final CellTemplates TEMPLATES = GWT.create(CellTemplates.class);


    @Override
    public void render(Context context, FormulaElement formulaElement, SafeHtmlBuilder builder) {

        if(formulaElement.hasCode()) {
            builder.append(TEMPLATES.cellWithCode(FormulaResources.INSTANCE.styles().fieldTreeCode(),
                    formulaElement.getCode(),
                    formulaElement.getLabel()));
        } else {
            builder.appendEscaped(formulaElement.getLabel());
        }
    }
}
