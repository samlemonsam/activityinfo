package org.activityinfo.ui.client.component.importDialog.validation.cells;
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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.core.shared.importing.strategy.ColumnAccessor;
import org.activityinfo.core.shared.importing.validation.ValidatedRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.importDialog.validation.ValidationPageStyles;

/**
 * @author yuriyz on 5/5/14.
 */
public class ValidationResultCell extends AbstractCell<ValidatedRow> {

    public static interface Templates extends SafeHtmlTemplates {

        public static final Templates INSTANCE = GWT.create(Templates.class);

        @Template("<div class='{0}' title='{1}'>&nbsp;{2}</div>")
        public SafeHtml html(String style, String tooltip, String text);
    }


    private final ColumnAccessor accessor;
    private final int columnIndex;

    public ValidationResultCell(ColumnAccessor accessor, int columnIndex) {
        super();
        this.accessor = accessor;
        this.columnIndex = columnIndex;
    }

    @Override
    public void render(Context context, ValidatedRow data, SafeHtmlBuilder sb) {
        ValidationResult result = data.getResult(columnIndex);
        final SafeHtml safeHtml = Templates.INSTANCE.html(style(result), tooltip(result), accessor.getValue(data.getSourceRow()));
        sb.append(safeHtml);
    }

    private static String tooltip(ValidationResult result) {
        if (result.getState() == ValidationResult.State.CONFIDENCE && result.getConfidence() < 1) {
            int confidencePercent = (int) (result.getConfidence() * 100);
            return I18N.MESSAGES.confidence(confidencePercent);
        }
        return "";
    }

    private static String style(ValidationResult result) {
        if (result != null) {
            switch (result.getState()) {
                case OK:
                    return ValidationPageStyles.INSTANCE.stateOk();
                case CONFIDENCE:
                    if (result.getConfidence() == 1) {
                        return ValidationPageStyles.INSTANCE.stateOk();
                    }
                    return ValidationPageStyles.INSTANCE.stateConfidence();
                case ERROR:
                case MISSING:
                    if (!result.hasReferenceMatch()) {
                        return ValidationPageStyles.INSTANCE.stateError();
                    }
            }
        }
        return "";
    }
}
