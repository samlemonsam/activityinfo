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
package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.analysis.pivot.ImmutableMeasureModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.model.analysis.pivot.Statistic;
import org.activityinfo.ui.client.icons.IconBundle;

public class CountDistinctNode extends MeasureTreeNode {

    private ResourceId rootFormId;
    private FormClass formClass;

    public CountDistinctNode(ResourceId rootFormId, FormClass formClass) {
        this.rootFormId = rootFormId;
        this.formClass = formClass;
    }

    @Override
    public String getId() {
        return formClass.getId().toString() + ":distinct";
    }

    @Override
    public String getLabel() {
        return I18N.MESSAGES.countDistinctMeasure(formClass.getLabel());
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.count();
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
            .label(I18N.MESSAGES.countDistinctMeasure(formClass.getLabel()))
            .formId(rootFormId)
            .formula(new CompoundExpr(formClass.getId(), ColumnModel.RECORD_ID_SYMBOL).asExpression())
            .addStatistics(Statistic.COUNT_DISTINCT)
            .build();
    }
}
