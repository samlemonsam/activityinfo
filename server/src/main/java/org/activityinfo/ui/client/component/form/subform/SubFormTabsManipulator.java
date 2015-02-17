package org.activityinfo.ui.client.component.form.subform;
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

import com.google.common.base.Function;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.subform.PeriodSubFormKind;
import org.activityinfo.model.type.subform.SubFormKindRegistry;
import org.activityinfo.model.type.subform.SubformConstants;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author yuriyz on 02/17/2015.
 */
public class SubFormTabsManipulator {

    private final SubFormTabsPresenter presenter;
    private final ResourceLocator resourceLocator;

    private PeriodValue periodValue = null; // if not null then period instance generator is in use

    public SubFormTabsManipulator(@NotNull ResourceLocator resourceLocator) {
        this(resourceLocator, new SubFormTabs());
    }

    public SubFormTabsManipulator(@NotNull ResourceLocator resourceLocator, @NotNull SubFormTabs tabs) {
        this.resourceLocator = resourceLocator;
        this.presenter = new SubFormTabsPresenter(tabs);
    }

    public SubFormTabsManipulator show(FormClass subForm) {
        ReferenceType typeClass = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();
        ResourceId typeClassId = typeClass.getRange().iterator().next();
        QuantityType tabsCountType = (QuantityType) subForm.getField(SubformConstants.TAB_COUNT_FIELD_ID).getType();

        presenter.setTabCountSafely(tabsCountType.getUnits());

        if (PredefinedPeriods.isPeriodId(typeClassId)) {
            generateFormInstanceForPeriod(subForm, typeClassId);
        } else {
            queryFormInstances(typeClassId);
        }
        return this;
    }

    private void generateFormInstanceForPeriod(FormClass subForm, ResourceId typeClassId) {
        this.periodValue = ((PeriodSubFormKind) SubFormKindRegistry.get().getKind(typeClassId)).getPeriod();

        final InstanceGenerator instanceGenerator = new InstanceGenerator(subForm.getId());
        presenter.set(instanceGenerator.generate(periodValue, new Date(), InstanceGenerator.Direction.BACK, presenter.getTabCount()));

        presenter.setMoveButtonClickHandler(new org.activityinfo.ui.client.widget.ClickHandler<SubFormTabsPresenter.ButtonType>() {
            @Override
            public void onClick(SubFormTabsPresenter.ButtonType buttonType) {
                onPeriodMoveButtonClick(buttonType, instanceGenerator);
            }
        });
    }

    private void onPeriodMoveButtonClick(SubFormTabsPresenter.ButtonType buttonType, InstanceGenerator instanceGenerator) {
        switch (buttonType) {
            case NEXT:
                presenter.set(instanceGenerator.next());
                break;
            case FULL_NEXT:
                presenter.set(instanceGenerator.fullNext());
                break;
            case FULL_PREVIOUS:
                presenter.set(instanceGenerator.fullPrevious());
                break;
            case PREVIOUS:
                presenter.set(instanceGenerator.previous());
                break;
            default:
                throw new UnsupportedOperationException("Button type is not supported, type:" + buttonType);
        }
    }

    private void queryFormInstances(ResourceId typeClassId) {
        resourceLocator.queryInstances(new ClassCriteria(typeClassId)).then(new Function<List<FormInstance>, Object>() {
            @Nullable
            @Override
            public Object apply(List<FormInstance> input) {
                presenter.set(input);
                return null;
            }
        });
        presenter.setMoveButtonClickHandler(new org.activityinfo.ui.client.widget.ClickHandler<SubFormTabsPresenter.ButtonType>() {
            @Override
            public void onClick(SubFormTabsPresenter.ButtonType buttonType) {
                // todo
            }
        });
    }

    public SubFormTabsPresenter getPresenter() {
        return presenter;
    }
}
