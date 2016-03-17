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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.date.LocalDateRange;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.model.type.subform.PeriodSubFormType;
import org.activityinfo.model.type.subform.SubFormTypeRegistry;
import org.activityinfo.model.type.subform.SubformConstants;
import org.activityinfo.ui.client.component.form.FieldContainer;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.RelevanceHandler;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.widget.ClickHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.Set;

/**
 * @author yuriyz on 02/17/2015.
 */
public class SubFormTabsManipulator {

    private final SubFormTabsPresenter presenter;
    private final ResourceLocator resourceLocator;
    private final StateProvider stateProvider;
    private final boolean designMode;
    private Optional<RelevanceHandler> relevanceHandler = Optional.absent();

    @Nullable
    private PeriodValue periodValue = null; // if not null then period instance generator is in use

    private FormClass subForm;
    private FormModel formModel;
    private FormDesigner formDesigner;

    public SubFormTabsManipulator(@Nonnull ResourceLocator resourceLocator, StateProvider stateProvider, RelevanceHandler relevanceHandler) {
        this.resourceLocator = resourceLocator;
        this.stateProvider = stateProvider;
        this.presenter = new SubFormTabsPresenter(new SubFormTabs(), stateProvider);
        this.designMode = false;
        this.relevanceHandler = Optional.of(relevanceHandler);
    }

    public SubFormTabsManipulator(@Nonnull FormDesigner formDesigner, @Nonnull SubFormTabs tabs) {
        this.resourceLocator = formDesigner.getResourceLocator();
        this.stateProvider = formDesigner.getStateProvider();
        this.formDesigner = formDesigner;
        this.presenter = new SubFormTabsPresenter(tabs, stateProvider);
        this.designMode = true;
    }

    public SubFormTabsManipulator show(@Nonnull FormClass subForm, @Nonnull FormModel formModel) {
        return show(subForm, formModel, false);
    }

    public SubFormTabsManipulator show(@Nonnull FormClass subForm, @Nonnull FormModel formModel, boolean force) {

        Preconditions.checkNotNull(subForm);
        Preconditions.checkNotNull(formModel);

        if (!force && subForm.equals(this.subForm) && (formModel.equals(this.formModel) || designMode)) {
            return this; // we already showing this subform
        }

        this.subForm = subForm;
        this.formModel = formModel;

        ResourceId typeClassId = subForm.getKeyFieldType().get().getRange().iterator().next();

        presenter.setTabCountSafely(SubformConstants.DEFAULT_TAB_COUNT);

        if (PredefinedPeriods.isPeriodId(typeClassId)) {
            generateFormInstanceForPeriod(subForm, typeClassId);
        } else {
            queryFormInstances(typeClassId);
        }
        return this;
    }

    private void generateFormInstanceForPeriod(FormClass subForm, ResourceId typeClassId) {
        this.periodValue = ((PeriodSubFormType) SubFormTypeRegistry.get().getKind(typeClassId)).getPeriod();

        final LocalDateRange selectedRange = getSelectedRange();
        final Date startDate = selectedRange != null ? selectedRange.asDateRange().getStart() : new Date();

        final PeriodInstanceKeyedGenerator instanceGenerator = new PeriodInstanceKeyedGenerator(subForm.getId());
        presenter.setPeriodType(PredefinedPeriods.fromPeriod(periodValue));

        instanceGenerator.generate(periodValue, startDate, PeriodInstanceKeyedGenerator.Direction.BACK, presenter.getTabCount());
        presenter.set(instanceGenerator.next());

        presenter.setShowNextButtons(true);
        presenter.setShowPreviousButtons(true);

        presenter.setMoveButtonClickHandler(new org.activityinfo.ui.client.widget.ClickHandler<SubFormTabsPresenter.ButtonType>() {
            @Override
            public void onClick(SubFormTabsPresenter.ButtonType buttonType) {
                onPeriodMoveButtonClick(buttonType, instanceGenerator);
            }
        });
        presenter.setInstanceTabClickHandler(new ClickHandler<FormInstance>() {
            @Override
            public void onClick(FormInstance instance) {
                onInstanceTabClick(instance);
            }
        });
    }

    private void onInstanceTabClick(FormInstance instance) {
        if (!designMode) {
            formModel.setSelectedInstance(instance, subForm);

            applyInstanceValues(formModel.getSubFormInstances().get(new FormModel.SubformValueKey(subForm, instance)));

            relevanceHandler.get().onValueChange();
        }
    }

    private void onPeriodMoveButtonClick(SubFormTabsPresenter.ButtonType buttonType, PeriodInstanceKeyedGenerator instanceGenerator) {
        presenter.setPeriodType(PredefinedPeriods.fromPeriod(periodValue));
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
                onInstanceTabClick(instance);
            }
        });
    }

    private void applyInstanceValues(FormInstance instance) {
        Set<FieldContainer> containers = formModel.getContainersOfClass(subForm.getId());
        for (FieldContainer fieldContainer : containers) {
            FieldValue fieldValue = instance.get(fieldContainer.getField().getId());
            if (fieldValue != null) {
                fieldContainer.getFieldWidget().setValue(fieldValue);
            } else {
                fieldContainer.getFieldWidget().clearValue();
            }
        }
    }


    private void queryInstances(final InstanceQuery query) {
        resourceLocator.queryInstances(query).then(new Function<QueryResult<FormInstance>, Object>() {
            @Nullable
            @Override
            public Object apply(QueryResult<FormInstance> queryResult) {
                presenter.setShowPreviousButtons(query.getOffset() > 0);
                presenter.setShowNextButtons(queryResult.hasNext(query.getOffset()));
                presenter.setPeriodType(null);
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

    public boolean isDesignMode() {
        return designMode;
    }

    public LocalDateRange getSelectedRange() {
        return presenter.getSelectedRange(PredefinedPeriods.fromPeriod(periodValue));
    }
}
