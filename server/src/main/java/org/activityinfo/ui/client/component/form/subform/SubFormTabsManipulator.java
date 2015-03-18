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
import com.google.common.base.Preconditions;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.model.type.subform.PeriodSubFormKind;
import org.activityinfo.model.type.subform.SubFormKindRegistry;
import org.activityinfo.model.type.subform.SubformConstants;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.widget.ClickHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

/**
 * @author yuriyz on 02/17/2015.
 */
public class SubFormTabsManipulator {

    private final SubFormTabsPresenter presenter;
    private final ResourceLocator resourceLocator;

    @Nullable
    private PeriodValue periodValue = null; // if not null then period instance generator is in use

    private FormClass subForm;
    private FormModel formModel;
    private FormDesigner formDesigner;

    public SubFormTabsManipulator(@Nonnull ResourceLocator resourceLocator) {
        this.resourceLocator = formDesigner.getResourceLocator();
        this.presenter = new SubFormTabsPresenter(new SubFormTabs());
    }

    public SubFormTabsManipulator(@Nonnull FormDesigner formDesigner, @Nonnull SubFormTabs tabs) {
        this.resourceLocator = formDesigner.getResourceLocator();
        this.formDesigner = formDesigner;
        this.presenter = new SubFormTabsPresenter(tabs);
    }

    public SubFormTabsManipulator show(@Nonnull FormClass subForm, @Nonnull FormModel formModel) {

        Preconditions.checkNotNull(subForm);
        Preconditions.checkNotNull(formModel);

        this.subForm = subForm;
        this.formModel = formModel;

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
        presenter.setPeriodType(PredefinedPeriods.fromPeriod(periodValue));
        presenter.set(instanceGenerator.generate(periodValue, new Date(), InstanceGenerator.Direction.BACK, presenter.getTabCount()));

        presenter.setMoveButtonClickHandler(new org.activityinfo.ui.client.widget.ClickHandler<SubFormTabsPresenter.ButtonType>() {
            @Override
            public void onClick(SubFormTabsPresenter.ButtonType buttonType) {
                onPeriodMoveButtonClick(buttonType, instanceGenerator);
            }
        });
        presenter.setInstanceTabClickHandler(new ClickHandler<FormInstance>() {
            @Override
            public void onClick(FormInstance instance) {
                onPeriodInstanceTabClick(instance);
            }
        });
    }

    private void onPeriodInstanceTabClick(FormInstance instance) {
        formModel.setSelectedInstance(instance, subForm);
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
        presenter.clear();

        Criteria criteria = ClassType.isClassType(typeClassId) ?
                ParentCriteria.isChildOf(typeClassId, formDesigner.getRootFormClass().getId()) :
                new ClassCriteria(typeClassId);

        final InstanceQuery query = new InstanceQuery()
                .setCriteria(criteria)
                .setMaxCount(presenter.getTabCount())
                .setOffset(0);

        queryInstances(query);

        presenter.setMoveButtonClickHandler(new org.activityinfo.ui.client.widget.ClickHandler<SubFormTabsPresenter.ButtonType>() {
            @Override
            public void onClick(SubFormTabsPresenter.ButtonType buttonType) {
                onInstanceMoveButtonClick(buttonType, query);
            }
        });
        presenter.setInstanceTabClickHandler(new ClickHandler<FormInstance>() {
            @Override
            public void onClick(FormInstance instance) {
                // todo : support user-defined form classes
            }
        });
    }

    private void queryInstances(final InstanceQuery query) {
        resourceLocator.queryInstances(query).then(new Function<QueryResult<FormInstance>, Object>() {
            @Nullable
            @Override
            public Object apply(QueryResult<FormInstance> queryResult) {
                presenter.setShowPreviousButtons(query.getOffset() > 0);
                presenter.setShowNextButtons(queryResult.hasNext(query.getOffset()));
                presenter.set(queryResult.getItems());
                return null;
            }
        });
    }

    private void onInstanceMoveButtonClick(SubFormTabsPresenter.ButtonType buttonType, InstanceQuery query) {
        switch (buttonType) {
            case NEXT:
                queryInstances(query.incrementOffsetOn(1));
                break;
            case FULL_NEXT:
                queryInstances(query.incrementOffsetOn(query.getMaxCount()));
                break;
            case FULL_PREVIOUS:
                queryInstances(query.incrementOffsetOn(-query.getMaxCount()));
                break;
            case PREVIOUS:
                queryInstances(query.incrementOffsetOn(-1));
                break;
            default:
                throw new UnsupportedOperationException("Button type is not supported, type:" + buttonType);
        }
    }

    public SubFormTabsPresenter getPresenter() {
        return presenter;
    }

}
