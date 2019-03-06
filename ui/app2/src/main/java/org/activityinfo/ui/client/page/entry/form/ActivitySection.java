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
package org.activityinfo.ui.client.page.entry.form;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodSet;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.page.entry.form.field.PartnerComboBox;
import org.activityinfo.ui.client.page.entry.form.field.ProjectComboBox;

import java.util.List;

public class ActivitySection extends FormSectionWithFormLayout<SiteDTO> {

    private final ActivityFormDTO activity;

    private DateField dateField1;
    private DateField dateField2;
    private PartnerComboBox partnerCombo;
    private ProjectComboBox projectCombo;

    public ActivitySection(LockedPeriodSet locks, final ActivityFormDTO activity, boolean isNewSite) {
        super();

        this.activity = activity;

        getFormLayout().setLabelWidth(100);
        getFormLayout().setDefaultWidth(200);

        TextField<String> databaseField = new TextField<>();
        databaseField.setValue(activity.getDatabaseName());
        databaseField.setFieldLabel(I18N.CONSTANTS.database());
        databaseField.setReadOnly(true);
        add(databaseField);

        TextField<String> activityField = new TextField<>();
        activityField.setValue(activity.getName());
        activityField.setFieldLabel(I18N.CONSTANTS.activity());
        activityField.setReadOnly(true);
        add(activityField);

        partnerCombo = new PartnerComboBox(determinePartners(activity, isNewSite));
        add(partnerCombo);

        projectCombo = new ProjectComboBox(activity);
        if (!activity.getProjects().isEmpty()) {
            add(projectCombo);
        }

        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {

            dateField1 = new DateField();
            dateField1.setName("date1");
            dateField1.setAllowBlank(false);
            dateField1.setFieldLabel(I18N.CONSTANTS.startDate());
            dateField1.setPropertyEditor(new DateTimePropertyEditor(LocalDate.ISO_FORMAT));
            add(dateField1);

            dateField2 = new DateField();
            dateField2.setName("date2");
            dateField2.setAllowBlank(false);
            dateField2.setFieldLabel(I18N.CONSTANTS.endDate());
            dateField2.setValidator((field, value) -> validateDateRange(locks, activity));
            dateField2.setPropertyEditor(new DateTimePropertyEditor(LocalDate.ISO_FORMAT));

            add(dateField2);

        }
    }

    private List<PartnerDTO> determinePartners(ActivityFormDTO activity, boolean isNewSite) {
        if (isNewSite) {
            return activity.isCreateAllAllowed()
                    ? activity.getPartnerRange()
                    : activity.getAssignedPartners();
        } else {
            return activity.isEditAllAllowed()
                    ? activity.getPartnerRange()
                    : activity.getAssignedPartners();
        }
    }

    private String validateDateRange(LockedPeriodSet locks, ActivityFormDTO activity) {
        if (dateField1.getValue() != null && dateField2.getValue() != null) {
            if (dateField2.getValue().before(dateField1.getValue())) {
                return I18N.CONSTANTS.inconsistentDateRangeWarning();
            }
            if (locks.isActivityLocked(activity.getId(), dateField2.getValue())) {
                return I18N.CONSTANTS.dateFallsWithinLockedPeriodWarning();
            }
            if (projectCombo.getValue() != null) {
                int projectId = projectCombo.getValue().getId();
                if (locks.isProjectLocked(projectId, dateField2.getValue())) {
                    return I18N.CONSTANTS.dateFallsWithinLockedPeriodWarning();
                }
            }
        }
        return null;
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            valid &= dateField1.validate();
            valid &= dateField2.validate();
        }
        valid &= partnerCombo.validate();
        valid &= projectCombo.validate();
        return valid;
    }

    @Override
    public void updateModel(SiteDTO m) {
        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            m.setDate1(dateField1.getValue());
            m.setDate2(dateField2.getValue());
        }
        m.setPartner(partnerCombo.getValue());
        m.setProject(projectCombo.getValue());
    }

    @Override
    public void updateForm(SiteDTO m, boolean isNew) {
        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            dateField1.setValue(m.getDate1() == null ? null : m.getDate1().atMidnightInMyTimezone());
            dateField2.setValue(m.getDate2() == null ? null : m.getDate2().atMidnightInMyTimezone());
        }
        partnerCombo.setValue(m.getPartner());
        projectCombo.setValue(m.getProject());
    }

}
